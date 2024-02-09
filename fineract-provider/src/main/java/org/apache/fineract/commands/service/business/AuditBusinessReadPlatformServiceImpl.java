/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.commands.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.ProcessingResultLookup;
import org.apache.fineract.commands.data.business.AuditBusinessSearchData;
import org.apache.fineract.infrastructure.core.data.PaginationParametersDataValidator;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditBusinessReadPlatformServiceImpl implements AuditBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final AppUserReadPlatformService appUserReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final PaginationParametersDataValidator paginationParametersDataValidator;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final ColumnValidator columnValidator;
    private final AuditMapper auditMapper = new AuditMapper();

    @Override
    public Page<AuditData> retrieveAll(SearchParametersBusiness searchParameters) {

        final String userOfficeHierarchy = this.context.officeHierarchy();
        final String underHierarchySearchString = userOfficeHierarchy + "%";

        List<Object> paramList = new ArrayList<>(Arrays.asList(underHierarchySearchString));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ").append(sqlGenerator.calcFoundRows()).append(" ");
        sqlBuilder.append(this.auditMapper.schema());
        sqlBuilder.append(" where (o.hierarchy like ?) ");

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromAuditCriteria(searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" and (").append(extraCriteria).append(")");
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.auditMapper);
    }

    private static final class AuditMapper implements RowMapper<AuditData> {

        public String schema() {

            String partSql = " aud.id as id, aud.action_name as actionName, aud.entity_name as entityName,"
                    + " aud.resource_id as resourceId,aud.client_id as clientId, aud.loan_id as loanId,"
                    + " aud.maker, aud.made_on_date as madeOnDate, " + " aud.api_get_url as resourceGetUrl, "
                    + "aud.checker, aud.checked_on_date as checkedOnDate, aud.processingResult, "
                    + " aud.officeName, aud.groupLevelName, aud.groupName, aud.clientName, "
                    + " aud.loanAccountNo, aud.savingsAccountNo "
                    + " from m_audit_view aud "
                    + " LEFT JOIN m_office o ON o.id=aud.office_id ";
            return partSql;
        }

        @Override
        public AuditData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String actionName = rs.getString("actionName");
            final String entityName = rs.getString("entityName");
            final Long resourceId = JdbcSupport.getLong(rs, "resourceId");
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final Long loanId = JdbcSupport.getLong(rs, "loanId");
            final String maker = rs.getString("maker");
            final ZonedDateTime madeOnDate = JdbcSupport.getDateTime(rs, "madeOnDate");
            final String checker = rs.getString("checker");
            final ZonedDateTime checkedOnDate = JdbcSupport.getDateTime(rs, "checkedOnDate");
            final String processingResult = rs.getString("processingResult");
            final String resourceGetUrl = rs.getString("resourceGetUrl");
            String commandAsJson = null;

            final String officeName = rs.getString("officeName");
            final String groupLevelName = rs.getString("groupLevelName");
            final String groupName = rs.getString("groupName");
            final String clientName = rs.getString("clientName");
            final String loanAccountNo = rs.getString("loanAccountNo");
            final String savingsAccountNo = rs.getString("savingsAccountNo");

            return new AuditData(id, actionName, entityName, resourceId, null, maker, madeOnDate, checker, checkedOnDate,
                    processingResult, commandAsJson, officeName, groupLevelName, groupName, clientName, loanAccountNo, savingsAccountNo,
                    clientId, loanId, resourceGetUrl);
        }
    }

    @Override
    public AuditBusinessSearchData retrieveSearchTemplate(final String useType) {

        if (!(useType.equals("audit") || useType.equals("makerchecker"))) {
            throw new PlatformDataIntegrityException("error.msg.invalid.auditSearchTemplate.useType",
                    "Invalid Audit Search Template UseType: " + useType);
        }

        final AppUser currentUser = this.context.authenticatedUser();

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        String sql = " SELECT distinct(action_name) as actionName, CASE WHEN action_name in ('CREATE', 'DELETE', 'UPDATE') THEN action_name ELSE 'OTHERS' END as classifier "
                + " FROM m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by classifier, action_name";
        final ActionNamesMapper mapper = new ActionNamesMapper();
        final List<String> actionNames = this.jdbcTemplate.query(sql, mapper); // NOSONAR

        sql = " select distinct(entity_name) as entityName, CASE WHEN " + sqlGenerator.escape("grouping")
                + " = 'datatable' THEN 'OTHERS' ELSE entity_name END as classifier " + " from m_permission p ";
        sql += makercheckerCapabilityOnly(useType, currentUser);
        sql += " order by classifier, entity_name";
        final EntityNamesMapper mapper2 = new EntityNamesMapper();
        final List<String> entityNames = this.jdbcTemplate.query(sql, mapper2); // NOSONAR

        Collection<ProcessingResultLookup> processingResults = null;
        if (useType.equals("audit")) {
            final ProcessingResultsMapper mapper3 = new ProcessingResultsMapper();
            processingResults = this.jdbcTemplate.query(mapper3.schema(), mapper3);
        }

        return new AuditBusinessSearchData(offices, actionNames, entityNames, processingResults);
    }

    private String makercheckerCapabilityOnly(final String useType, final AppUser currentUser) {
        String sql = "";
        Boolean isLimitedChecker = false;
        if (useType.equals("makerchecker")) {
            if (currentUser.hasNotPermissionForAnyOf("ALL_FUNCTIONS", "CHECKER_SUPER_USER")) {
                isLimitedChecker = true;
            }
        }

        if (isLimitedChecker) {
            sql += " join m_role_permission rp on rp.permission_id = p.id" + " join m_role r on r.id = rp.role_id "
                    + " join m_appuser_role ur on ur.role_id = r.id and ur.appuser_id = " + currentUser.getId();

        }
        sql += " where p.action_name is not null and p.action_name <> 'READ' ";
        if (isLimitedChecker) {
            sql += "and p.code like '%\\_CHECKER'";
        }
        return sql;
    }

    private static final class ActionNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            return rs.getString("actionName");
        }

    }

    private static final class EntityNamesMapper implements RowMapper<String> {

        @Override
        public String mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            return rs.getString("entityName");
        }

    }

    private static final class ProcessingResultsMapper implements RowMapper<ProcessingResultLookup> {

        @Override
        public ProcessingResultLookup mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String processingResult = rs.getString("processingResult");

            return new ProcessingResultLookup(id, processingResult);
        }

        public String schema() {
            return " select enum_id as id, enum_message_property as processingResult from r_enum_value where enum_name = 'processing_result_enum' "
                    + " order by enum_id";
        }
    }

    private String buildSqlStringFromAuditCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

