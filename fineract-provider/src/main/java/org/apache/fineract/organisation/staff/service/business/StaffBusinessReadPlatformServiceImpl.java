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
package org.apache.fineract.organisation.staff.service.business;

import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.statusParameterName;

import com.google.gson.JsonObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.data.business.StaffBusinessData;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformServiceImpl.ClientCountSummaryMapper;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformServiceImpl.LoanActiveSummaryMapper;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformServiceImpl.LoanPrincipalAmountSummaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StaffBusinessReadPlatformServiceImpl implements StaffBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ColumnValidator columnValidator;
    private final PaginationHelper paginationHelper;
    final StaffMapper rm = new StaffMapper();
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanActiveSummaryMapper loanActiveSummaryMapper = new LoanActiveSummaryMapper();
    private final LoanPrincipalAmountSummaryMapper loanPrincipalAmountSummaryMapper = new LoanPrincipalAmountSummaryMapper();
    private final ClientCountSummaryMapper clientCountSummaryMapper = new ClientCountSummaryMapper();

    @Autowired
    public StaffBusinessReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, final PaginationHelper paginationHelper) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
    }

    @Transactional(readOnly = true)
    @Override
    public StaffBusinessData retrieveStaff(final Long staffId) {

        // adding the Authorization criteria so that a user cannot see an
        // employee who does not belong to his office or a sub office for his
        // office.
        final String hierarchy = this.context.authenticatedUser().getOffice().getHierarchy() + "%";

        try {
            final String sql = "select " + rm.schema() + " where s.id = ? and o.hierarchy like ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { staffId, hierarchy }); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(staffId, e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StaffBusinessData> retrieveAll(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(rm.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromStaffCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" where (").append(extraCriteria).append(")");
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
                sqlBuilder.append(" limit ").append(searchParameters.getLimit());
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(" offset ").append(searchParameters.getOffset());
                }
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.rm);
    }

    private String buildSqlStringFromStaffCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        // s.office_id,s.display_name,s.is_active,s.organisational_role_parent_staff_id,s.organisational_role_enum
        final Long officeId = searchParameters.getOfficeId();
        final Long supervisorId = searchParameters.getStaffId();
        final String name = searchParameters.getName();
        final Long isOrganisationalRoleEnumIdPassed = searchParameters.getOrganisationalRoleEnumId();
        final Boolean active = searchParameters.isActive();
        final Boolean isSupervisor = searchParameters.getIsSupervisor();
        final Boolean isLoanOfficer = searchParameters.getIsLoanOfficer();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(s.joining_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(s.joining_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(s.joining_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isSupervisorPassed()) {
            extraCriteria += " and so.isSupervisor = ? ";
            paramList.add(isSupervisor);
        }
        if (searchParameters.isLoanOfficerPassed()) {
            extraCriteria += " and s.is_loan_officer = ? ";
            paramList.add(isLoanOfficer);
        }
        if (searchParameters.isOfficeIdPassed()) {
            extraCriteria += " and s.office_id = ? ";
            paramList.add(officeId);
        }
        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += " and s.organisational_role_parent_staff_id = ? ";
            paramList.add(supervisorId);
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%".concat(name.concat("%")));
            extraCriteria += " and s.display_name like ? ";
        }
        if (searchParameters.isActivePassed()) {
            extraCriteria += " and s.is_active = ? ";
            paramList.add(active);
        }

        if (searchParameters.isOrganisationalRoleEnumIdPassed()) {
            extraCriteria += " and s.organisational_role_enum = ? ";
            paramList.add(isOrganisationalRoleEnumIdPassed);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Transactional(readOnly = true)
    @Override
    public JsonObject retrieveBalance(Long staffId) {
        this.context.authenticatedUser();
        final JsonObject jsonObjectBalance = new JsonObject();

        JsonObject jsonObjectClient = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.clientCountSummaryMapper.schema());
            sqlBuilder.append(" WHERE mcv.staff_id=? ");

            String sql = sqlBuilder.toString();
            jsonObjectClient = this.jdbcTemplate.queryForObject(sql, this.clientCountSummaryMapper, staffId);
            jsonObjectBalance.add("clientAccount", jsonObjectClient);
        } catch (DataAccessException e) {
            log.warn("staffRetrieveBalance Loan: {}", e);
            jsonObjectClient.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("clientAccount", jsonObjectClient);
        }

        JsonObject jsonObjectLoanDIsbursed = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.loanPrincipalAmountSummaryMapper.schema());
            sqlBuilder.append(" WHERE ml.loan_officer_id=? AND ml.loan_status_id=300 ");

            String sql = sqlBuilder.toString();
            jsonObjectLoanDIsbursed = this.jdbcTemplate.queryForObject(sql, this.loanPrincipalAmountSummaryMapper, staffId);
            jsonObjectBalance.add("loanDisbursed", jsonObjectLoanDIsbursed);
        } catch (DataAccessException e) {
            log.warn("staffRetrieveBalance Loan: {}", e);
            jsonObjectLoanDIsbursed.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("loanDisbursed", jsonObjectLoanDIsbursed);
        }

        JsonObject jsonObjectLoan = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.loanActiveSummaryMapper.schema());
            sqlBuilder.append(" WHERE ml.loan_officer_id=? ");

            String sql = sqlBuilder.toString();
            jsonObjectLoan = this.jdbcTemplate.queryForObject(sql, this.loanActiveSummaryMapper, staffId);
            jsonObjectBalance.add("loanAccount", jsonObjectLoan);
        } catch (DataAccessException e) {
            log.warn("staffRetrieveBalance Loan: {}", e);
            jsonObjectLoan.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("loanAccount", jsonObjectLoan);
        }
        return jsonObjectBalance;
    }

    private static final class StaffMapper implements RowMapper<StaffBusinessData> {

        public String schema() {
            return " s.id as id,s.office_id as officeId, o.name as officeName, s.firstname as firstname, s.lastname as lastname, s.email_address as emailAddress, "
                    + " s.display_name as displayName, s.is_loan_officer as isLoanOfficer, s.external_id as externalId, s.mobile_no as mobileNo,"
                    + " s.is_active as isActive, s.joining_date as joiningDate, so.isSupervisor, "
                    + " s.organisational_role_parent_staff_id organisationalRoleParentStaff, ms.display_name organisationalRoleParentStaffName, s.organisational_role_enum organisationalRoleType, mcv.code_value organisationalRoleTypeName "
                    + " from m_staff s " + " join m_office o on o.id = s.office_id "
                    + " left join m_staff ms on ms.id=s.organisational_role_parent_staff_id "
                    + " left join staffOther so on so.staff_id=s.id " + " left join m_code_value mcv on mcv.id=s.organisational_role_enum ";
        }

        @Override
        public StaffBusinessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String emailAddress = rs.getString("emailAddress");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
            final String officeName = rs.getString("officeName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isActive = rs.getBoolean("isActive");
            final boolean isSupervisor = rs.getBoolean("isSupervisor");
            final LocalDate joiningDate = JdbcSupport.getLocalDate(rs, "joiningDate");

            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaff = JdbcSupport.getLongDefaultToNullIfZero(rs, "organisationalRoleParentStaff");
            if (organisationalRoleParentStaff != null) {
                final String organisationalRoleParentStaffName = rs.getString("organisationalRoleParentStaffName");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaff, organisationalRoleParentStaffName);
            }

            CodeValueData organisationalRoleTypeData = null;
            final Long organisationalRoleType = JdbcSupport.getLongDefaultToNullIfZero(rs, "organisationalRoleType");
            if (organisationalRoleType != null) {
                final String organisationalRoleTypeName = rs.getString("organisationalRoleTypeName");
                organisationalRoleTypeData = CodeValueData.instance(organisationalRoleType, organisationalRoleTypeName);
            }

            return StaffBusinessData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId,
                    mobileNo, isActive, joiningDate, organisationalRoleTypeData, supervisorStaffData, isSupervisor, emailAddress);
        }
    }

}
