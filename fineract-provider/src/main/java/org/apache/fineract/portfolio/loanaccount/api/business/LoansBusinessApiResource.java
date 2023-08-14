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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.data.GlimRepaymentTemplate;
import org.apache.fineract.portfolio.loanaccount.data.LoanAccountData;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.business.LoanBusinessReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/business")
@Component
@Scope("singleton")
@Tag(name = "Loans", description = "The API concept of loans models the loan application process and the loan contract/monitoring process.")
public class LoansBusinessApiResource {

    private final Set<String> loanDataParameters = new HashSet<>(Arrays.asList("id", "accountNo", "status", "externalId", "clientId",
            "group", "loanProductId", "loanProductName", "loanProductDescription",
            //"isLoanProductLinkedToFloatingRate", 
            "fundId",
            "fundName", "loanPurposeId", "loanPurposeName", "loanOfficerId", "loanOfficerName", "currency", "principal", "totalOverpaid",
            "inArrearsTolerance", "termFrequency", "termPeriodFrequencyType", "numberOfRepayments", "repaymentEvery",
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
            //"isFloatingInterestRate", 
            "interestRatesPeriods", LoanApiConstants.canUseForTopup, LoanApiConstants.isTopup,
            LoanApiConstants.loanIdToClose, LoanApiConstants.topupAmount, LoanApiConstants.clientActiveLoanOptions,
            LoanApiConstants.datatables, LoanProductConstants.RATES_PARAM_NAME, LoanApiConstants.MULTIDISBURSE_DETAILS_PARAMNAME,
            LoanApiConstants.EMI_AMOUNT_VARIATIONS_PARAMNAME, LoanApiConstants.COLLECTION_PARAMNAME));

    private final String resourceNameForPermissions = "LOAN";

    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final LoanChargeReadPlatformService loanChargeReadPlatformService;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final GroupReadPlatformService groupReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer;
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

    private final LoanBusinessReadPlatformService loanBusinessReadPlatformService;
    private final LoansApiResource loansApiResource;

    public LoansBusinessApiResource(final PlatformSecurityContext context,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanDropdownReadPlatformService dropdownReadPlatformService, final FundReadPlatformService fundReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final LoanChargeReadPlatformService loanChargeReadPlatformService,
            final LoanScheduleCalculationPlatformService calculationPlatformService,
            final GuarantorReadPlatformService guarantorReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final GroupReadPlatformService groupReadPlatformService,
            final DefaultToApiJsonSerializer<LoanAccountData> toApiJsonSerializer,
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
            final LoanBusinessReadPlatformService loanBusinessReadPlatformService,
            final DefaultToApiJsonSerializer<String> calculateLoanScheduleToApiJsonSerializer, final LoansApiResource loansApiResource) {
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
        this.loanBusinessReadPlatformService = loanBusinessReadPlatformService;
        this.calculateLoanScheduleToApiJsonSerializer = calculateLoanScheduleToApiJsonSerializer;
        this.loansApiResource = loansApiResource;
    }

    /*
     * This template API is used for loan approval, ideally this should be invoked on loan that are pending for
     * approval. But system does not validate the status of the loan, it returns the template irrespective of loan
     * status
     */
    @POST
    @Path("calculate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Calculate loan repayment schedule")
    @RequestBody(required = true
    //,content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //,content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))
        )})
    public String calculateLoanScheduleLoanApplication(@Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        return this.loanBusinessReadPlatformService.calculateLoanScheduleLoanApplication(apiRequestBodyAsJson, uriInfo);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Loans", description = """
                                                     The list capability of loans can support pagination and sorting.
                                                     Example Requests:
                                                     
                                                     loans\\business
                                                     
                                                     loans\\business?fields=accountNo
                                                     
                                                     loans\\business?offset=10&limit=50
                                                     
                                                     loans\\business?orderBy=accountNo&sortOrder=DESC""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))
        )})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
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
            fromDate
                    = startPeriod.getDate(
                            LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoansBusiness(officeId, externalId, statusId, null, offset, limit, orderBy, sortOrder, null, accountNo, fromDate, toDate);

        final Page<LoanAccountData> loanBasicDetails = this.loanBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, loanBasicDetails, this.loanDataParameters);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Submit a new Loan Application", description = """
                                                                                                            Submits a new loan application
                                                                                                            Mandatory Fields: clientId, productId, principal, loanTermFrequency, loanTermFrequencyType, loanType, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyId, expectedDisbursementDate, submittedOnDate, loanType
                                                                                                            Optional Fields: graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, linkAccountId, allowPartialPeriodInterestCalcualtion, fixedEmiAmount, maxOutstandingLoanBalance, disbursementData, graceOnArrearsAgeing, createStandingInstructionAtDisbursement (requires linkedAccountId if set to true)
                                                                                                            Additional Mandatory Fields if interest recalculation is enabled for product and Rest frequency not same as repayment period: recalculationRestFrequencyDate
                                                                                                            Additional Mandatory Fields if interest recalculation with interest/fee compounding is enabled for product and compounding frequency not same as repayment period: recalculationCompoundingFrequencyDate
                                                                                                            Additional Mandatory Field if Entity-Datatable Check is enabled for the entity of type loan: datatables""")
    @RequestBody(required = true
    //, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))
        )})
    public String submitLoanApplication(
            @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        final String loanTemplateJson = LoanBusinessApiConstants.loanTemplateConfig(this.loansApiResource, apiRequestBodyAsJson, fromJsonHelper, null, true, uriInfo);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanBusinessApplication().withJson(loanTemplateJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Modify a loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true
    //, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdResponse.class))
        )})
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanBusinessApplication(loanId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
