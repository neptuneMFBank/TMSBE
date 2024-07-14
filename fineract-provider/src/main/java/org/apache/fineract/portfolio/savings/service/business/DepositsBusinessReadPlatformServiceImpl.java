/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.service.business;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;
import org.apache.fineract.portfolio.savings.data.business.DepositAccountBusinessData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.exception.DepositAccountNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountTransactionNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    private final SavingsAmountOnHoldDataMapper savingsAmountOnHoldDataMapper = new SavingsAmountOnHoldDataMapper();
    private final ReconciliationWalletSummaryMapper reconciliationWalletSummaryMapper = new ReconciliationWalletSummaryMapper();
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    private final ColumnValidator columnValidator;

    @Override
    @CronTarget(jobName = JobName.CREATE_RECONCILIATION_WALLET)
    public void createReconciliationWalletMissingForClient() {
        // create reconciliation wallet missing and update it as client default savings account
        log.info("createReconciliationWalletMissingForClient start");

        final String sql = "select " + this.reconciliationWalletSummaryMapper.schema();
        Collection<JsonObject> reconciliationWalletSummaryJson = this.jdbcTemplate.query(sql, this.reconciliationWalletSummaryMapper);
        if (!CollectionUtils.isEmpty(reconciliationWalletSummaryJson)) {
            for (JsonElement element : reconciliationWalletSummaryJson) {
                if (element.isJsonObject()) {
                    final JsonObject jsonObject = element.getAsJsonObject();
                    final Long id = jsonObject.get(ClientApiConstants.idParamName).getAsLong();
                    log.info("createReconciliationWalletMissingForClient clientId: {}", id);
                    final Long productId = jsonObject.get(SavingsApiConstants.productIdParamName).getAsLong();

                    final JsonObject createReconciliationSavings = new JsonObject();

                    createReconciliationSavings.addProperty("productId", productId);
                    createReconciliationSavings.addProperty("clientId", id);
                    createReconciliationSavings.addProperty("nominalAnnualInterestRate", 0);
                    createReconciliationSavings.addProperty("withdrawalFeeForTransfers", false);
                    createReconciliationSavings.addProperty("allowOverdraft", true);
                    createReconciliationSavings.addProperty("overdraftLimit", 5000000);
                    createReconciliationSavings.addProperty("enforceMinRequiredBalance", false);
                    createReconciliationSavings.addProperty("withHoldTax", false);
                    createReconciliationSavings.addProperty("interestCompoundingPeriodType", 1);
                    createReconciliationSavings.addProperty("interestPostingPeriodType", 4);
                    createReconciliationSavings.addProperty("interestCalculationType", 1);
                    createReconciliationSavings.addProperty("interestCalculationDaysInYearType", 365);

                    final LocalDate clientActivationLocalDate = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
                    createReconciliationSavings.addProperty("submittedOnDate", clientActivationLocalDate.toString());
                    createReconciliationSavings.addProperty("locale", GeneralConstants.LOCALE_EN_DEFAULT);
                    createReconciliationSavings.addProperty("dateFormat", GeneralConstants.DATEFORMET_DEFAULT);
                    createReconciliationSavings.addProperty("monthDayFormat", GeneralConstants.DATEFORMAT_MONTHDAY_DEFAULT);

                    final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsAccount()
                            .withJson(createReconciliationSavings.toString()).build();
                    final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
                    final Long resultReconciliationSavings = result.resourceId();

                    // Approve Reconciliation Wallet
                    final JsonObject approveReconciliationSavings = new JsonObject();
                    approveReconciliationSavings.addProperty("approvedOnDate", clientActivationLocalDate.toString());
                    approveReconciliationSavings.addProperty("note", "System Approved");
                    approveReconciliationSavings.addProperty("locale", GeneralConstants.LOCALE_EN_DEFAULT);
                    approveReconciliationSavings.addProperty("dateFormat", GeneralConstants.DATEFORMET_DEFAULT);
                    final CommandWrapper commandRequestApprove = new CommandWrapperBuilder()
                            .approveSavingsAccountApplication(resultReconciliationSavings).withJson(approveReconciliationSavings.toString())
                            .build();
                    this.commandsSourceWritePlatformService.logCommandSource(commandRequestApprove);

                    final JsonObject activateReconciliationSavings = new JsonObject();
                    activateReconciliationSavings.addProperty("activatedOnDate", clientActivationLocalDate.toString());
                    activateReconciliationSavings.addProperty("locale", GeneralConstants.LOCALE_EN_DEFAULT);
                    activateReconciliationSavings.addProperty("dateFormat", GeneralConstants.DATEFORMET_DEFAULT);
                    final CommandWrapper commandRequestActivate = new CommandWrapperBuilder()
                            .savingsAccountActivation(resultReconciliationSavings).withJson(activateReconciliationSavings.toString())
                            .build();
                    this.commandsSourceWritePlatformService.logCommandSource(commandRequestActivate);

                    // update client to have this account as default savings account
                    final JsonObject updateSavingsAccount = new JsonObject();
                    updateSavingsAccount.addProperty("savingsAccountId", resultReconciliationSavings);

                    final CommandWrapper commandRequestUpdateAccount = new CommandWrapperBuilder().updateClientSavingsAccount(id)
                            .withJson(updateSavingsAccount.toString()).build();
                    this.commandsSourceWritePlatformService.logCommandSource(commandRequestUpdateAccount);
                }
            }
        }
        log.info("createReconciliationWalletMissingForClient end");
    }

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
        final Integer depositTypeId = searchParameters.getDepositTypeId();
        final Integer statusId = searchParameters.getStatusId();
        final Long officeId = searchParameters.getOfficeId();
        final String externalId = searchParameters.getExternalId();
        final String accountNo = searchParameters.getAccountNo();
        final String displayName = searchParameters.getName();
        final Boolean accountWithBalance = searchParameters.isOrphansOnly();
        final Long excludeProductId = searchParameters.getCategoryId();

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

        if (searchParameters.isOrphansOnlyPassed() && BooleanUtils.isTrue(accountWithBalance)) {
            extraCriteria += " AND ms.available_balance > 0 ";
        }

        if (searchParameters.isClientIdPassed()) {
            paramList.add(clientId);
            extraCriteria += " and ms.client_id = ? ";
        }
        if (searchParameters.isProductIdPassed()) {
            paramList.add(productId);
            extraCriteria += " and ms.product_id = ? ";
        }
        if (searchParameters.isCategoryIdPassed()) {
            paramList.add(excludeProductId);
            extraCriteria += " and ms.product_id <> ? ";
        }
        if (searchParameters.isDepositTypeIdPassed()) {
            paramList.add(depositTypeId);
            extraCriteria += " and ms.deposit_type_enum = ? ";
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%" + displayName + "%");
            extraCriteria += " and ms.display_name like ? ";
        }

        if (searchParameters.isExternalIdPassed()) {
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

            final DepositAccountBusinessData depositAccountBusinessData = this.jdbcTemplate.queryForObject(sql, this.depositViewMapper,
                    accountNo);
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

            final DepositAccountBusinessData depositAccountBusinessData = this.jdbcTemplate.queryForObject(sql, this.depositViewMapper,
                    accountNo);
            final DepositAccountBusinessData accountBusinessData = DepositAccountBusinessData.retrieveName(depositAccountBusinessData);
            return accountBusinessData;

        } catch (DataAccessException e) {
            log.warn("retrieveName: {}", e);
            throw new DepositAccountNotFoundException("Deposit account with account " + accountNo + " does not exist");
        }
    }

    @Override
    public Page<JsonObject> retrieveAllSavingsAmountOnHold(SearchParametersBusiness searchParameters) {

        final String userOfficeHierarchy = this.context.officeHierarchy();
        final String underHierarchySearchString = userOfficeHierarchy + "%";

        List<Object> paramList = new ArrayList<>(Arrays.asList(underHierarchySearchString));
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.savingsAmountOnHoldDataMapper.schema());

        sqlBuilder.append(" where sav.hierarchy like ? ");

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromSavingsAmountOnHoldCriteria(searchParameters, paramList);

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
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(),
                this.savingsAmountOnHoldDataMapper);
    }

    private String buildSqlStringFromSavingsAmountOnHoldCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        final Long productId = searchParameters.getProductId();
        final Long clientId = searchParameters.getClientId();
        final Long officeId = searchParameters.getOfficeId();
        final String accountNo = searchParameters.getAccountNo();
        final String displayName = searchParameters.getName();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(sav.created_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(sav.created_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(sav.created_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isClientIdPassed()) {
            paramList.add(clientId);
            extraCriteria += " and sav.client_id = ? ";
        }
        if (searchParameters.isProductIdPassed()) {
            paramList.add(productId);
            extraCriteria += " and sav.product_id = ? ";
        }
        if (searchParameters.isNamePassed()) {
            paramList.add("%" + displayName + "%");
            extraCriteria += " and sav.display_name like ? ";
        }

        if (searchParameters.isAccountNoPassed()) {
            paramList.add(accountNo);
            extraCriteria += " and sav.account_no = ? ";
        }
        if (searchParameters.isOfficeIdPassed()) {
            extraCriteria += " and sav.office_id = ? ";
            paramList.add(officeId);
        }
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public JsonObject retrieveSavingsAmountOnHold(Long savingsAmountOnHoldId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + savingsAmountOnHoldDataMapper.schema() + " where sav.id = ?";
            return this.jdbcTemplate.queryForObject(sql, savingsAmountOnHoldDataMapper, new Object[]{savingsAmountOnHoldId});
        } catch (DataAccessException e) {
            log.error("SavingsAmountOnHold not found: {}", e);
            throw new SavingsAccountTransactionNotFoundException(savingsAmountOnHoldId, savingsAmountOnHoldId);
        }
    }

    @Override
    public void approveActivateSavings(final Long savingsId) {
        final AppUser appUser = this.context.authenticatedUser();
        final Long appUserId = appUser.getId();
        try {
            String clientUpdateSql = "UPDATE m_savings_account SET status_enum=?, approvedon_date=CURRENT_TIMESTAMP, approvedon_userid=?, activatedon_date=CURRENT_TIMESTAMP, activatedon_userid=? WHERE id=?";
            jdbcTemplate.update(clientUpdateSql, SavingsAccountStatusType.ACTIVE.getValue(), appUserId, appUserId, savingsId);
        } catch (DataAccessException e) {
            log.error("approveActivateSavings: {}", e);
        }
    }

    private static final class SavingsAmountOnHoldDataMapper implements RowMapper<JsonObject> {

        public String schema() {
            final StringBuilder accountsSummary = new StringBuilder(
                    " sav.id, sav.savings_account_id, sav.amount, sav.account_no, sav.product_id, sav.product_name, sav.client_id, "
                            + " sav.display_name, sav.office_id, sav.office_name, sav.mobile_no, sav.email_address, sav.bvn, sav.nin, "
                            + " sav.tin, sav.alternateMobileNumber, sav.appuser_id, sav.originator, sav.reason_for_block, "
                            + " sav.created_date from m_savings_amount_on_hold_view sav ");

            return accountsSummary.toString();
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long savingsId = rs.getLong("savings_account_id");
            final BigDecimal amount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "amount");
            final String accountNo = rs.getString("account_no");
            final Long productId = rs.getLong("product_id");
            final String productName = rs.getString("product_name");
            final Long clientId = rs.getLong("client_id");
            final String displayName = rs.getString("display_name");
            final String officeName = rs.getString("office_name");
            final String mobileNo = rs.getString("mobile_no");
            final String emailAddress = rs.getString("email_address");
            final String reasonForBlock = rs.getString("reason_for_block");
            final String bvn = rs.getString("bvn");
            final String nin = rs.getString("nin");
            final String tin = rs.getString("tin");
            final String alternateMobileNumber = rs.getString("alternateMobileNumber");
            final Long appuserId = rs.getLong("appuser_id");
            final String originator = rs.getString("originator");
            final LocalDateTime createdDateTime = JdbcSupport.getLocalDateTime(rs, "created_date");

            final JsonObject savingsAmountOnHold = new JsonObject();
            savingsAmountOnHold.addProperty("id", id);
            savingsAmountOnHold.addProperty("savingsId", savingsId);
            savingsAmountOnHold.addProperty("amount", amount);
            savingsAmountOnHold.addProperty("accountNo", accountNo);
            savingsAmountOnHold.addProperty("productId", productId);
            savingsAmountOnHold.addProperty("productName", productName);
            savingsAmountOnHold.addProperty("clientId", clientId);
            savingsAmountOnHold.addProperty("displayName", displayName);
            savingsAmountOnHold.addProperty("officeName", officeName);
            savingsAmountOnHold.addProperty("mobileNo", mobileNo);
            savingsAmountOnHold.addProperty("emailAddress", emailAddress);
            savingsAmountOnHold.addProperty("bvn", bvn);
            savingsAmountOnHold.addProperty("nin", nin);
            savingsAmountOnHold.addProperty("tin", tin);
            savingsAmountOnHold.addProperty("alternateMobileNumber", alternateMobileNumber);
            savingsAmountOnHold.addProperty("appuserId", appuserId);
            savingsAmountOnHold.addProperty("originator", originator);
            savingsAmountOnHold.addProperty("createdDateTime", createdDateTime.toString());
            savingsAmountOnHold.addProperty("reasonForBlock", reasonForBlock);

            return savingsAmountOnHold;
        }
    }

    private static final class DepositViewMapper implements RowMapper<DepositAccountBusinessData> {

        private final String schema;

        DepositViewMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(
                    " ms.office_id officeId, ms.office_name officeName, ms.id, ms.external_id externalId, ms.account_no accountNo, ms.product_id productId, ms.product_name productName, ms.deposit_type_enum depositType, ");
            sqlBuilder.append(
                    " ms.client_id clientId, ms.display_name displayName, ms.ledger_balance ledgerBalance, ms.available_balance availableBalance, ms.min_required_balance minRequiredBalance, ");
            sqlBuilder.append(
                    " ms.submittedon_date createdOn, ms.activatedon_date activatedOn, ms.last_transaction_date lastTransactionOn, ms.status_enum as statusEnum ");
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
            final BigDecimal minRequiredBalance = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minRequiredBalance");
            final BigDecimal ledgerBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "ledgerBalance");
            final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOn");
            final LocalDate activatedOn = JdbcSupport.getLocalDate(rs, "activatedOn");
            final LocalDate lastTransactionOn = JdbcSupport.getLocalDate(rs, "lastTransactionOn");

            return DepositAccountBusinessData.lookUp(id, accountNo, depositType, status, clientId, clientName, productId, productName,
                    availableBalance, ledgerBalance, createdOn, activatedOn, lastTransactionOn, externalId, officeId, officeName, minRequiredBalance);

        }

    }

    public static final class ReconciliationWalletSummaryMapper implements RowMapper<JsonObject> {

        public String schema() {
            return " cwrv.id, cwrv.using_savings_product_id usingSavingsProductId FROM client_without_reconciliation_view cwrv ";
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long usingSavingsProductId = rs.getLong("usingSavingsProductId");

            final JsonObject reconciliationWallet = new JsonObject();
            reconciliationWallet.addProperty(ClientApiConstants.idParamName, id);
            reconciliationWallet.addProperty(SavingsApiConstants.productIdParamName, usingSavingsProductId);
            return reconciliationWallet;
        }
    }

}
