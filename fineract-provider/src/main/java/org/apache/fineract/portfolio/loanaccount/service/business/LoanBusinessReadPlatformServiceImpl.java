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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
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
import org.apache.fineract.portfolio.accountdetails.service.AccountEnumerations;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApplicationTimelineData;
import org.apache.fineract.portfolio.loanaccount.data.LoanStatusEnumData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSubStatus;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.CLIENT_DEFAULT_ID_API;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LoanBusinessReadPlatformServiceImpl implements LoanBusinessReadPlatformService {

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

    @Autowired
    public LoanBusinessReadPlatformServiceImpl(final PlatformSecurityContext context,
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
            final ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper,
            final ApplicationContext applicationContext,
            final LoansApiResource loansApiResource,
            final FromJsonHelper fromJsonHelper) {
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
    }

    @Override
    public String calculateLoanScheduleLoanApplication(String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        this.context.authenticatedUser();
        final String loanTemplateJson = LoanBusinessApiConstants.loanTemplateConfig(this.loansApiResource, apiRequestBodyAsJson, fromJsonHelper, clientDefaultId, true, uriInfo);
        log.info("calculateLoanScheduleLoanApplicationTemplate: {}", loanTemplateJson);
        return this.loansApiResource.calculateLoanScheduleOrSubmitLoanApplication("calculateLoanSchedule", uriInfo,
                loanTemplateJson);
    }

    @Override
    public Page<LoanAccountData> retrieveAll(final SearchParametersBusiness searchParameters) {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final StringBuilder sqlBuilder = new StringBuilder(200);
//        sqlBuilder.append("select " + sqlGenerator.calcFoundRows() + " ");
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.loanLoanMapper.loanSchema());

        // TODO - for time being this will data scope list of loans returned to
        // only loans that have a client associated.
        // to support senario where loan has group_id only OR client_id will
        // probably require a UNION query
        // but that at present is an edge case
        sqlBuilder.append(" join m_office o on (o.id = c.office_id or o.id = g.office_id) ");
        sqlBuilder.append(" left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
        sqlBuilder.append(" where ( o.hierarchy like ? or transferToOffice.hierarchy like ?)");

        int arrayPos = 2;
        List<Object> extraCriterias = new ArrayList<>();
        extraCriterias.add(hierarchySearchString);
        extraCriterias.add(hierarchySearchString);

        if (searchParameters != null) {

            if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
                final LocalDate startPeriod = searchParameters.getFromDate();
                final LocalDate endPeriod = searchParameters.getToDate();
                final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                if (startPeriod != null && endPeriod != null) {
                    sqlBuilder.append(" and CAST(l.submittedon_date AS DATE) BETWEEN ? AND ? ");
                    extraCriterias.add(df.format(startPeriod));
                    arrayPos = arrayPos + 1;
                    extraCriterias.add(df.format(endPeriod));
                    arrayPos = arrayPos + 1;
                } else if (startPeriod != null) {
                    sqlBuilder.append(" and CAST(l.submittedon_date AS DATE) >= ? ");
                    extraCriterias.add(df.format(startPeriod));
                    arrayPos = arrayPos + 1;
                } else if (endPeriod != null) {
                    sqlBuilder.append(" and CAST(l.submittedon_date AS DATE) <= ? ");
                    extraCriterias.add(df.format(endPeriod));
                    arrayPos = arrayPos + 1;
                }
            }

            if (searchParameters.isStatusIdPassed()) {
                sqlBuilder.append(" and l.loan_status_id = ?");
                extraCriterias.add(searchParameters.getStatusId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.isExternalIdPassed()) {
                sqlBuilder.append(" and l.external_id = ?");
                extraCriterias.add(searchParameters.getExternalId());
                arrayPos = arrayPos + 1;
            }
            if (searchParameters.isOfficeIdPassed()) {
                sqlBuilder.append("and c.office_id =?");
                extraCriterias.add(searchParameters.getOfficeId());
                arrayPos = arrayPos + 1;
            }

            if (searchParameters.isAccountNoPassed()) {
                sqlBuilder.append(" and l.account_no = ?");
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

    private static final class LoanMapper implements RowMapper<LoanAccountData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        LoanMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String loanSchema() {
            return " l.id,"
                    + " l.external_id externalId,"
                    + " l.account_no accountNo,"
                    + " l.client_id clientId, c.display_name as clientName,"
                    + " l.group_id groupId, g.display_name as groupName,"
                    + " l.product_id loanProductId, lp.name as loanProductName,"
                    + " l.loan_officer_id, s.display_name as loanOfficerName,"
                    + " l.loanpurpose_cv_id, cv.code_value as loanPurposeName,"
                    + " l.loan_status_id lifeCycleStatusId,"
                    + " l.loan_type_enum loanType,"
                    + " l.principal_amount_proposed as proposedPrincipal, l.principal_amount as principal, l.approved_principal as approvedPrincipal,"
                    + " l.net_disbursal_amount netDisbursalAmount,"
                    + " l.nominal_interest_rate_per_period interestRatePerPeriod, l.annual_nominal_interest_rate as annualInterestRate,"
                    + " l.term_frequency termFrequency,"
                    + " l.number_of_repayments numberOfRepayments,"
                    + " l.is_topup isTopup, l.is_npa as isNPA, lp.can_use_for_topup as canUseForTopup,"
                    + " la.total_overdue_derived as totalOverdue,"
                    + " la.overdue_since_date_derived as overdueSinceDate,"
                    + " l.currency_code as currencyCode, rc."
                    + sqlGenerator.escape("name")
                    + " as currencyName, rc.display_symbol as currencyDisplaySymbol, rc.internationalized_name_code as currencyNameCode, "
                    + " l.submittedon_date as submittedOnDate, sbu.username as submittedByUsername,"
                    + " l.rejectedon_date as rejectedOnDate, rbu.username as rejectedByUsername,"
                    + " l.withdrawnon_date as withdrawnOnDate, wbu.username as withdrawnByUsername,"
                    + " l.approvedon_date as approvedOnDate, abu.username as approvedByUsername,"
                    + " l.disbursedon_date as actualDisbursementDate, dbu.username as disbursedByUsername,"
                    + " l.closedon_date as closedOnDate, cbu.username as closedByUsername, cbu.firstname as closedByFirstname, cbu.lastname as closedByLastname, l.writtenoffon_date as writtenOffOnDate "
                    + " FROM m_loan_view l "
                    + " left join m_loan_arrears_aging la on la.loan_id = l.id" //
                    + " left join m_client c on c.id = l.client_id "
                    + " left join m_group g on g.id = l.group_id"
                    + " join m_product_loan lp on lp.id = l.product_id"
                    + " left join m_staff s on s.id = l.loan_officer_id"
                    + " left join m_appuser sbu on sbu.id = l.created_by"
                    + " left join m_appuser rbu on rbu.id = l.rejectedon_userid"
                    + " left join m_appuser wbu on wbu.id = l.withdrawnon_userid"
                    + " left join m_appuser abu on abu.id = l.approvedon_userid"
                    + " left join m_appuser dbu on dbu.id = l.disbursedon_userid" + " left join m_appuser cbu on cbu.id = l.closedon_userid"
                    + " left join m_code_value cv on cv.id = l.loanpurpose_cv_id"
                    + " join m_currency rc on rc." + sqlGenerator.escape("code") + " = l.currency_code";
        }

        @Override
        public LoanAccountData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final String currencyCode = rs.getString("currencyCode");
            final String currencyName = rs.getString("currencyName");
            final String currencyNameCode = rs.getString("currencyNameCode");
            final String currencyDisplaySymbol = rs.getString("currencyDisplaySymbol");
            final CurrencyData currencyData = new CurrencyData(currencyCode, currencyName, 0, null,
                    currencyDisplaySymbol, currencyNameCode);

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("accountNo");
            final String externalId = rs.getString("externalId");

            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            final String clientName = rs.getString("clientName");

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");

            final Integer loanTypeId = JdbcSupport.getInteger(rs, "loanType");
            final EnumOptionData loanType = AccountEnumerations.loanType(loanTypeId);

            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            final String loanOfficerName = rs.getString("loanOfficerName");

            final Long loanPurposeId = JdbcSupport.getLong(rs, "loanPurposeId");
            final String loanPurposeName = rs.getString("loanPurposeName");

            final Long loanProductId = JdbcSupport.getLong(rs, "loanProductId");
            final String loanProductName = rs.getString("loanProductName");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final LocalDate rejectedOnDate = JdbcSupport.getLocalDate(rs, "rejectedOnDate");
            final String rejectedByUsername = rs.getString("rejectedByUsername");
            final String rejectedByFirstname = rs.getString("rejectedByFirstname");
            final String rejectedByLastname = rs.getString("rejectedByLastname");

            final LocalDate withdrawnOnDate = JdbcSupport.getLocalDate(rs, "withdrawnOnDate");
            final String withdrawnByUsername = rs.getString("withdrawnByUsername");
            final String withdrawnByFirstname = rs.getString("withdrawnByFirstname");
            final String withdrawnByLastname = rs.getString("withdrawnByLastname");

            final LocalDate approvedOnDate = JdbcSupport.getLocalDate(rs, "approvedOnDate");
            final String approvedByUsername = rs.getString("approvedByUsername");
            final String approvedByFirstname = rs.getString("approvedByFirstname");
            final String approvedByLastname = rs.getString("approvedByLastname");

            final LocalDate actualDisbursementDate = JdbcSupport.getLocalDate(rs, "actualDisbursementDate");
            final String disbursedByUsername = rs.getString("disbursedByUsername");
            final String disbursedByFirstname = rs.getString("disbursedByFirstname");
            final String disbursedByLastname = rs.getString("disbursedByLastname");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate writtenOffOnDate = JdbcSupport.getLocalDate(rs, "writtenOffOnDate");

            final LoanApplicationTimelineData timeline = new LoanApplicationTimelineData(submittedOnDate, submittedByUsername,
                    submittedByFirstname, submittedByLastname, rejectedOnDate, rejectedByUsername, rejectedByFirstname, rejectedByLastname,
                    withdrawnOnDate, withdrawnByUsername, withdrawnByFirstname, withdrawnByLastname, approvedOnDate, approvedByUsername,
                    approvedByFirstname, approvedByLastname, null, actualDisbursementDate, disbursedByUsername,
                    disbursedByFirstname, disbursedByLastname, closedOnDate, closedByUsername, closedByFirstname, closedByLastname,
                    null, writtenOffOnDate, closedByUsername, closedByFirstname, closedByLastname);

            final BigDecimal principal = rs.getBigDecimal("principal");
            final BigDecimal approvedPrincipal = rs.getBigDecimal("approvedPrincipal");
            final BigDecimal proposedPrincipal = rs.getBigDecimal("proposedPrincipal");
            final BigDecimal netDisbursalAmount = rs.getBigDecimal("netDisbursalAmount");

            final Integer numberOfRepayments = JdbcSupport.getInteger(rs, "numberOfRepayments");
            final BigDecimal interestRatePerPeriod = rs.getBigDecimal("interestRatePerPeriod");
            final BigDecimal annualInterestRate = rs.getBigDecimal("annualInterestRate");

            final Integer termFrequency = JdbcSupport.getInteger(rs, "termFrequency");

            final Integer lifeCycleStatusId = JdbcSupport.getInteger(rs, "lifeCycleStatusId");
            final LoanStatusEnumData status = LoanEnumerations.status(lifeCycleStatusId);

            final Integer loanSubStatusId = JdbcSupport.getInteger(rs, "loanSubStatusId");
            EnumOptionData loanSubStatus = null;
            if (loanSubStatusId != null) {
                loanSubStatus = LoanSubStatus.loanSubStatus(loanSubStatusId);
            }

            LoanSummaryData loanSummary = null;
            Boolean inArrears = false;
            if (status.id().intValue() >= 300) {
                final BigDecimal totalOverdue = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdue");

                final LocalDate overdueSinceDate = JdbcSupport.getLocalDate(rs, "overdueSinceDate");
                if (overdueSinceDate != null) {
                    inArrears = true;
                }

                loanSummary = new LoanSummaryData(currencyData, null, null, null,
                        null, null, null, null, null, null,
                        null, null, null, null, null,
                        null, null, null, null, null,
                        null, null, null, null,
                        null, null, null, null, null,
                        null, null, null, totalOverdue, overdueSinceDate, null, null,
                        null);
            }

            GroupGeneralData groupData = null;
            if (groupId != null) {
                final Integer groupStatusEnum = JdbcSupport.getInteger(rs, "statusEnum");
                final EnumOptionData groupStatus = ClientEnumerations.status(groupStatusEnum);
                final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
                groupData = GroupGeneralData.instance(groupId, null, groupName, null, groupStatus, activationDate,
                        null, null, null, null, null, null, null, null, null);
            }

            final Boolean isNPA = rs.getBoolean("isNPA");

            final boolean canUseForTopup = rs.getBoolean("canUseForTopup");
            final boolean isTopup = rs.getBoolean("isTopup");

            return LoanAccountData.basicLoanDetails(id, accountNo, status, externalId, clientId, null, clientName,
                    null, groupData, loanType, loanProductId, loanProductName, null,
                    false,
                    null,
                    null,
                    loanPurposeId,
                    loanPurposeName,
                    loanOfficerId, loanOfficerName,
                    currencyData, proposedPrincipal, principal, approvedPrincipal, netDisbursalAmount, null, null,
                    termFrequency, null, numberOfRepayments, null, null, null, null,
                    null, null, null, interestRatePerPeriod, null,
                    annualInterestRate, null,
                    false,
                    null,
                    null,
                    null, null, null,
                    null, null, null, null,
                    timeline, loanSummary, null, null, null, null,
                    null, null, null, null, inArrears, null,
                    isNPA, null,
                    null,
                    false,
                    null,
                    null, null, null, null, loanSubStatus,
                    canUseForTopup, isTopup, null, null, null, false,
                    null);
        }
    }

}
