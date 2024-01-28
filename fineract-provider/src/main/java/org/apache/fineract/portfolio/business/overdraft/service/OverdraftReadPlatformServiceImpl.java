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
package org.apache.fineract.portfolio.business.overdraft.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.overdraft.data.OverdraftData;
import org.apache.fineract.portfolio.business.overdraft.domain.Overdraft;
import org.apache.fineract.portfolio.business.overdraft.domain.OverdraftRepositoryWrapper;
import org.apache.fineract.portfolio.business.overdraft.exception.OverdraftNotFoundException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdraftReadPlatformServiceImpl implements OverdraftReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final OverdraftMapper overdraftMapper = new OverdraftMapper();
    private final OverdraftRepositoryWrapper overdraftRepositoryWrapper;

    @Override
    @Transactional
    @CronTarget(jobName = JobName.UPDATE_DUE_OVERDRAFT)
    public void updateDueOverdraft() {
        final String sqlFinder = "select moev.overdraft_id overdraftId from m_overdraft_expired_view moev ";
        List<Long> overdraftDue = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        for (Long overdraftDueId : overdraftDue) {
            final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftDueId);
            final SavingsAccount savingsAccount = overdraft.getSavingsAccount();
            final Long savingsAccountId = savingsAccount.getId();
            String sql = "UPDATE m_savings_account ms SET ms.allow_overdraft=?, ms.overdraft_limit=?, ms.nominal_annual_interest_rate_overdraft=? WHERE ms.id=?";
            this.jdbcTemplate.update(sql, false, 0, 0, savingsAccountId);

        }
        log.info("{}: Records overdraft due: {}", ThreadLocalContextUtil.getTenant().getName(),
                overdraftDue.size());
    }

    @Override
    public Page<OverdraftData> retrieveAll(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(overdraftMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromMetricsCriteria(searchParameters, paramList);
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

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.overdraftMapper);
    }

    @Override
    public OverdraftData retrieveOne(Long overdraftId) {
        try {
            this.context.authenticatedUser();
            final OverdraftMapper rm = new OverdraftMapper();
            final String sql = "select " + rm.schema() + " where ov.id = ?";
            return this.jdbcTemplate.queryForObject(sql, rm, new Object[]{overdraftId});
        } catch (final EmptyResultDataAccessException e) {
            throw new OverdraftNotFoundException(overdraftId);
        }
    }

    @Override
    public OverdraftData retrieveTemplate() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private String buildSqlStringFromMetricsCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        final Long savingsId = searchParameters.getSavingsId();
        final Integer statusId = searchParameters.getStatusId();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(ov.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(ov.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(ov.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isStatusIdPassed()) {
            extraCriteria += " and ov.status_enum = ? ";
            paramList.add(statusId);
        }

        if (searchParameters.isSavingsIdPassed()) {
            extraCriteria += " and ov.savings_id = ? ";
            paramList.add(savingsId);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class OverdraftMapper implements RowMapper<OverdraftData> {

        public String schema() {
            return " ov.id, ov.status_enum statusEnum, ov.amount, ov.nominal_annual_interest_rate_overdraft nominalAnnualInterestRateOverdraft, "
                    + " ov.start_date startDate, ov.expiry_date expiryDate, ov.savings_id savingsId, "
                    + " ov.created_on_utc createdOn, ov.last_modified_on_utc modifiedOn, "
                    + " sbu.username as submittedByUsername, sbum.username as modifiedByUsername "
                    + " from m_overdraft ov "
                    + " left join m_appuser sbu on sbu.id = ov.created_by "
                    + " left join m_appuser sbum on sbum.id = ov.last_modified_by ";
        }

        @Override
        public OverdraftData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = LoanApprovalStatus.status(statusEnum);
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
            final BigDecimal nominalAnnualInterestRateOverdraft = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "nominalAnnualInterestRateOverdraft");
            final LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
            final LocalDate expiryDate = JdbcSupport.getLocalDate(rs, "expiryDate");
            final Integer numberOfDays = GeneralConstants.numberOfDays(startDate, expiryDate);
            final Long savingsId = JdbcSupport.getLongDefaultToNullIfZero(rs, "savingsId");

            final LocalDateTime createdOnTime = JdbcSupport.getLocalDateTime(rs, "createdOn");
            final LocalDate createdOn = createdOnTime != null ? createdOnTime.toLocalDate() : null;

            final LocalDateTime modifiedOnTime = JdbcSupport.getLocalDateTime(rs, "modifiedOn");
            final LocalDate modifiedOn = modifiedOnTime != null ? modifiedOnTime.toLocalDate() : null;

            final String createdByUser = rs.getString("submittedByUsername");
            final String modifiedByUser = rs.getString("modifiedByUsername");

            return OverdraftData.instance(amount, nominalAnnualInterestRateOverdraft, startDate, expiryDate, createdByUser, modifiedByUser, createdOn, modifiedOn, id, savingsId, status, numberOfDays);
        }

    }

}
