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
package org.apache.fineract.portfolio.loanaccount.service.business;

import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.CLIENT_DEFAULT_ID_API;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.business.LoanBusinessAccountData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LoanArrearsBusinessReadPlatformServiceImpl implements LoanArrearsBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final LoanDropdownReadPlatformService loanDropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final FloatingRatesReadPlatformService floatingRatesReadPlatformService;
    private final LoanUtilService loanUtilService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final Long clientDefaultId;
    private final LoansApiResource loansApiResource;
    private final FromJsonHelper fromJsonHelper;
    private final LoanMapper loanLoanMapper;
    private final LoanArrearsSummaryMapper loanArrearsSummaryMapper;

    public static String loanProductIdParameterName = "loanProductId";
    public static String loanProductNameParameterName = "loanProductName";
    public static String currencyDisplaySymbolParameterName = "currencyDisplaySymbol";
    public static String totalRepaymentParameterName = "totalRepayment";
    public static String totalOverdueParameterName = "totalOverdue";
    public static String totalPrincipalParameterName = "totalPrincipal";
    public static String totalLoanBalanceParameterName = "totalLoanBalance";
    public static String totalLoanCountParameterName = "totalLoanCount";
    public static String statusParameterName = "status";

    @Autowired
    public LoanArrearsBusinessReadPlatformServiceImpl(final PlatformSecurityContext context,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,
            final LoanProductReadPlatformService loanProductReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final GroupReadPlatformService groupReadPlatformService, final LoanDropdownReadPlatformService loanDropdownReadPlatformService,
            final FundReadPlatformService fundReadPlatformService, final ChargeReadPlatformService chargeReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final JdbcTemplate jdbcTemplate,
            final NamedParameterJdbcTemplate namedParameterJdbcTemplate, final CalendarReadPlatformService calendarReadPlatformService,
            final StaffReadPlatformService staffReadPlatformService, final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final FloatingRatesReadPlatformService floatingRatesReadPlatformService, final LoanUtilService loanUtilService,
            final ConfigurationDomainService configurationDomainService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService, final LoanRepositoryWrapper loanRepositoryWrapper,
            final ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper,
            final LoanArrearsSummaryMapper loanArrearsSummaryMapper, final ApplicationContext applicationContext,
            final LoansApiResource loansApiResource, final FromJsonHelper fromJsonHelper) {
        this.context = context;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.loanDropdownReadPlatformService = loanDropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.staffReadPlatformService = staffReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.floatingRatesReadPlatformService = floatingRatesReadPlatformService;
        this.loanUtilService = loanUtilService;
        this.configurationDomainService = configurationDomainService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
        Environment environment = applicationContext.getEnvironment();
        this.clientDefaultId = Long.valueOf(environment.getProperty(CLIENT_DEFAULT_ID_API));
        this.loansApiResource = loansApiResource;
        this.fromJsonHelper = fromJsonHelper;
        this.loanLoanMapper = new LoanMapper(sqlGenerator);
        this.loanArrearsSummaryMapper = new LoanArrearsSummaryMapper(sqlGenerator);
    }

    @Override
    public JsonObject retrieveLoanArrearsSummary(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        final JsonObject jsonObjectArrearsSummary = new JsonObject();

        JsonElement jsonElement = null;
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.loanArrearsSummaryMapper.schema());

            List<Object> paramListSummary = new ArrayList<>();
            if (searchParameters != null) {
                final String extraCriteria = buildSqlStringFromLoanArrearsSummaryCriteria(searchParameters, paramListSummary);
                if (StringUtils.isNotBlank(extraCriteria)) {
                    sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
                }
            }
            sqlBuilder.append(" GROUP BY mla.product_id ");
            String sql = sqlBuilder.toString();

            log.info("jsonObjectLoanArrearsSummaryInfo SQL: {}", sql);
            log.info("jsonObjectLoanArrearsSummaryInfo PARAM: {}", Arrays.toString(paramListSummary.toArray()));
            final Collection<JsonObject> jsonObjectLoanArrearsSummaryInfo = this.jdbcTemplate.query(sql, this.loanArrearsSummaryMapper, paramListSummary.toArray());

            if (!CollectionUtils.isEmpty(jsonObjectLoanArrearsSummaryInfo)) {
                final String jsonElementString = this.fromJsonHelper.toJson(jsonObjectLoanArrearsSummaryInfo);
                jsonElement = this.fromJsonHelper.parse(jsonElementString);
            }
            jsonObjectArrearsSummary.add("summaryInfo", jsonElement);
        } catch (DataAccessException e) {
            log.warn("jsonObjectLoanArrearsSummaryInfo Error: {}", e);
            jsonObjectArrearsSummary.add("summaryInfo", jsonElement);
        }

        JsonObject jsonObjectLoanArrearsSummary = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.loanArrearsSummaryMapper.schema());

            List<Object> paramListSummary = new ArrayList<>();
            if (searchParameters != null) {
                final String extraCriteria = buildSqlStringFromLoanArrearsSummaryCriteria(searchParameters, paramListSummary);
                if (StringUtils.isNotBlank(extraCriteria)) {
                    sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
                }
            }
            String sql = sqlBuilder.toString();
            log.info("jsonObjectLoanArrearsSummary SQL: {}", sql);
            log.info("jsonObjectLoanArrearsSummary PARAM: {}", Arrays.toString(paramListSummary.toArray()));
            jsonObjectLoanArrearsSummary = this.jdbcTemplate.queryForObject(sql, this.loanArrearsSummaryMapper, paramListSummary.toArray());
            jsonObjectArrearsSummary.add("summary", jsonObjectLoanArrearsSummary);
        } catch (DataAccessException e) {
            log.warn("jsonObjectLoanArrearsSummary Error: {}", e);
            jsonObjectLoanArrearsSummary.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectArrearsSummary.add("summary", jsonObjectLoanArrearsSummary);
        }

        return jsonObjectArrearsSummary;
    }

    @Override
    public Page<LoanBusinessAccountData> retrieveAll(final SearchParametersBusiness searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
        // sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.loanLoanMapper.loanSchema());

        // TODO - for time being this will data scope list of loans returned to
        // only loans that have a client associated.
        // to support senario where loan has group_id only OR client_id will
        // probably require a UNION query
        // but that at present is an edge case
        sqlBuilder.append(" join m_office o on (o.id = mla.client_office_id or o.id = mla.group_office_id) ");
        sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = mla.transfer_to_office_id ");
        sqlBuilder.append(" where ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

        int arrayPos = 2;
        List<Object> extraCriterias = new ArrayList<>();
        extraCriterias.add(hierarchySearchString);
        extraCriterias.add(hierarchySearchString);

        if (searchParameters != null) {

            if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
                final LocalDate startPeriod = searchParameters.getFromDate();
                final LocalDate endPeriod = searchParameters.getToDate();
                // final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
                if (startPeriod != null && endPeriod != null) {
                    sqlBuilder.append(" and CAST(mla.overdue_since_date_derived AS DATE) BETWEEN ? AND ? ");
                    extraCriterias.add(df.format(startPeriod));
                    arrayPos = arrayPos + 1;
                    extraCriterias.add(df.format(endPeriod));
                    arrayPos = arrayPos + 1;
                } else if (startPeriod != null) {
                    sqlBuilder.append(" and CAST(mla.overdue_since_date_derived AS DATE) >= ? ");
                    extraCriterias.add(df.format(startPeriod));
                    arrayPos = arrayPos + 1;
                } else if (endPeriod != null) {
                    sqlBuilder.append(" and CAST(mla.overdue_since_date_derived AS DATE) <= ? ");
                    extraCriterias.add(df.format(endPeriod));
                    arrayPos = arrayPos + 1;
                }
            }

            if (searchParameters.isProductIdPassed()) {
                sqlBuilder.append("and mla.product_id =?");
                extraCriterias.add(searchParameters.getProductId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.isStaffIdPassed()) {
                sqlBuilder.append("and mla.loan_officer_id =?");
                extraCriterias.add(searchParameters.getStaffId());
                arrayPos = arrayPos + 1;
            }

            if (searchParameters.isOfficeIdPassed()) {
                sqlBuilder.append("and mla.office_id =?");
                extraCriterias.add(searchParameters.getOfficeId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.isClientIdPassed()) {
                sqlBuilder.append("and mla.client_id =?");
                extraCriterias.add(searchParameters.getClientId());
                arrayPos = arrayPos + 1;
            }

            if (searchParameters.isAccountNoPassed()) {
                sqlBuilder.append(" and mla.account_no = ?");
                extraCriterias.add(searchParameters.getAccountNo());
                arrayPos = arrayPos + 1;
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
        final Object[] objectArray = extraCriterias.toArray();
        final Object[] finalObjectArray = Arrays.copyOf(objectArray, arrayPos);
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), finalObjectArray, this.loanLoanMapper);
    }

    private static final class LoanMapper implements RowMapper<LoanBusinessAccountData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String loanSchema() {
            return " mla.id, mla.is_topup isTopup, mla.account_no accountNo, mla.product_id loanProductId, mla.product_name loanProductName, "
                    + " mla.loan_officer_id loanOfficerId, mla.loan_officer_name loanOfficerName, mla.number_of_repayments numberOfRepayments, mla.total_recovered_derived totalRecovered, "
                    + " mla.term_frequency termFrequency, mla.term_period_frequency_enum termPeriodFrequencyType, mla.total_outstanding_derived totalOutstanding, mla.total_repayment_derived totalRepayment, mla.principal_amount principal,"
                    + " mla.principal_overdue_derived principalOverdue, mla.interest_overdue_derived interestOverdue, mla.fee_charges_overdue_derived feeChargesOverdue, mla.expected_maturedon_date expectedMaturityDate,"
                    + " mla.penalty_charges_overdue_derived penaltyChargesOverdue, mla.total_overdue_derived totalOverdue, mla.overdue_since_date_derived overdueSinceDate, mla.total_expected_repayment_derived totalExpectedRepayment,"
                    + " mla.client_id clientId, mla.client_display_name clientName, mla.mobile_no mobileNo, mla.email_address emailAddress, mla.bvn, mla.nin, mla.alternateMobileNumber,"
                    + " mla.group_id groupId, mla.group_display_name groupName, mla.nominal_interest_rate_per_period interestRatePerPeriod, mla.annual_nominal_interest_rate annualInterestRate, "
                    + " mla.currency_code currencyCode, rc." + sqlGenerator.escape("name")
                    + " as currencyName, rc.display_symbol currencyDisplaySymbol, rc.internationalized_name_code currencyNameCode, "
                    + " mla.submittedon_date submittedOnDate, mla.disbursedon_date actualDisbursementDate, mla.client_office_id, mla.group_office_id, mla.transfer_to_office_id "
                    + " FROM m_loan_arrears_view mla " + " join m_currency rc on rc." + sqlGenerator.escape("code")
                    + " = mla.currency_code";
        }

        @Override
        public LoanBusinessAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, 0, null, currencyDisplaySymbol,
                    currencyNameCode);

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");

            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");
            final String mobileNo = rs.getString("mobileNo");
            final String emailAddress = rs.getString("emailAddress");
            final String bvn = rs.getString("bvn");
            final String alternateMobileNumber = rs.getString("alternateMobileNumber");
            final JsonObject clientData = new JsonObject();
            clientData.addProperty("clientId", clientId);
            clientData.addProperty("clientName", clientName);
            clientData.addProperty("mobileNo", mobileNo);
            clientData.addProperty("emailAddress", emailAddress);
            clientData.addProperty("bvn", bvn);
            clientData.addProperty("alternateMobileNumber", alternateMobileNumber);

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");

            final EnumOptionData loanType = null;

            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            final String loanOfficerName = rs.getString("loanOfficerName");

            final Long loanPurposeId = null;
            final String loanPurposeName = null;

            final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
            final String loanProductName = rs.getString("loanProductName");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = null;

            final LocalDate rejectedOnDate = null;
            final String rejectedByUsername = null;

            final LocalDate withdrawnOnDate = null;
            final String withdrawnByUsername = null;

            final LocalDate approvedOnDate = null;
            final String approvedByUsername = null;

            final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
            final String disbursedByUsername = null;

            final LocalDate closedOnDate = null;
            final String closedByUsername = null;

            final LocalDate writtenOffOnDate = null;

            final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername, null, null,
                    rejectedOnDate, rejectedByUsername, null, null, withdrawnOnDate, withdrawnByUsername, null, null, approvedOnDate,
                    approvedByUsername, null, null, null, actualDisbursementDate, disbursedByUsername, null, null, closedOnDate,
                    closedByUsername, null, null, null, writtenOffOnDate, closedByUsername, null, null);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal approvedPrincipal = null;
            final BigDecimal proposedPrincipal = null;
            final BigDecimal netDisbursalAmount = null;

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final Integer termFrequency = JdbcSupport.getInteger(rs, "termFrequency");
            final Integer termPeriodFrequencyTypeInt = JdbcSupport.getInteger(rs, "termPeriodFrequencyType");
            final EnumOptionData termPeriodFrequencyType = LoanEnumerations.termFrequencyType(termPeriodFrequencyTypeInt);

            final LoanStatusEnumData status = null;

            // loan summary
            final BigDecimal principalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalOverdue");

            final BigDecimal interestOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "interestOverdue");
            final BigDecimal feeChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "feeChargesOverdue");

            final BigDecimal penaltyChargesOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "penaltyChargesOverdue");

            final BigDecimal totalExpectedRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalExpectedRepayment");
            final BigDecimal totalRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRepayment");
            final BigDecimal totalOutstanding = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOutstanding");
            final BigDecimal totalRecovered = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRecovered");

            Boolean inArrears = true;
            final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");

            final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");

            final LoanSummaryData loanSummary = new LoanSummaryData(currencyData, null, null, null, null, principalOverdue, null, null,
                    null, null, null, interestOverdue, null, null, null, null, null, null, feeChargesOverdue, null, null, null, null, null,
                    penaltyChargesOverdue, totalExpectedRepayment, totalRepayment, null, null, null, null, totalOutstanding, totalOverdue,
                    overdueSinceDate, null, null, totalRecovered);

            GroupGeneralData groupData = null;
            if (groupId != null) {
                final EnumOptionData groupStatus = null;
                final LocalDate activationDate = null;
                groupData = GroupGeneralData.instance(groupId, null, groupName, null, groupStatus, activationDate, null, null, null, null,
                        null, null, null, null, null);
            }
            final Boolean isNPA = null;
            final Boolean canUseForTopup = null;
            final boolean isTopup = rs.getBoolean("isTopup");

            final LoanBusinessAccountData loanBusinessAccountData = LoanBusinessAccountData.basicLoanDetails(id, accountNo, status, null,
                    clientId, null, clientName, null, groupData, loanType, loanProductId, loanProductName, null, false, null, null,
                    loanPurposeId, loanPurposeName, loanOfficerId, loanOfficerName, currencyData, proposedPrincipal, principal,
                    approvedPrincipal, netDisbursalAmount, null, null, termFrequency, termPeriodFrequencyType, numberOfRepayments, null,
                    null, null, null, null, null, null, interestRatePerPeriod, null, annualInterestRate, null, false, null, null, null,
                    null, null, null, null, null, null, timeline, loanSummary, null, null, null, null, null, null, null, null, inArrears,
                    null, isNPA, null, null, false, null, null, null, null, null, null, canUseForTopup, isTopup, null, null, null, false,
                    null);
            loanBusinessAccountData.setClientData(clientData);
            return loanBusinessAccountData;
        }
    }

    public static final class LoanArrearsSummaryMapper implements RowMapper<JsonObject> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanArrearsSummaryMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            return " rc.display_symbol currencyDisplaySymbol, SUM(COALESCE(mla.principal_amount,0)) totalPrincipal, "
                    + " SUM(COALESCE(mla.total_overdue_derived,0)) totalOverdue, SUM(COALESCE(mla.total_repayment_derived,0)) totalRepayment, "
                    + " mla.product_id loanProductId, mla.product_name loanProductName, " + " COUNT(mla.id) AS totalCount "
                    + " FROM m_loan_arrears_view mla " + " join m_currency rc on rc." + sqlGenerator.escape("code")
                    + " = mla.currency_code";
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
            final String loanProductName = rs.getString("loanProductName");

            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");

            final BigDecimal totalRepayment = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalRepayment");
            final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");
            final BigDecimal totalPrincipal = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalPrincipal");
            final BigDecimal totalLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "loanBalance");
            final Long totalLoanCount = rs.getLong("totalCount");

            final JsonObject loanSummary = new JsonObject();
            loanSummary.addProperty(statusParameterName, Boolean.TRUE);
            loanSummary.addProperty(loanProductIdParameterName, loanProductId);
            loanSummary.addProperty(loanProductNameParameterName, loanProductName);
            loanSummary.addProperty(currencyDisplaySymbolParameterName, currencyDisplaySymbol);
            loanSummary.addProperty(totalRepaymentParameterName, totalRepayment);
            loanSummary.addProperty(totalOverdueParameterName, totalOverdue);
            loanSummary.addProperty(totalPrincipalParameterName, totalPrincipal);
            loanSummary.addProperty(totalLoanBalanceParameterName, totalLoanBalance);
            loanSummary.addProperty(totalLoanCountParameterName, totalLoanCount);
            return loanSummary;
        }
    }

    private String buildSqlStringFromLoanArrearsSummaryCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            // final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(mla.overdue_since_date_derived AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(mla.overdue_since_date_derived AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(mla.overdue_since_date_derived AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isProductIdPassed()) {
            extraCriteria += "and mla.product_id =?";
            paramList.add(searchParameters.getProductId());
        }
        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += "and mla.loan_officer_id =?";
            paramList.add(searchParameters.getStaffId());
        }

        if (searchParameters.isOfficeIdPassed()) {
            extraCriteria += "and mla.office_id =?";
            paramList.add(searchParameters.getOfficeId());
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

}
