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

import static org.apache.fineract.portfolio.loanproduct.service.LoanEnumerations.interestType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.dataqueries.api.DataTableApiConstant;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentBusinessWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.accountdetails.data.LoanAccountSummaryData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.data.ClientAddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.business.metrics.service.MetricsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessData;
import org.apache.fineract.portfolio.client.service.business.ClientIdentifierBusinessReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.data.LoanCollateralResponseData;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralManagementReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.InterestRatePeriodData;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.DisbursementData;
import org.apache.fineract.portfolio.loanaccount.data.GlimRepaymentTemplate;
import org.apache.fineract.portfolio.loanaccount.data.LoanApprovalData;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTermVariationsData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.data.PaidInAdvanceData;
import org.apache.fineract.portfolio.loanaccount.data.RepaymentScheduleRelatedLoanData;
import org.apache.fineract.portfolio.loanaccount.data.business.LoanBusinessAccountData;
import org.apache.fineract.portfolio.loanaccount.data.business.LoanBusinessDocData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTermVariationType;
import org.apache.fineract.portfolio.loanaccount.guarantor.data.GuarantorData;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleHistoryReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.business.LoanBusinessReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.InterestMethod;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.business.LoanProductBusinessReadPlatformService;
import org.apache.fineract.portfolio.note.data.NoteData;
import org.apache.fineract.portfolio.note.domain.NoteType;
import org.apache.fineract.portfolio.note.service.NoteReadPlatformService;
import org.apache.fineract.portfolio.rate.data.RateData;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Path("/loans/business")
@Component
@Scope("singleton")
@Tag(name = "Loans", description = "The API concept of loans models the loan application process and the loan contract/monitoring process.")
public class LoansBusinessApiResource {