//    pcs.id, pcs.action_name, pcs.entity_name, pcs.office_id, o.name office_name, pcs.api_get_url, pcs.resource_id, pcs.maker_id,
//    pcs.made_on_date, pcs.checker_id, pcs.checked_on_date, pcs.processing_result_enum,
//    pcs.group_id, pcs.client_id, pcs.loan_id, pcs.savings_account_id
//aud.
        final Long officeId = searchParameters.getOfficeId();
        final Long makerCheckerId = searchParameters.getStaffId();
        final Long groupId = searchParameters.getIndustryId();//groupId
        final Long loanId = searchParameters.getLoanId();
        final Long clientId = searchParameters.getClientId();
        final Long savingsAccountId = searchParameters.getSavingsId();
        final String externalId = searchParameters.getExternalId();//actionName
        final String displayName = searchParameters.getName();//entityName
        final Integer type = searchParameters.getType();//processingResult
        final Boolean isChecker = searchParameters.getIsSupervisor();

        String dateColumn;
        String makerCheckerColumn;

        String extraCriteria = "";
        if (BooleanUtils.isTrue(isChecker)) {
            dateColumn = "aud.checked_on_date";
            makerCheckerColumn = "aud.checker_id";
        } else {
            dateColumn = "aud.made_on_date";
            makerCheckerColumn = "aud.maker_id";
        }
        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(" + dateColumn + " AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(" + dateColumn + " AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(" + dateColumn + " AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (makerCheckerId != null) {
            extraCriteria += " and " + makerCheckerColumn + " = ? ";
            paramList.add(makerCheckerId);
        }
        if (officeId != null) {
            extraCriteria += " and aud.office_id = ? ";
            paramList.add(officeId);
        }

        if (externalId != null) {
            paramList.add(externalId);
            extraCriteria += " and aud.action_name = ? ";
        }

        if (displayName != null) {
            paramList.add(displayName);
            extraCriteria += " and aud.entity_name = ? ";
        }
        if (searchParameters.isTypePassed()) {
            paramList.add(type);
            extraCriteria += " and aud.processing_result_enum = ? ";
        }

        if (groupId != null) {
            paramList.add(groupId);
            extraCriteria += " and aud.group_id = ? ";
        }
        if (loanId != null) {
            paramList.add(loanId);
            extraCriteria += " and aud.loanId = ? ";
        }
        if (clientId != null) {
            paramList.add(clientId);
            extraCriteria += " and aud.client_id = ? ";
        }
        if (savingsAccountId != null) {
            paramList.add(savingsAccountId);
            extraCriteria += " and aud.savings_account_id = ? ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

}
