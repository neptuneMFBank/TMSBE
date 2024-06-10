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
package org.apache.fineract.portfolio.business.bankTransfer.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.business.bankTransfer.data.TransferApprovalData;
import org.apache.fineract.portfolio.business.bankTransfer.domain.BankTransferEnumerations;
import org.apache.fineract.portfolio.business.bankTransfer.domain.BankTransferType;
import org.apache.fineract.portfolio.business.bankTransfer.exception.TransferApprovalNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferApprovalReadPlatformServiceImpl implements TransferApprovalReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final TransferApprovalMapper transferApprovalMapper = new TransferApprovalMapper();
    private final ColumnValidator columnValidator;

    @Override
    @Transactional(readOnly = true)
    public Page<TransferApprovalData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(400);
        sqlBuilder.append("select ").append(sqlGenerator.calcFoundRows()).append(" ");
        sqlBuilder.append(this.transferApprovalMapper.schema());

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
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
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.transferApprovalMapper);
    }

    private String buildSqlStringFromClientCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long transferType = searchParameters.getTransferType();
        final Long tobankId = searchParameters.getTobankId();
        final String toAccountNumber = searchParameters.getToAccountNumber();
        final String fromAccountNumber = searchParameters.getFromAccountNumber();

        final Integer status = searchParameters.getStatusId();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(mta.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(mta.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(mta.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }
        if (transferType != null) {
            extraCriteria += " and mta.transfer_type = ? ";
            paramList.add(transferType);
        }

        if (searchParameters.isStatusIdPassed()) {
            paramList.add(status);
            extraCriteria += " and mta.status = ? ";
        }

        if (tobankId != null) {
            paramList.add(tobankId);
            extraCriteria += " and mta.to_bank_id like ? ";
        }

        if (toAccountNumber != null) {
            paramList.add(toAccountNumber);
            extraCriteria += " and mta.to_account_number like ? ";
        }
        if (fromAccountNumber != null) {
            paramList.add(fromAccountNumber);
            extraCriteria += " and mta.from_account_number like ? ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public TransferApprovalData retrieveOne(final Long transferApprovalId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + this.transferApprovalMapper.schema()
                    + " where  mta.id = ?";
            final TransferApprovalData transferApprovalData = this.jdbcTemplate.queryForObject(sql, this.transferApprovalMapper, // NOSONAR
                    transferApprovalId);

            return transferApprovalData;
        } catch (final EmptyResultDataAccessException e) {
            throw new TransferApprovalNotFoundException(transferApprovalId);
        }
    }

    @Override
    public TransferApprovalData retrieveTemplate() {
        this.context.authenticatedUser();

        final Collection<EnumOptionData> transferTypeOptions = BankTransferEnumerations.bankAccountTransferTypes(BankTransferType.values());

        final TransferApprovalData transferApprovalData = TransferApprovalData.template(transferTypeOptions);
        return transferApprovalData;
    }

    private static final class TransferApprovalMapper implements RowMapper<TransferApprovalData> {

        private final String schema;

        TransferApprovalMapper() {

            final StringBuilder builder = new StringBuilder(400);

            builder.append(
                    "mta.id as id,"
                    + " mta.amount as amount,"
                    + " mta.status as status, "
                    + " mta.transfer_type as transferType,"
                    + " mta.hold_transaction_id as holdTransactionId,"
                    + " mta.release_transaction_id as releaseTransactionId,"
                    + " mta.withdraw_transaction_id as withdrawTransactionId,"
                    + " mta.from_account_id as fromAccountId,"
                    + " mta.from_account_type as fromAccountType,"
                    + " mta.from_account_number as fromAccountNumber,"
                    + " mta.from_account_name as fromAccountName,"
                    + " mta.to_account_id as toAccountId,"
                    + " mta.to_account_type as toAccountType,"
                    + " mta.to_account_number as toAccountNumber,"
                    + " mta.to_bank_id as toBankId,"
                    + " mta.activation_channel_id as activationChannelId,"
                    + " mta.reason as reason, mta.note as note ");
            builder.append(
                    " mta.created_by as createdById,"
                    + " mta.created_on_utc as createdOnUtc,"
                    + " mta.last_modified_by as lastModifiedBy,"
                    + " mta.last_modified_on_utc as lastModifiedOnUtc, ");

            builder.append(" sbu.username as createdByUsername, ");
            builder.append(" sbu.firstname as createdByFirstname, ");
            builder.append(" sbu.lastname as createdByLastname, ");
            builder.append(" cv.code_value as activationChannelValue, ");
            builder.append(" cvb.code_value as bankIdValue ");

            builder.append(" from m_transfer_approval mta ");
            builder.append(" left join m_appuser sbu on sbu.id = mta.created_by ");
            builder.append(" left join m_code_value cv on cv.id = mta.activation_channel_id ");
            builder.append(" left join m_code_value cvb on cvb.id = mta.to_bank_id ");

            this.schema = builder.toString();

        }

        public String schema() {
            return this.schema;
        }

        @Override
        public TransferApprovalData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
            final Integer status = JdbcSupport.getInteger(rs, "status");
            final Integer transferTypeId = JdbcSupport.getInteger(rs, "transferType");
            EnumOptionData transferType = BankTransferEnumerations.status(transferTypeId);
            final Integer holdTransactionId = JdbcSupport.getInteger(rs, "holdTransactionId");
            final Integer releaseTransactionId = JdbcSupport.getInteger(rs, "releaseTransactionId");
            final Integer withdrawTransactionId = JdbcSupport.getInteger(rs, "withdrawTransactionId");
            final Integer fromAccountId = JdbcSupport.getInteger(rs, "fromAccountId");
            final Integer fromAccountType = JdbcSupport.getInteger(rs, "fromAccountType");
            final String fromAccountNumber = rs.getString("fromAccountNumber");
            final Integer toAccountId = JdbcSupport.getInteger(rs, "toAccountId");
            final Integer toAccountType = JdbcSupport.getInteger(rs, "toAccountType");

            final String toAccountNumber = rs.getString("toAccountNumber");
            final String reason = rs.getString("reason");

            final Long activationChannelId = JdbcSupport.getLong(rs, "activationChannelId");
            final String activationChannelValue = rs.getString("activationChannelValue");
            final CodeValueData activationChannel = CodeValueData.instance(activationChannelId, activationChannelValue);

            final Long toBankId = JdbcSupport.getLong(rs, "toBankId");
            final String toBankValue = rs.getString("bankIdValue");
            final CodeValueData toBank = CodeValueData.instance(toBankId, toBankValue);

            final String createdByUsername = rs.getString("createdByUsername");
            final String createdByFirstname = rs.getString("createdByFirstname");
            final String createdByLastname = rs.getString("createdByLastname");
            final Long createdById = JdbcSupport.getLong(rs, "createdById");
            final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOnUtc");
            final String fromAccountName = rs.getString("fromAccountName");
            final String note = rs.getString("note");

            return TransferApprovalData.instance(id, amount, status, transferType, holdTransactionId, releaseTransactionId,
                    withdrawTransactionId, fromAccountId, fromAccountType, fromAccountNumber, toAccountId, toAccountType,
                    toAccountNumber, activationChannel, toBank, reason, createdByUsername, createdByFirstname,
                    createdByLastname, createdById, createdOn, fromAccountName, note);

        }
    }
}
