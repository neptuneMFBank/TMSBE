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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.data.GLAccountDataForLookup;
import org.apache.fineract.accounting.rule.data.AccountingRuleData;
import org.apache.fineract.accounting.rule.service.AccountingRuleReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.paymentdetail.data.PaymentDetailData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.business.PaymentCalculationType;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionEnumData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.business.CommissionVend;
import org.apache.fineract.portfolio.savings.domain.business.CommissionVendRepository;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SavingsAccountBusinessReadPlatformServiceImpl implements SavingsAccountBusinessReadPlatformService {

    // mappers
    SavingsAccountTransactionsMapper transactionsMapper;

    private final PaginationHelper paginationHelper;

    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final JdbcTemplate jdbcTemplate;
    private final CommissionVendRepository commissionVendRepository;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final AccountingRuleReadPlatformService accountingRuleReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public SavingsAccountBusinessReadPlatformServiceImpl(final ColumnValidator columnValidator,
            final SavingsAccountAssembler savingAccountAssembler, PaginationHelper paginationHelper,
            final DatabaseSpecificSQLGenerator sqlGenerator, final JdbcTemplate jdbcTemplate, final CommissionVendRepository commissionVendRepository, final ConfigurationReadPlatformService configurationReadPlatformService,
            final AccountingRuleReadPlatformService accountingRuleReadPlatformService, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.transactionsMapper = new SavingsAccountTransactionsMapper();

        this.columnValidator = columnValidator;
        this.paginationHelper = paginationHelper;
        this.sqlGenerator = sqlGenerator;
        this.jdbcTemplate = jdbcTemplate;
        this.commissionVendRepository = commissionVendRepository;
        this.configurationReadPlatformService = configurationReadPlatformService;
        this.accountingRuleReadPlatformService = accountingRuleReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.COMMISSION_VEND_EOD)
    public void commissionVendEod() {
        final GlobalConfigurationPropertyData calculateCommission = this.configurationReadPlatformService
                .retrieveGlobalConfigurationX("calculate-commission");
        if (calculateCommission.isEnabled()) {
            //run commission vend calculation
            final List<CommissionVend> commissionVends = this.commissionVendRepository.findAll();
            if (commissionVends != null) {
                for (CommissionVend commissionVend : commissionVends) {
                    final Long transactionId = commissionVend.getId();

                    final Integer calculationType = commissionVend.getCalculationType();
                    final PaymentCalculationType paymentCalculationType = PaymentCalculationType.fromInt(calculationType);
                    final BigDecimal gridPercent = commissionVend.getGridPercent();
                    final BigDecimal gridAmount = commissionVend.getGridAmount();
                    final BigDecimal amount = commissionVend.getAmount();

                    BigDecimal commissionAmount = BigDecimal.ZERO;
                    if (paymentCalculationType.isCapped()) {
                        commissionAmount = amount.multiply(gridPercent).divide(BigDecimal.valueOf(100L), MoneyHelper.getRoundingMode());
                        if (commissionAmount.compareTo(gridAmount) > 0) {
                            commissionAmount = gridAmount;
                        }
                    } else if (paymentCalculationType.isPercentage()) {
                        commissionAmount = amount.multiply(gridPercent).divide(BigDecimal.valueOf(100L), MoneyHelper.getRoundingMode());
                    } else {
                        //isFlat or zero
                        commissionAmount = gridAmount;
                    }

                    int status = SavingsAccountStatusType.CLOSED.getValue();
                    if (commissionAmount == null || commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        status = SavingsAccountStatusType.REJECTED.getValue();
                    } else {
                        //run frequent posting

                        final String bankNumber = commissionVend.getBankNumber();
                        final String note = commissionVend.getNote();
                        final String receiptNumber = commissionVend.getReceiptNumber();
                        final String refNo = commissionVend.getRefNo();
                        final Long accountingRuleId = commissionVend.getAccountingRules();
                        final String currencyCode = commissionVend.getCurrencyCode();
                        final Long paymentTypeId = commissionVend.getPaymentTypeId();
                        final AccountingRuleData accountingRuleData = this.accountingRuleReadPlatformService.retrieveAccountingRuleById(accountingRuleId);
                        final Long officeId = accountingRuleData.getOfficeId();

                        JsonArray credits = new JsonArray();
                        final List<GLAccountDataForLookup> accountDataForLookupsCredits = accountingRuleData.getCreditAccounts();
                        if (accountDataForLookupsCredits != null) {
                            for (GLAccountDataForLookup accountDataForLookupsCredit : accountDataForLookupsCredits) {
                                final JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("glAccountId", accountDataForLookupsCredit.getId());
                                jsonObject.addProperty(SavingsApiConstants.amountParamName, commissionAmount);
                                credits.add(jsonObject);
                            }
                        }
                        JsonArray debits = new JsonArray();
                        final List<GLAccountDataForLookup> accountDataForLookupsDebits = accountingRuleData.getDebitAccounts();
                        if (accountDataForLookupsDebits != null) {
                            for (GLAccountDataForLookup accountDataForLookupsDebit : accountDataForLookupsDebits) {
                                final JsonObject jsonObject = new JsonObject();
                                jsonObject.addProperty("glAccountId", accountDataForLookupsDebit.getId());
                                jsonObject.addProperty(SavingsApiConstants.amountParamName, commissionAmount);
                                debits.add(jsonObject);
                            }
                        }
//                        {
//    "locale": "en",
//    "dateFormat": "dd MMMM yyyy",
//    "officeId": 1,
//    "transactionDate": "14 May 2024",
//    "referenceNumber": "6678",
//    "comments": "Checks",
//    "accountingRule": 1,
//    "currencyCode": "EUR",
//    "paymentTypeId": 1,
//    "accountNumber": "1234456778",
//    "checkNumber": "11",
//    "routingCode": "11",
//    "receiptNumber": "11",
//    "bankNumber": "Sterling Bank",
//    "credits": [
//        {
//            "glAccountId": 27,
//            "amount": "10"
//        }
//    ],
//    "debits": [
//        {
//            "glAccountId": 17,
//            "amount": "10"
//        }
//    ]
//}

                        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
                        final JsonObject accountEntryJson = new JsonObject();
                        accountEntryJson.addProperty(SavingsApiConstants.transactionDateParamName, today.toString());
                        accountEntryJson.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
                        accountEntryJson.addProperty(SavingsApiConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);
                        accountEntryJson.addProperty(ClientApiConstants.officeIdParamName, officeId);
                        accountEntryJson.addProperty("referenceNumber", refNo);
                        accountEntryJson.addProperty("comments", note);
                        accountEntryJson.addProperty("accountingRule", accountingRuleId);
                        accountEntryJson.addProperty("currencyCode", currencyCode);
                        accountEntryJson.addProperty(SavingsApiConstants.paymentTypeIdParamName, paymentTypeId);
                        accountEntryJson.addProperty(SavingsApiConstants.receiptNumberParamName, receiptNumber);
                        accountEntryJson.addProperty(SavingsApiConstants.bankNumberParamName, bankNumber);
                        accountEntryJson.add("credits", credits);
                        accountEntryJson.add("debits", debits);
                        final String apiRequestBodyAsJson = accountEntryJson.toString();
                        log.info("commissionVendEod Posting- {}", apiRequestBodyAsJson);
                        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
                        final CommandWrapper commandRequest = builder.createJournalEntry().build();
                        commandsSourceWritePlatformService.logCommandSource(commandRequest);
                    }

                    String updateCommissionVatCalculated = "INSERT INTO m_commision_vat_calculated (savings_account_transaction_id, type, status) VALUES (?, ?, ?)";
                    jdbcTemplate.update(updateCommissionVatCalculated, transactionId, 1, status);

                }
            }
        } else {
            log.info("Commission calc disAbled");
        }
    }

    @Override
    public Page<SavingsAccountTransactionData> retrieveAllTransactionsBySavingsId(Long savingsId, DepositAccountType depositAccountType,
            final SearchParametersBusiness searchParameters) {

        List<Object> paramList = new ArrayList<>(Arrays.asList(savingsId, depositAccountType));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ").append(sqlGenerator.calcFoundRows()).append(" ");
        sqlBuilder.append(this.transactionsMapper.schema());
        sqlBuilder.append(" where (tr.savings_account_id = ? or tr.transaction_type_enum = ?) ");

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromTransactionCriteria(this.transactionsMapper.schema(), searchParameters,
                    paramList);

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
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.transactionsMapper);
    }

    private String buildSqlStringFromTransactionCriteria(String schemaSql, final SearchParametersBusiness searchParameters,
            List<Object> paramList) {
        String extraCriteria = "";

        final Long transactionTypeId = searchParameters.getTransactionTypeId();
        final Long transactionId = searchParameters.getTransactionId();

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(tr.transaction_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(tr.transaction_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(tr.transaction_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (transactionTypeId != null) {
            paramList.add(transactionTypeId);
            extraCriteria += " and tr.transaction_type_enum = ? ";
        }

        if (transactionId != null) {
            paramList.add(transactionId);
            extraCriteria += " and tr.id = ? ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class SavingsAccountTransactionsMapper implements RowMapper<SavingsAccountTransactionData> {

        private final String schemaSql;

        SavingsAccountTransactionsMapper() {

            final StringBuilder sqlBuilder = new StringBuilder(400);
            sqlBuilder.append("tr.id as transactionId, tr.transaction_type_enum as transactionType, ");
            sqlBuilder.append("tr.transaction_date as transactionDate, tr.amount as transactionAmount,");
            sqlBuilder.append(" tr.release_id_of_hold_amount as releaseTransactionId,");
            sqlBuilder.append(" tr.reason_for_block as reasonForBlock, tr.ref_no as refNo, ");
            sqlBuilder.append("tr.created_date as submittedOnDate,");
            sqlBuilder.append(" au.username as submittedByUsername, ");
            sqlBuilder.append(" nt.note as transactionNote, ");
            sqlBuilder.append("tr.running_balance_derived as runningBalance, tr.is_reversed as reversed,");
            sqlBuilder.append(
                    "tr.is_reversal as isReversal, tr.original_transaction_id as originalTransactionId, tr.is_lien_transaction as lienTransaction, ");
            sqlBuilder.append("fromtran.id as fromTransferId, fromtran.is_reversed as fromTransferReversed,");
            sqlBuilder.append("fromtran.transaction_date as fromTransferDate, fromtran.amount as fromTransferAmount,");
            sqlBuilder.append("fromtran.description as fromTransferDescription,");
            sqlBuilder.append("totran.id as toTransferId, totran.is_reversed as toTransferReversed,");
            sqlBuilder.append("totran.transaction_date as toTransferDate, totran.amount as toTransferAmount,");
            sqlBuilder.append("totran.description as toTransferDescription,");
            sqlBuilder.append("sa.id as savingsId, sa.account_no as accountNo,");
            sqlBuilder.append("pd.payment_type_id as paymentType,pd.account_number as accountNumber,pd.check_number as checkNumber, ");
            sqlBuilder.append("pd.receipt_number as receiptNumber, pd.bank_number as bankNumber,pd.routing_code as routingCode, ");
            sqlBuilder.append(
                    "sa.currency_code as currencyCode, sa.currency_digits as currencyDigits, sa.currency_multiplesof as inMultiplesOf, ");

            sqlBuilder.append(
                    "msac.charge_id as chargeId, mc.name as chargeName, ");

            sqlBuilder.append("curr.name as currencyName, curr.internationalized_name_code as currencyNameCode, ");
            sqlBuilder.append("curr.display_symbol as currencyDisplaySymbol, ");
            sqlBuilder.append("pt.value as paymentTypeName, ");
            sqlBuilder.append("tr.is_manual as postInterestAsOn ");
            sqlBuilder.append("from m_savings_account sa ");
            sqlBuilder.append("join m_savings_account_transaction tr on tr.savings_account_id = sa.id ");
            sqlBuilder.append("join m_currency curr on curr.code = sa.currency_code ");
            sqlBuilder.append("left join m_account_transfer_transaction fromtran on fromtran.from_savings_transaction_id = tr.id ");
            sqlBuilder.append("left join m_account_transfer_transaction totran on totran.to_savings_transaction_id = tr.id ");
            sqlBuilder.append("left join m_payment_detail pd on tr.payment_detail_id = pd.id ");
            sqlBuilder.append("left join m_payment_type pt on pd.payment_type_id = pt.id ");
            sqlBuilder.append(" left join m_appuser au on au.id=tr.appuser_id ");
            sqlBuilder.append(" left join m_note nt ON nt.savings_account_transaction_id=tr.id ");
            sqlBuilder.append("left join m_savings_account_charge_paid_by msacpb on msacpb.savings_account_transaction_id = tr.id ");
            sqlBuilder.append("left join m_savings_account_charge msac on msac.id = msacpb.savings_account_charge_id ");
            sqlBuilder.append(" left join m_charge mc ON mc.id=msac.charge_id ");
            this.schemaSql = sqlBuilder.toString();
        }

        public String schema() {
            return this.schemaSql;
        }

        @Override
        public SavingsAccountTransactionData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("transactionId");
            final int transactionTypeInt = JdbcSupport.getInteger(rs, "transactionType");
            final SavingsAccountTransactionEnumData transactionType = SavingsEnumerations.transactionType(transactionTypeInt);

            final LocalDate date = JdbcSupport.getLocalDate(rs, "transactionDate");
            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final LocalDateTime submittedOnDateTime = JdbcSupport.getLocalDateTime(rs, "submittedOnDate");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "transactionAmount");
            final Long releaseTransactionId = rs.getLong("releaseTransactionId");
            final String reasonForBlock = rs.getString("reasonForBlock");
            final BigDecimal outstandingChargeAmount = null;
            final BigDecimal runningBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "runningBalance");
            final boolean reversed = rs.getBoolean("reversed");
            final boolean isReversal = rs.getBoolean("isReversal");
            final Long originalTransactionId = rs.getLong("originalTransactionId");
            final Boolean lienTransaction = rs.getBoolean("lienTransaction");

            final Long savingsId = rs.getLong("savingsId");
            final String accountNo = rs.getString("accountNo");
            final boolean postInterestAsOn = rs.getBoolean("postInterestAsOn");

            PaymentDetailData paymentDetailData = null;
            if (transactionType.isDepositOrWithdrawal()) {
                final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentType");
                if (paymentTypeId != null) {
                    final String typeName = rs.getString("paymentTypeName");
                    final PaymentTypeData paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
                    final String accountNumber = rs.getString("accountNumber");
                    final String checkNumber = rs.getString("checkNumber");
                    final String routingCode = rs.getString("routingCode");
                    final String receiptNumber = rs.getString("receiptNumber");
                    final String bankNumber = rs.getString("bankNumber");
                    paymentDetailData = new PaymentDetailData(id, paymentType, accountNumber, checkNumber, routingCode, receiptNumber,
                            bankNumber);
                }
            }

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final Integer currencyDigits = JdbcSupport.getInteger(rs, "currencyDigits");
            final Integer inMultiplesOf = JdbcSupport.getInteger(rs, "inMultiplesOf");
            final CurrencyData currency = new CurrencyData(currencyCode, currencyName, currencyDigits, inMultiplesOf, currencyDisplaySymbol,
                    currencyNameCode);

            AccountTransferData transfer = null;
            final Long fromTransferId = JdbcSupport.getLong(rs, "fromTransferId");
            final Long toTransferId = JdbcSupport.getLong(rs, "toTransferId");
            if (fromTransferId != null) {
                final LocalDate fromTransferDate = JdbcSupport.getLocalDate(rs, "fromTransferDate");
                final BigDecimal fromTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "fromTransferAmount");
                final boolean fromTransferReversed = rs.getBoolean("fromTransferReversed");
                final String fromTransferDescription = rs.getString("fromTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(fromTransferId, currency, fromTransferAmount, fromTransferDate,
                        fromTransferDescription, fromTransferReversed);
            } else if (toTransferId != null) {
                final LocalDate toTransferDate = JdbcSupport.getLocalDate(rs, "toTransferDate");
                final BigDecimal toTransferAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "toTransferAmount");
                final boolean toTransferReversed = rs.getBoolean("toTransferReversed");
                final String toTransferDescription = rs.getString("toTransferDescription");

                transfer = AccountTransferData.transferBasicDetails(toTransferId, currency, toTransferAmount, toTransferDate,
                        toTransferDescription, toTransferReversed);
            }
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String note = rs.getString("transactionNote");
            final String refNo = rs.getString("refNo");

            final long chargeId = rs.getLong("chargeId");
            ChargeData chargeData = null;
            if (chargeId > 0) {
                final String chargeName = rs.getString("chargeName");
                chargeData = ChargeData.lookup(chargeId, chargeName, false);
            }

            final SavingsAccountTransactionData savingsAccountTransactionData = SavingsAccountTransactionData.create(id, transactionType, paymentDetailData, savingsId, accountNo, date, currency,
                    amount, outstandingChargeAmount, runningBalance, reversed, transfer, submittedOnDate, postInterestAsOn,
                    submittedByUsername, note, isReversal, originalTransactionId, lienTransaction, releaseTransactionId, reasonForBlock);
            savingsAccountTransactionData.setChargeData(chargeData);
            savingsAccountTransactionData.setSubmittedOnDateTime(submittedOnDateTime);
            savingsAccountTransactionData.setRefNo(refNo);
            return savingsAccountTransactionData;
        }
    }

}
