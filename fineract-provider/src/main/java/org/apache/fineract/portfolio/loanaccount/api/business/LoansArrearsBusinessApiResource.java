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
package org.apache.fineract.portfolio.loanaccount.api.business;

import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.CLIENT_SIGNATURE_ID;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentBusinessWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.business.metrics.service.MetricsReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.service.business.ClientIdentifierBusinessReadPlatformService;
import org.apache.fineract.portfolio.collateral.service.CollateralReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.data.GlimRepaymentTemplate;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.business.LoanBusinessAccountData;
import org.apache.fineract.portfolio.loanaccount.data.business.LoanBusinessDocData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.business.LoanArrearsBusinessReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductPaymentTypeConfigReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.business.LoanProductBusinessReadPlatformService;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/loans/business/arrears")
@Component
@Scope("singleton")
@Tag(name = "Loans Arrears", description = "The API concept of loans Arrears models the loan application process and the loan contract/monitoring process.")
public class LoansArrearsBusinessApiResource {

    private final Set<String> loanDataDocParameters = new HashSet<>(
            Arrays.asList("loanBusinessAccountData", "clientAddressData", "loanProductData", "clientSignature"));

    private final Set<String> loanDataParameters = new HashSet<>(Arrays.asList("id", "accountNo", "status", "externalId", "clientId",
            "group", "loanProductId", "loanProductName", "loanProductDescription",
            // "isLoanProductLinkedToFloatingRate",
            "fundId", "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal",
            "totalOverpaid", "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery",
            "interestRatePerPeriod", "annualInterestRate", "repaymentFrequencyType", "transactionProcessingStrategyId",
            "transactionProcessingStrategyName", "interestRateFrequencyType", "amortizationType", "interestType",
            "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "expectedFirstRepaymentOnDate", "graceOnPrincipalPayment", "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment",
            "graceOnInterestCharged", "interestChargedFromDate", "timeline", "totalFeeChargesAtDisbursement", "summary",
            "repaymentSchedule", "transactions", "charges", "collateral", "guarantors", "meeting", "productOptions",
            "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", "repaymentFrequencyTypeOptions",
            "repaymentFrequencyNthDayTypeOptions", "repaymentFrequencyDaysOfWeekTypeOptions", "termFrequencyTypeOptions",
            "interestRateFrequencyTypeOptions", "fundOptions", "repaymentStrategyOptions", "chargeOptions", "loanOfficerOptions",
            "loanPurposeOptions", "loanCollateralOptions", "chargeTemplate", "calendarOptions", "syncDisbursementWithMeeting",
            "loanCounter", "loanProductCounter", "notes", "accountLinkingOptions", "linkedAccount", "interestRateDifferential",
            // "isFloatingInterestRate",
            "interestRatesPeriods", LoanApiConstants.canUseForTopup, LoanApiConstants.isTopup, LoanApiConstants.loanIdToClose,
            LoanApiConstants.topupAmount, LoanApiConstants.clientActiveLoanOptions, LoanApiConstants.datatables,
            LoanProductConstants.RATES_PARAM_NAME, LoanApiConstants.MULTIDISBURSE_DETAILS_PARAMNAME,
            LoanApiConstants.EMI_AMOUNT_VARIATIONS_PARAMNAME, LoanApiConstants.COLLECTION_PARAMNAME, "clientData",
            LoanBusinessApiConstants.activationChannelIdParam, LoanBusinessApiConstants.activationChannelNameParam,
            LoanBusinessApiConstants.metricsDataParam, "collateralOld", LoanBusinessApiConstants.loanProductPaymentTypeConfigDataParam));

    private final String resourceNameForPermissions = "LOAN_ARREARS";

    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanProductBusinessReadPlatformService loanProductBusinessReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanBusinessDocData> toApiDocJsonSerializer;
    private final ToApiJsonSerializer<JsonObject> loanArrearsSummarySerializer;
    private final DefaultToApiJsonSerializer<LoanBusinessAccountData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer;
    private final DefaultToApiJsonSerializer<String> calculateLoanScheduleToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final FromJsonHelper fromJsonHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final NoteReadPlatformService noteReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final RateReadService rateReadService;
    private final ConfigurationDomainService configurationDomainService;
    private final DefaultToApiJsonSerializer<GlimRepaymentTemplate> glimTemplateToApiJsonSerializer;
    private final GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService;
    private final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService;

    private final LoanArrearsBusinessReadPlatformService loanArrearsBusinessReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoansApiResource loansApiResource;
    private final MetricsReadPlatformService metricsReadPlatformService;
    private final AddressReadPlatformServiceImpl readPlatformService;
    private final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService;
    private final DocumentBusinessWritePlatformService documentWritePlatformService;
    private final Long clientSignatureId;
    private final CollateralReadPlatformService loanCollateralReadPlatformService;
    private final LoanProductPaymentTypeConfigReadPlatformService loanProductPaymentTypeConfigReadPlatformService;