    private final Set<String> loanDataDocParameters = new HashSet<>(Arrays.asList("loanBusinessAccountData", "clientAddressData", "loanProductData", "clientSignature"));

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
            LoanApiConstants.EMI_AMOUNT_VARIATIONS_PARAMNAME, LoanApiConstants.COLLECTION_PARAMNAME,
            LoanBusinessApiConstants.activationChannelIdParam, LoanBusinessApiConstants.activationChannelNameParam, LoanBusinessApiConstants.metricsDataParam));

    private final String resourceNameForPermissions = "LOAN";

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

    private final LoanBusinessReadPlatformService loanBusinessReadPlatformService;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoansApiResource loansApiResource;
    private final MetricsReadPlatformService metricsReadPlatformService;
    private final AddressReadPlatformServiceImpl readPlatformService;
    private final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService;
    private final DocumentBusinessWritePlatformService documentWritePlatformService;

    public LoansBusinessApiResource(final PlatformSecurityContext context,
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
            final LoanBusinessReadPlatformService loanBusinessReadPlatformService,
            final DefaultToApiJsonSerializer<String> calculateLoanScheduleToApiJsonSerializer, final LoansApiResource loansApiResource,
            final LoanReadPlatformService loanReadPlatformService, final MetricsReadPlatformService metricsReadPlatformService, final LoanProductBusinessReadPlatformService loanProductBusinessReadPlatformService,
            final DefaultToApiJsonSerializer<LoanBusinessDocData> toApiDocJsonSerializer, final AddressReadPlatformServiceImpl readPlatformService, final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService, final DocumentBusinessWritePlatformService documentWritePlatformService) {
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
        this.loanReadPlatformService = loanReadPlatformService;
        this.metricsReadPlatformService = metricsReadPlatformService;
        this.loanProductBusinessReadPlatformService = loanProductBusinessReadPlatformService;
        this.toApiDocJsonSerializer = toApiDocJsonSerializer;
        this.readPlatformService = readPlatformService;
        this.clientIdentifierBusinessReadPlatformService = clientIdentifierBusinessReadPlatformService;
        this.documentWritePlatformService = documentWritePlatformService;
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
    // ,content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // ,content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))
        )})
    public String calculateLoanScheduleLoanApplication(@Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        return this.loanBusinessReadPlatformService.calculateLoanScheduleLoanApplication(apiRequestBodyAsJson, uriInfo);
    }

    @GET
    @Path("{loanId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Loan", description = """
            Note: template=true parameter doesn't apply to this resource.Example Requests:

            loans\\business/1


            loans/1?fields=id,principal,annualInterestRate


            loans\\business/1?associations=all

            loans\\business/1?associations=all&exclude=guarantors


            loans\\business/1?fields=id,principal,annualInterestRate&associations=repaymentSchedule,transactions""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))
        )})
    public String retrieveLoan(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("associations") @Parameter(in = ParameterIn.QUERY, name = "associations", description = "Loan object relations to be included in the response", required = false, examples = {
        @ExampleObject(value = "all"),
        @ExampleObject(value = "repaymentSchedule,transactions")}) final String associations,
            @QueryParam("exclude") @Parameter(in = ParameterIn.QUERY, name = "exclude", description = "Optional Loan object relation list to be filtered in the response", required = false, example = "guarantors,futureSchedule") final String exclude,
            @QueryParam("fields") @Parameter(in = ParameterIn.QUERY, name = "fields", description = "Optional Loan attribute list to be in the response", required = false, example = "id,principal,annualInterestRate") final String fields,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LoanBusinessAccountData loanBasicDetails = this.loanBusinessReadPlatformService.retrieveOne(loanId);

        if (loanBasicDetails.isInterestRecalculationEnabled()) {
            Collection<CalendarData> interestRecalculationCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(
                    loanBasicDetails.getInterestRecalculationDetailId(), CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL.getValue(),
                    null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCalendarDatas)) {
                calendarData = interestRecalculationCalendarDatas.iterator().next();
            }

            Collection<CalendarData> interestRecalculationCompoundingCalendarDatas = this.calendarReadPlatformService
                    .retrieveCalendarsByEntity(loanBasicDetails.getInterestRecalculationDetailId(),
                            CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL.getValue(), null);
            CalendarData compoundingCalendarData = null;
            if (!CollectionUtils.isEmpty(interestRecalculationCompoundingCalendarDatas)) {
                compoundingCalendarData = interestRecalculationCompoundingCalendarDatas.iterator().next();
            }
            loanBasicDetails = LoanBusinessAccountData.withInterestRecalculationCalendarData(loanBasicDetails, calendarData,
                    compoundingCalendarData);
        }
        if (loanBasicDetails.isMonthlyRepaymentFrequencyType()) {
            Collection<CalendarData> loanCalendarDatas = this.calendarReadPlatformService.retrieveCalendarsByEntity(loanId,
                    CalendarEntityType.LOANS.getValue(), null);
            CalendarData calendarData = null;
            if (!CollectionUtils.isEmpty(loanCalendarDatas)) {
                calendarData = loanCalendarDatas.iterator().next();
            }
            if (calendarData != null) {
                loanBasicDetails = LoanBusinessAccountData.withLoanCalendarData(loanBasicDetails, calendarData);
            }
        }
        Collection<InterestRatePeriodData> interestRatesPeriods = this.loanBusinessReadPlatformService
                .retrieveLoanInterestRatePeriodData(loanBasicDetails);
        Collection<LoanTransactionData> loanRepayments = null;
        LoanScheduleData repaymentSchedule = null;
        Collection<LoanChargeData> charges = null;
        Collection<GuarantorData> guarantors = null;
        CalendarData meeting = null;
        Collection<NoteData> notes = null;
        PortfolioAccountData linkedAccount = null;
        Collection<DisbursementData> disbursementData = null;
        Collection<LoanTermVariationsData> emiAmountVariations = null;
        Collection<LoanCollateralResponseData> loanCollateralManagements = null;
        Collection<LoanCollateralManagementData> loanCollateralManagementData = new ArrayList<>();
        CollectionData collectionData = CollectionData.template();

        final Set<String> mandatoryResponseParameters = new HashSet<>();
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains(DataTableApiConstant.allAssociateParamName)) {
                associationParameters.addAll(Arrays.asList(DataTableApiConstant.repaymentScheduleAssociateParamName,
                        DataTableApiConstant.futureScheduleAssociateParamName, DataTableApiConstant.originalScheduleAssociateParamName,
                        DataTableApiConstant.transactionsAssociateParamName, DataTableApiConstant.chargesAssociateParamName,
                        DataTableApiConstant.guarantorsAssociateParamName, DataTableApiConstant.collateralAssociateParamName,
                        DataTableApiConstant.notesAssociateParamName, DataTableApiConstant.linkedAccountAssociateParamName,
                        DataTableApiConstant.multiDisburseDetailsAssociateParamName, DataTableApiConstant.collectionAssociateParamName));
            }

            ApiParameterHelper.excludeAssociationsForResponseIfProvided(exclude, associationParameters);

            if (associationParameters.contains(DataTableApiConstant.guarantorsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.guarantorsAssociateParamName);
                guarantors = this.guarantorReadPlatformService.retrieveGuarantorsForLoan(loanId);
                if (CollectionUtils.isEmpty(guarantors)) {
                    guarantors = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.transactionsAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.transactionsAssociateParamName);
                final Collection<LoanTransactionData> currentLoanRepayments = this.loanReadPlatformService.retrieveLoanTransactions(loanId);
                if (!CollectionUtils.isEmpty(currentLoanRepayments)) {
                    loanRepayments = currentLoanRepayments;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.multiDisburseDetailsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.multiDisburseDetailsAssociateParamName);
                disbursementData = this.loanReadPlatformService.retrieveLoanDisbursementDetails(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.emiAmountVariationsAssociateParamName)
                    || associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.emiAmountVariationsAssociateParamName);
                emiAmountVariations = this.loanReadPlatformService.retrieveLoanTermVariations(loanId,
                        LoanTermVariationType.EMI_AMOUNT.getValue());
            }

            if (associationParameters.contains(DataTableApiConstant.repaymentScheduleAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.repaymentScheduleAssociateParamName);
                final RepaymentScheduleRelatedLoanData repaymentScheduleRelatedData = loanBasicDetails.repaymentScheduleRelatedData();
                repaymentSchedule = this.loanReadPlatformService.retrieveRepaymentSchedule(loanId, repaymentScheduleRelatedData,
                        disbursementData, loanBasicDetails.isInterestRecalculationEnabled(), loanBasicDetails.getTotalPaidFeeCharges());

                if (associationParameters.contains(DataTableApiConstant.futureScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.futureScheduleAssociateParamName);
                    this.calculationPlatformService.updateFutureSchedule(repaymentSchedule, loanId);
                }

                if (associationParameters.contains(DataTableApiConstant.originalScheduleAssociateParamName)
                        && loanBasicDetails.isInterestRecalculationEnabled() && loanBasicDetails.isActive()) {
                    mandatoryResponseParameters.add(DataTableApiConstant.originalScheduleAssociateParamName);
                    LoanScheduleData loanScheduleData = this.loanScheduleHistoryReadPlatformService.retrieveRepaymentArchiveSchedule(loanId,
                            repaymentScheduleRelatedData, disbursementData);
                    loanBasicDetails = LoanBusinessAccountData.withOriginalSchedule(loanBasicDetails, loanScheduleData);
                }
            }

            if (associationParameters.contains(DataTableApiConstant.chargesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.chargesAssociateParamName);
                charges = this.loanChargeReadPlatformService.retrieveLoanCharges(loanId);
                if (CollectionUtils.isEmpty(charges)) {
                    charges = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.collateralAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.collateralAssociateParamName);
                loanCollateralManagements = this.loanCollateralManagementReadPlatformService.getLoanCollateralResponseDataList(loanId);
                for (LoanCollateralResponseData loanCollateralManagement : loanCollateralManagements) {
                    loanCollateralManagementData.add(loanCollateralManagement.toCommand());
                }
                if (CollectionUtils.isEmpty(loanCollateralManagements)) {
                    loanCollateralManagements = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.meetingAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.meetingAssociateParamName);
                meeting = this.calendarReadPlatformService.retrieveLoanCalendar(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.notesAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.notesAssociateParamName);
                notes = this.noteReadPlatformService.retrieveNotesByResource(loanId, NoteType.LOAN.getValue());
                if (CollectionUtils.isEmpty(notes)) {
                    notes = null;
                }
            }

            if (associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }

            if (associationParameters.contains(DataTableApiConstant.collectionAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.collectionAssociateParamName);
                if (loanBasicDetails.isActive()) {
                    collectionData = this.loanReadPlatformService.retrieveLoanCollectionData(loanId);
                }
            }
        }

        Collection<LoanProductData> productOptions = null;
        LoanProductData product = null;
        Collection<EnumOptionData> loanTermFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyNthDayTypeOptions = null;
        Collection<EnumOptionData> repaymentFrequencyDayOfWeekTypeOptions = null;
        Collection<TransactionProcessingStrategyData> repaymentStrategyOptions = null;
        Collection<EnumOptionData> interestRateFrequencyTypeOptions = null;
        Collection<EnumOptionData> amortizationTypeOptions = null;
        Collection<EnumOptionData> interestTypeOptions = null;
        Collection<EnumOptionData> interestCalculationPeriodTypeOptions = null;
        Collection<FundData> fundOptions = null;
        Collection<StaffData> allowedLoanOfficers = null;
        Collection<ChargeData> chargeOptions = null;
        ChargeData chargeTemplate = null;
        Collection<CodeValueData> loanPurposeOptions = null;
        Collection<CodeValueData> loanCollateralOptions = null;
        Collection<CalendarData> calendarOptions = null;
        Collection<PortfolioAccountData> accountLinkingOptions = null;
        PaidInAdvanceData paidInAdvanceTemplate = null;
        Collection<LoanAccountSummaryData> clientActiveLoanOptions = null;

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            productOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();
            product = this.loanProductReadPlatformService.retrieveLoanProduct(loanBasicDetails.loanProductId());
            loanBasicDetails.setProduct(product);
            loanTermFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
            repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
            repaymentFrequencyNthDayTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForNthDayOfMonth();
            repaymentFrequencyDayOfWeekTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyOptionsForDaysOfWeek();
            interestRateFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

            amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
            if (product.isLinkedToFloatingInterestRates()) {
                interestTypeOptions = Arrays.asList(interestType(InterestMethod.DECLINING_BALANCE));
            } else {
                interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
            }
            interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();

            fundOptions = this.fundReadPlatformService.retrieveAllFunds();
            repaymentStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
            if (product.getMultiDisburseLoan()) {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                        new ChargeTimeType[]{ChargeTimeType.OVERDUE_INSTALLMENT});
            } else {
                chargeOptions = this.chargeReadPlatformService.retrieveLoanAccountApplicableCharges(loanId,
                        new ChargeTimeType[]{ChargeTimeType.OVERDUE_INSTALLMENT, ChargeTimeType.TRANCHE_DISBURSEMENT});
            }
            chargeTemplate = this.loanChargeReadPlatformService.retrieveLoanChargeTemplate();

            allowedLoanOfficers = this.loanReadPlatformService.retrieveAllowedLoanOfficers(loanBasicDetails.officeId(),
                    staffInSelectedOfficeOnly);

            loanPurposeOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanPurpose");
            loanCollateralOptions = this.codeValueReadPlatformService.retrieveCodeValuesByCode("LoanCollateral");
            final CurrencyData currencyData = loanBasicDetails.currency();
            String currencyCode = null;
            if (currencyData != null) {
                currencyCode = currencyData.code();
            }
            final long[] accountStatus = {SavingsAccountStatusType.ACTIVE.getValue()};
            PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(PortfolioAccountType.SAVINGS.getValue(),
                    loanBasicDetails.clientId(), currencyCode, accountStatus, DepositAccountType.SAVINGS_DEPOSIT.getValue());
            accountLinkingOptions = this.portfolioAccountReadPlatformService.retrieveAllForLookup(portfolioAccountDTO);

            if (!associationParameters.contains(DataTableApiConstant.linkedAccountAssociateParamName)) {
                mandatoryResponseParameters.add(DataTableApiConstant.linkedAccountAssociateParamName);
                linkedAccount = this.accountAssociationsReadPlatformService.retriveLoanLinkedAssociation(loanId);
            }
            if (loanBasicDetails.groupId() != null) {
                calendarOptions = this.loanReadPlatformService.retrieveCalendars(loanBasicDetails.groupId());
            }

            if (loanBasicDetails.product().canUseForTopup() && loanBasicDetails.clientId() != null) {
                clientActiveLoanOptions = this.accountDetailsReadPlatformService
                        .retrieveClientActiveLoanAccountSummary(loanBasicDetails.clientId());
            }

        }

        Collection<ChargeData> overdueCharges = this.chargeReadPlatformService.retrieveLoanProductCharges(loanBasicDetails.loanProductId(),
                ChargeTimeType.OVERDUE_INSTALLMENT);

        paidInAdvanceTemplate = this.loanReadPlatformService.retrieveTotalPaidInAdvance(loanId);

        // Get rates from Loan
        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();
        List<RateData> rates = null;
        if (isRatesEnabled) {
            rates = this.rateReadService.retrieveLoanRates(loanId);
        }

        final LoanBusinessAccountData loanAccount = LoanBusinessAccountData.associationsAndTemplate(loanBasicDetails, repaymentSchedule,
                loanRepayments, charges, loanCollateralManagementData, guarantors, meeting, productOptions, loanTermFrequencyTypeOptions,
                repaymentFrequencyTypeOptions, repaymentFrequencyNthDayTypeOptions, repaymentFrequencyDayOfWeekTypeOptions,
                repaymentStrategyOptions, interestRateFrequencyTypeOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, fundOptions, chargeOptions, chargeTemplate, allowedLoanOfficers, loanPurposeOptions,
                loanCollateralOptions, calendarOptions, notes, accountLinkingOptions, linkedAccount, disbursementData, emiAmountVariations,
                overdueCharges, paidInAdvanceTemplate, interestRatesPeriods, clientActiveLoanOptions, rates, isRatesEnabled,
                collectionData);

        final Collection<MetricsData> metricsData = this.metricsReadPlatformService.retrieveLoanMetrics(loanId);
        if (!CollectionUtils.isEmpty(metricsData)) {
            loanAccount.setMetricsData(metricsData);
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters(),
                mandatoryResponseParameters);
        return this.toApiJsonSerializer.serialize(settings, loanAccount, this.loanDataParameters);
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
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansResponse.class))
        )})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
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

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoansBusiness(clientId, officeId, externalId,
                statusId, null, offset, limit, orderBy, sortOrder, null, accountNo, fromDate, toDate);

        final Page<LoanBusinessAccountData> loanBasicDetails = this.loanBusinessReadPlatformService.retrieveAll(searchParameters);

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
    // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PostLoansResponse.class))
        )})
    public String submitLoanApplication(@Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        final String loanTemplateJson = LoanBusinessApiConstants.loanTemplateConfig(this.loansApiResource, apiRequestBodyAsJson,
                fromJsonHelper, null, true, uriInfo, null);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanBusinessApplication().withJson(loanTemplateJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Modify a loan application", description = "Loan application can only be modified when in 'Submitted and pending approval' state. Once the application is approved, the details cannot be changed using this method.")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.PutLoansLoanIdResponse.class))
        )})
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        log.info("before modifyLoanApplication: {}", apiRequestBodyAsJson);
        final String loanTemplateJson = LoanBusinessApiConstants.loanTemplateConfig(this.loansApiResource, apiRequestBodyAsJson,
                fromJsonHelper, null, false, uriInfo, loanId);
        log.info("after modifyLoanApplication: {}", loanTemplateJson);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanBusinessApplication(loanId).withJson(loanTemplateJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{loanId}/doc")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Loan Doc", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))
        )})
    public String retrieveLoanDoc(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @DefaultValue("all") @QueryParam("associations") @Parameter(in = ParameterIn.QUERY, name = "associations", description = "Loan object relations to be included in the response", required = false, examples = {
        @ExampleObject(value = "all"),
        @ExampleObject(value = "repaymentSchedule,transactions")}) final String associations,
            @QueryParam("exclude") @Parameter(in = ParameterIn.QUERY, name = "exclude", description = "Optional Loan object relation list to be filtered in the response", required = false, example = "guarantors,futureSchedule") final String exclude,
            @QueryParam("fields") @Parameter(in = ParameterIn.QUERY, name = "fields", description = "Optional Loan attribute list to be in the response", required = false, example = "id,principal,annualInterestRate") final String fields,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final String retrieveLoan = this.retrieveLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields, uriInfo);
        final LoanBusinessAccountData loanBasicDetails = this.fromJsonHelper.fromJson(retrieveLoan, LoanBusinessAccountData.class);
        loanBasicDetails.setLoanOfficerOptions(null);
        loanBasicDetails.setMetricsData(null);

        final Long loanProductId = loanBasicDetails.loanProductId();
        final LoanProductData loanProductData = this.loanProductBusinessReadPlatformService.retrieveLoanProductData(loanProductId);

        final Long clientId = loanBasicDetails.getClientId();
        final Integer homeAddress = 15;
        final Collection<AddressData> addressDatas = this.readPlatformService.retrieveAddressbyTypeAndStatus(clientId, homeAddress, "true");
        final AddressData clientAddressData = addressDatas.stream().findFirst().orElse(null);

        final Collection<ClientIdentifierBusinessData> clientIdentifiers = this.clientIdentifierBusinessReadPlatformService
                .retrieveClientIdentifiers(clientId);
        final Long documentTypeSignatureId = 1104L;
        final ClientIdentifierBusinessData clientIdentifierBusinessData
                = clientIdentifiers.stream().filter(predicate -> predicate.getDocumentType() != null
                && Objects.equals(predicate.getDocumentType().getId(), documentTypeSignatureId))
                        .findFirst().orElse(null);

        String clientSignature = null;
        if (ObjectUtils.isNotEmpty(clientIdentifierBusinessData)) {
            final Long entityId = clientIdentifierBusinessData.getId();
            final Long attachmentId = clientIdentifierBusinessData.getAttachmentId();
            final CommandProcessingResult result = this.documentWritePlatformService.retrieveAttachment("client_identifiers", entityId, attachmentId);
            clientSignature = result.getResourceIdentifier();
        }

        final LoanBusinessDocData loanBusinessDocData = new LoanBusinessDocData(loanBasicDetails, clientAddressData, loanProductData, clientSignature);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiDocJsonSerializer.serialize(settings, loanBusinessDocData, this.loanDataDocParameters);
    }

}
