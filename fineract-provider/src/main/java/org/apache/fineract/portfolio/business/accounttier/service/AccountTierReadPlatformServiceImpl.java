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
package org.apache.fineract.portfolio.business.accounttier.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.accounttier.api.AccountTierApiResouceConstants;
import org.apache.fineract.portfolio.business.accounttier.data.AccountTierData;
import org.apache.fineract.portfolio.business.accounttier.data.CumulativeTransactionsData;
import org.apache.fineract.portfolio.business.accounttier.execption.AccountTierNotFoundException;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountTierReadPlatformServiceImpl implements AccountTierReadPlatformService {

    private final PlatformSecurityContext context;
    private final AccountTierMapper accountTierMapper = new AccountTierMapper();
    private final JdbcTemplate jdbcTemplate;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final CodeValueReadPlatformService codeValueReadPlatformService;

    @Autowired
    public AccountTierReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            final CodeValueReadPlatformService codeValueReadPlatformService
    ) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.savingsAccountRepositoryWrapper = savingsAccountRepositoryWrapper;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
    }

    @Override
    public Collection<AccountTierData> retrieveAll(final Long clientTypeId, final String name) {

        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>(Arrays.asList());
        String sql = "select " + this.accountTierMapper.schema() + " where matl.parent_id  is null ";

        if (clientTypeId != null) {
            sql += "and matl.client_type_cv_id = ?";
            paramList.add(clientTypeId);
        }
        if (name != null) {
            sql += " and matl.name like ?";
            paramList.add("%" + name + "%");
        }
        return this.jdbcTemplate.query(sql, this.accountTierMapper, paramList.toArray()); // NOSONAR

    }

    @Override
    public AccountTierData retrieveOne(final Long accountTierId) {
        try {
            this.context.authenticatedUser();
            final String sql = "select " + this.accountTierMapper.schema() + " where matl.id = ? ";

            AccountTierData accountTierData = this.jdbcTemplate.queryForObject(sql, this.accountTierMapper, // NOSONAR
                    new Object[]{accountTierId});
            Long parentId = accountTierData.getParentId();
            Long id = accountTierData.getId();

            if (parentId == 0) {
                String childSql = "select " + this.accountTierMapper.schema() + "where matl.parent_id = " + id;
                Collection<AccountTierData> childrenAccountTierData = this.jdbcTemplate.query(childSql, this.accountTierMapper);
                accountTierData.setChildrenAccountTierData(childrenAccountTierData);
            }

            return accountTierData;
        } catch (final EmptyResultDataAccessException e) {
            throw new AccountTierNotFoundException(accountTierId, e);
        }
    }

    private static final class AccountTierMapper implements RowMapper<AccountTierData> {

        private final String schemaSql;

        AccountTierMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("matl.id as id, ");
            sqlBuilder.append("matl.client_type_cv_id as clientTypeId, ");
            sqlBuilder.append("matl.parent_id as parentId, ");
            sqlBuilder.append("matl.activation_channel_id as activationChannelId, ");
            sqlBuilder.append("matl.daily_withdrawal_limit as dailyWithdrawalLimit,  ");
            sqlBuilder.append("matl.cumulative_balance as cumulativeBalance, ");
            sqlBuilder.append("matl.single_deposit_limit as singleDepositLimit, ");
            sqlBuilder.append("matl.description as description, ");
            sqlBuilder.append("matl.name as name, ");
            sqlBuilder.append("cvclienttype.code_value as clienttypeValue, ");
            sqlBuilder.append("cvactivationchannel.code_value as activationchannelValue ");
            sqlBuilder.append("from m_account_tier_limit matl ");
            sqlBuilder.append("left join m_code_value cvclienttype on cvclienttype.id = matl.client_type_cv_id ");
            sqlBuilder.append("left join m_code_value cvactivationchannel on cvactivationchannel.id = matl.activation_channel_id ");

            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public AccountTierData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long parentId = rs.getLong("parentId");
            final BigDecimal dailyWithdrawalLimit = rs.getBigDecimal("dailyWithdrawalLimit");
            final BigDecimal cumulativeBalance = rs.getBigDecimal("cumulativeBalance");
            final BigDecimal singleDepositLimit = rs.getBigDecimal("singleDepositLimit");
            final String description = rs.getString("description");
            final String name = rs.getString("name");

            final Long clientTypeId = JdbcSupport.getLong(rs, "clientTypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clientType = CodeValueData.instance(clientTypeId, clienttypeValue);

            final Long activationChannelId = JdbcSupport.getLong(rs, "activationChannelId");
            final String activationchannelValue = rs.getString("activationchannelValue");
            final CodeValueData activationChannel = CodeValueData.instance(activationChannelId, activationchannelValue);

            return AccountTierData.instance(id, clientType, parentId, activationChannel,
                    dailyWithdrawalLimit, singleDepositLimit, cumulativeBalance, description, name);

        }
    }

    @Override
    public CumulativeTransactionsData retrieveCumulativeTransactionsAmount(final Long savingsId, final Long channelId) {

        SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsId);
        Long clientTypeId = savingsAccount.getClient().clientTypeId();

        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        CumulativeTransactionMapper cumulativeTransactionMapper = new CumulativeTransactionMapper();
        SavingsAccountBalanceMapper savingsAccountBalanceMapper = new SavingsAccountBalanceMapper();

        final String cumulativeDepositAmountsql = cumulativeTransactionMapper.schema()
                + " where sa.id = ? and transaction_type_enum = ? and tr.transaction_date = ? ";
        final String savingsAccountBalancesql = savingsAccountBalanceMapper.schema()
                + " where sa.id = ?  ";

        BigDecimal savingsAccountBalance = this.jdbcTemplate.queryForObject(savingsAccountBalancesql, savingsAccountBalanceMapper,
                new Object[]{savingsId});

        BigDecimal cumulativeWithdrawalAmount = this.jdbcTemplate.queryForObject(cumulativeDepositAmountsql, cumulativeTransactionMapper,
                new Object[]{savingsId, SavingsAccountTransactionType.WITHDRAWAL.getValue(), today});

        List<Object> paramList = new ArrayList<>(Arrays.asList());
        String sql = "select " + this.accountTierMapper.schema() + " where matl.client_type_cv_id = ? ";
        paramList.add(clientTypeId);

        if (channelId != null) {
            sql += "and matl.activation_channel_id = ?";
            paramList.add(channelId);
        }

        Collection<AccountTierData> accountTierData = this.jdbcTemplate.query(sql, this.accountTierMapper, paramList.toArray());
        return CumulativeTransactionsData.instance(accountTierData, cumulativeWithdrawalAmount, savingsAccountBalance);

    }

    private static final class CumulativeTransactionMapper implements RowMapper<BigDecimal> {

        private final String schema;

        CumulativeTransactionMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append("select  sum(tr.amount) as cummulativeTransactionAmount "
                    + "from m_savings_account_transaction tr ");
            builder.append("left join m_savings_account sa on tr.savings_account_id = sa.id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public BigDecimal mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final BigDecimal cummulativeTransactionAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "cummulativeTransactionAmount");

            return cummulativeTransactionAmount;

        }
    }

    private static final class SavingsAccountBalanceMapper implements RowMapper<BigDecimal> {

        private final String schema;

        SavingsAccountBalanceMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append("select  account_balance_derived as accountBalanceDerived "
                    + "from m_savings_account sa ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public BigDecimal mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final BigDecimal accountBalanceDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "accountBalanceDerived");

            return accountBalanceDerived;

        }
    }

    @Override

    public AccountTierData retrieveTemplate() {
        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountTierApiResouceConstants.CLIENT_TYPE));
        final List<CodeValueData> channelOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(AccountTierApiResouceConstants.ACTIVATION_CHANNEL));

        return AccountTierData.template(clientTypeOptions, channelOptions);
    }
}