    public LoansArrearsBusinessApiResource(final PlatformSecurityContext context,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final GuarantorReadPlatformService guarantorReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final DefaultToApiJsonSerializer<LoanBusinessAccountData> toApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanApprovalData> loanApprovalDataToApiJsonSerializer,
            final DefaultToApiJsonSerializer<LoanScheduleData> loanScheduleToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final FromJsonHelper fromJsonHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final NoteReadPlatformService noteReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformServiceImpl,
            final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService,
            final LoanScheduleHistoryReadPlatformService loanScheduleHistoryReadPlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService, final RateReadService rateReadService,
            final ConfigurationDomainService configurationDomainService,
            final DefaultToApiJsonSerializer<GlimRepaymentTemplate> glimTemplateToApiJsonSerializer,
            final GLIMAccountInfoReadPlatformService glimAccountInfoReadPlatformService,
            final LoanCollateralManagementReadPlatformService loanCollateralManagementReadPlatformService,
            final LoanArrearsBusinessReadPlatformService loanArrearsBusinessReadPlatformService,
            final DefaultToApiJsonSerializer<String> calculateLoanScheduleToApiJsonSerializer, final LoansApiResource loansApiResource,
            final LoanReadPlatformService loanReadPlatformService, final MetricsReadPlatformService metricsReadPlatformService,
            final LoanProductBusinessReadPlatformService loanProductBusinessReadPlatformService,
            final ToApiJsonSerializer<JsonObject> loanArrearsSummarySerializer,
            final LoanProductPaymentTypeConfigReadPlatformService loanProductPaymentTypeConfigReadPlatformService,
            final CollateralReadPlatformService loanCollateralReadPlatformService, final ApplicationContext contextApplication,
            final DefaultToApiJsonSerializer<LoanBusinessDocData> toApiDocJsonSerializer,
            final AddressReadPlatformServiceImpl readPlatformService,
            final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService,
            final DocumentBusinessWritePlatformService documentWritePlatformService) {
        this.context = context;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.loanChargeReadPlatformService = loanChargeReadPlatformService;
        this.calculationPlatformService = calculationPlatformService;
        this.guarantorReadPlatformService = guarantorReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.groupReadPlatformService = groupReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.loanApprovalDataToApiJsonSerializer = loanApprovalDataToApiJsonSerializer;
        this.loanScheduleToApiJsonSerializer = loanScheduleToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.fromJsonHelper = fromJsonHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.noteReadPlatformService = noteReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformServiceImpl;
        this.accountAssociationsReadPlatformService = accountAssociationsReadPlatformService;
        this.loanScheduleHistoryReadPlatformService = loanScheduleHistoryReadPlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.rateReadService = rateReadService;
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
        this.configurationDomainService = configurationDomainService;
        this.glimTemplateToApiJsonSerializer = glimTemplateToApiJsonSerializer;
        this.glimAccountInfoReadPlatformService = glimAccountInfoReadPlatformService;
        this.loanCollateralManagementReadPlatformService = loanCollateralManagementReadPlatformService;
        this.loanArrearsBusinessReadPlatformService = loanArrearsBusinessReadPlatformService;
        this.calculateLoanScheduleToApiJsonSerializer = calculateLoanScheduleToApiJsonSerializer;
        this.loansApiResource = loansApiResource;
        this.loanReadPlatformService = loanReadPlatformService;
        this.metricsReadPlatformService = metricsReadPlatformService;
        this.loanProductBusinessReadPlatformService = loanProductBusinessReadPlatformService;
        this.toApiDocJsonSerializer = toApiDocJsonSerializer;
        this.readPlatformService = readPlatformService;
        this.clientIdentifierBusinessReadPlatformService = clientIdentifierBusinessReadPlatformService;
        this.documentWritePlatformService = documentWritePlatformService;
        Environment environment = contextApplication.getEnvironment();
        this.clientSignatureId = Long.valueOf(environment.getProperty(CLIENT_SIGNATURE_ID));
        this.loanCollateralReadPlatformService = loanCollateralReadPlatformService;
        this.loanProductPaymentTypeConfigReadPlatformService = loanProductPaymentTypeConfigReadPlatformService;
        this.loanArrearsSummarySerializer = loanArrearsSummarySerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loans", description = """
            The list capability of loans can support pagination and sorting.
            Example Requests:

            loans\\business

            loans\\business?fields=accountNo

            loans\\business?offset=10&limit=50

            loans\\business?orderBy=accountNo&sortOrder=DESC""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))
    ) })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoansArrearsBusiness(productId, clientId, officeId,
                offset, limit, orderBy, sortOrder, staffId, accountNo, fromDate, toDate);

        final Page<LoanBusinessAccountData> loanBasicDetails = this.loanArrearsBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, this.loanDataParameters);
    }

    @GET
    @Path("summary")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve loan arrears summary", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdAccountsResponse.class))
    ), @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveAssociatedAccounts(@Context final UriInfo uriInfo,
            @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }
        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoansArrearsBusiness(productId, clientId, officeId,
                offset, limit, orderBy, sortOrder, staffId, accountNo, fromDate, toDate);
        final JsonObject retrieveSummary = this.loanArrearsBusinessReadPlatformService.retrieveLoanArrearsSummary(searchParameters);
        final Set<String> SUMMARY_DATA_PARAMETERS = new HashSet<>(Arrays.asList("summary", "summaryInfo"));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.loanArrearsSummarySerializer.serialize(settings, retrieveSummary, SUMMARY_DATA_PARAMETERS);
    }
}
