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
package org.apache.fineract.portfolio.savings.service.business;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.business.DepositAccountBusinessData;
import org.apache.fineract.portfolio.savings.exception.DepositAccountNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositsBusinessReadPlatformServiceImpl implements DepositsBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    // data mappers
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DepositViewMapper depositViewMapper = new DepositViewMapper();

    private final ColumnValidator columnValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<DepositAccountBusinessData> retrieveAll(final SearchParametersBusiness searchParameters) {

        final String userOfficeHierarchy = this.context.officeHierarchy();
        final String underHierarchySearchString = userOfficeHierarchy + "%";

        List<Object> paramList = new ArrayList<>(Arrays.asList(underHierarchySearchString));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.depositViewMapper.schema());

        sqlBuilder.append(" join m_office o on o.id = ms.office_id");
        sqlBuilder.append(" where o.hierarchy like ?");

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromDepositCriteria(searchParameters, paramList);

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
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.depositViewMapper);
    }

    private String buildSqlStringFromDepositCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        final Long productId = searchParameters.getProductId();
        final Long clientId = searchParameters.getClientId();
        final Integer depositTypeId = searchParameters.getStatusId();
        final Integer statusId = searchParameters.getStatusId();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String accountNo = searchParameters.getAccountNo();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(ms.submittedon_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(ms.submittedon_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(ms.submittedon_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isClientIdPassed()) {
            paramList.add(clientId);
            extraCriteria += " and ms.client_id = ? ";
        }
        if (searchParameters.isProductIdPassed()) {
            paramList.add(productId);
            extraCriteria += " and ms.product_id = ? ";
        }
        if (searchParameters.isDepositTypeIdPassed()) {
            paramList.add(depositTypeId);
            extraCriteria += " and ms.deposit_type_enum = ? ";
        }

        if (externalId != null) {
            paramList.add("%" + externalId + "%");
            extraCriteria += " and ms.external_id like ? ";
        }

        if (searchParameters.isAccountNoPassed()) {
            paramList.add(accountNo);
            extraCriteria += " and ms.account_no = ? ";
        }
        if (searchParameters.isOfficeIdPassed()) {
            extraCriteria += " and ms.office_id = ? ";
            paramList.add(officeId);
        }

        if (searchParameters.isStatusIdPassed()) {
            extraCriteria += " and ms.status_enum = ? ";
            paramList.add(statusId);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public DepositAccountBusinessData retrieveBalance(String accountNo) {
        this.context.authenticatedUser();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.depositViewMapper.schema());
            sqlBuilder.append(" WHERE ms.account_no=? ");

            String sql = sqlBuilder.toString();

            final DepositAccountBusinessData depositAccountBusinessData = this.jdbcTemplate.queryForObject(sql, this.depositViewMapper, accountNo);
            final DepositAccountBusinessData accountBusinessData = DepositAccountBusinessData.retrieveBalance(depositAccountBusinessData);
            return accountBusinessData;

        } catch (DataAccessException e) {
            log.warn("retrieveBalance: {}", e);
            throw new DepositAccountNotFoundException("Deposit account with account " + accountNo + " does not exist");
        }
    }

    @Override
    public DepositAccountBusinessData retrieveName(String accountNo) {
        this.context.authenticatedUser();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.depositViewMapper.schema());
            sqlBuilder.append(" WHERE ms.account_no=? ");

            String sql = sqlBuilder.toString();

            final DepositAccountBusinessData depositAccountBusinessData = this.jdbcTemplate.queryForObject(sql, this.depositViewMapper, accountNo);
            final DepositAccountBusinessData accountBusinessData = DepositAccountBusinessData.retrieveName(depositAccountBusinessData);
            return accountBusinessData;

        } catch (DataAccessException e) {
            log.warn("retrieveName: {}", e);
            throw new DepositAccountNotFoundException("Deposit account with account " + accountNo + " does not exist");
        }
    }

    private static final class DepositViewMapper implements RowMapper<DepositAccountBusinessData> {

        private final String schema;

        DepositViewMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" ms.office_id officeId, ms.office_name officeName, ms.id, ms.external_id externalId, ms.account_no accountNo, ms.product_id productId, ms.product_name productName, ms.deposit_type_enum depositType, ");
            sqlBuilder.append(" ms.client_id clientId, ms.display_name displayName, ms.ledger_balance ledgerBalance, ms.available_balance availableBalance, ");
            sqlBuilder.append(" ms.submittedon_date createdOn, ms.activatedon_date activatedOn, ms.last_transaction_date lastTransactionOn, ms.status_enum as statusEnum ");
            sqlBuilder.append(" from m_saving_view ms ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public DepositAccountBusinessData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final String externalId = rs.getString("externalId");
            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final SavingsAccountStatusEnumData status = SavingsEnumerations.status(statusEnum);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("displayName");

            final Long productId = rs.getLong("productId");
            final String productName = rs.getString("productName");

            final Integer depositTypeId = rs.getInt("depositType");
            final EnumOptionData depositType = SavingsEnumerations.depositType(depositTypeId);

            final BigDecimal availableBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "availableBalance");
            final BigDecimal ledgerBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "ledgerBalance");
            final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOn");
            final LocalDate activatedOn = JdbcSupport.getLocalDate(rs, "activatedOn");
            final LocalDate lastTransactionOn = JdbcSupport.getLocalDate(rs, "lastTransactionOn");

            return DepositAccountBusinessData.lookUp(id, accountNo, depositType, status, clientId, clientName, productId, productName, availableBalance, ledgerBalance, createdOn, activatedOn, lastTransactionOn, externalId, officeId, officeName);

        }

    }

}
