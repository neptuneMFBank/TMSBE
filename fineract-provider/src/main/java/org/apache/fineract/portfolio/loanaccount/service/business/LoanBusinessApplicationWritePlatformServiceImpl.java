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

import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.PAYMENT_TYPE_CHEQUE;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.PAYMENT_TYPE_DEDUCTION;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.data.business.CodeBusinessData;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationProperty;
import org.apache.fineract.infrastructure.configuration.domain.GlobalConfigurationRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.business.ConfigurationBusinessDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentBusinessRepositoryWrapper;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentBusinessWritePlatformService;
import org.apache.fineract.infrastructure.entityaccess.FineractEntityAccessConstants;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityAccessType;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelation;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityRelationRepository;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMapping;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityToEntityMappingRepository;
import org.apache.fineract.infrastructure.entityaccess.exception.NotOfficeSpecificProductException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.account.domain.AccountAssociationType;
import org.apache.fineract.portfolio.account.domain.AccountAssociations;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.businessevent.domain.loan.LoanCreatedBusinessEvent;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.exception.CalendarNotFoundException;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepository;
import org.apache.fineract.portfolio.collateralmanagement.service.LoanCollateralAssembler;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.fund.domain.Fund;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.GroupMemberNotFoundInGSIMException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.loanaccount.data.LoanChargeData;
import org.apache.fineract.portfolio.loanaccount.domain.DefaultLoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.GLIMAccountInfoRepository;
import org.apache.fineract.portfolio.loanaccount.domain.GroupLoanIndividualMonitoringAccount;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagementRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanDisbursementDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanLifecycleStateMachine;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallmentRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleTransactionProcessorFactory;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.domain.LoanSummaryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTopupDetails;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.domain.business.LoanInstrumentStatus;
import org.apache.fineract.portfolio.loanaccount.domain.business.LoanOther;
import org.apache.fineract.portfolio.loanaccount.domain.business.LoanOtherRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationDateException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.AprCalculator;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanApplicationTerms;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleModel;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleAssembler;
import org.apache.fineract.portfolio.loanaccount.loanschedule.service.LoanScheduleCalculationPlatformService;
import org.apache.fineract.portfolio.loanaccount.serialization.LoanApplicationTransitionApiJsonValidator;
import org.apache.fineract.portfolio.loanaccount.serialization.business.LoanBusinessApplicationCommandFromApiJsonHelper;
import org.apache.fineract.portfolio.loanaccount.service.GLIMAccountInfoWritePlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanChargeAssembler;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.service.LoanUtilService;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductPaymentTypeConfigData;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductPaymentTypeConfigReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.apache.fineract.portfolio.loanproduct.domain.RecalculationFrequencyType;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.exception.business.LoanProductPaymentTypeConfigNotFoundException;
import org.apache.fineract.portfolio.loanproduct.serialization.LoanProductDataValidator;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.products.data.business.DocumentProductConfigData;
import org.apache.fineract.portfolio.products.service.business.DocumentProductConfigReadPlatformService;
import org.apache.fineract.portfolio.rate.service.RateAssembler;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.data.GroupSavingsIndividualMonitoringAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.service.GSIMReadPlatformService;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import static org.apache.fineract.simplifytech.data.GeneralConstants.holdAmount;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class LoanBusinessApplicationWritePlatformServiceImpl implements LoanBusinessApplicationWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanBusinessApplicationWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;
    private final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator;
    private final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer;
    private final LoanBusinessApplicationCommandFromApiJsonHelper fromApiJsonDeserializer;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final LoanScheduleCalculationPlatformService calculationPlatformService;
    private final LoanAssembler loanAssembler;
    private final ClientRepositoryWrapper clientRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanChargeAssembler loanChargeAssembler;
    private final LoanCollateralAssembler loanCollateralAssembler;
    private final AprCalculator aprCalculator;
    private final AccountNumberGenerator accountNumberGenerator;
    private final LoanSummaryWrapper loanSummaryWrapper;
    private final GroupRepositoryWrapper groupRepository;
    private final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory;
    private final CalendarRepository calendarRepository;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final LoanReadPlatformService loanReadPlatformService;
    private final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final ConfigurationDomainService configurationDomainService;
    private final LoanScheduleAssembler loanScheduleAssembler;
    private final LoanUtilService loanUtilService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    private final GlobalConfigurationRepositoryWrapper globalConfigurationRepository;
    private final FineractEntityToEntityMappingRepository repository;
    private final FineractEntityRelationRepository fineractEntityRelationRepository;
    private final LoanProductReadPlatformService loanProductReadPlatformService;

    private final RateAssembler rateAssembler;
    private final GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService;
    private final GLIMAccountInfoRepository glimRepository;
    private final LoanRepository loanRepository;
    private final GSIMReadPlatformService gsimReadPlatformService;
    private final LoanCollateralManagementRepository loanCollateralManagementRepository;
    private final ClientCollateralManagementRepository clientCollateralManagementRepository;

    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanOtherRepositoryWrapper loanOtherRepositoryWrapper;

    private final Long paymentTypeDeductionId;
    private final Long paymentTypeChequeId;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;
    private final DocumentBusinessWritePlatformService documentWritePlatformService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    private final DocumentBusinessRepositoryWrapper documentBusinessRepositoryWrapper;
    private final DocumentProductConfigReadPlatformService documentProductConfigReadPlatformService;
    private final LoanProductPaymentTypeConfigReadPlatformService loanProductPaymentTypeConfigReadPlatformService;
    private final ConfigurationBusinessDomainService configurationBusinessDomainService;

    @Autowired
    public LoanBusinessApplicationWritePlatformServiceImpl(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,
            final LoanApplicationTransitionApiJsonValidator loanApplicationTransitionApiJsonValidator,
            final LoanBusinessApplicationCommandFromApiJsonHelper fromApiJsonDeserializer,
            final LoanProductDataValidator loanProductCommandFromApiJsonDeserializer, final AprCalculator aprCalculator,
            final LoanAssembler loanAssembler, final LoanChargeAssembler loanChargeAssembler,
            final LoanCollateralAssembler loanCollateralAssembler, final LoanRepositoryWrapper loanRepositoryWrapper,
            final NoteRepository noteRepository, final LoanScheduleCalculationPlatformService calculationPlatformService,
            final ClientRepositoryWrapper clientRepository, final LoanProductRepository loanProductRepository,
            final AccountNumberGenerator accountNumberGenerator, final LoanSummaryWrapper loanSummaryWrapper,
            final GroupRepositoryWrapper groupRepository,
            final LoanRepaymentScheduleTransactionProcessorFactory loanRepaymentScheduleTransactionProcessorFactory,
            final CalendarRepository calendarRepository, final CalendarInstanceRepository calendarInstanceRepository,
            final SavingsAccountAssembler savingsAccountAssembler, final AccountAssociationsRepository accountAssociationsRepository,
            final LoanRepaymentScheduleInstallmentRepository repaymentScheduleInstallmentRepository,
            final LoanReadPlatformService loanReadPlatformService, final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            final BusinessEventNotifierService businessEventNotifierService, final ConfigurationDomainService configurationDomainService,
            final LoanScheduleAssembler loanScheduleAssembler, final LoanUtilService loanUtilService,
            final CalendarReadPlatformService calendarReadPlatformService,
            final GlobalConfigurationRepositoryWrapper globalConfigurationRepository,
            final FineractEntityToEntityMappingRepository repository,
            final FineractEntityRelationRepository fineractEntityRelationRepository,
            final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService,
            final GLIMAccountInfoWritePlatformService glimAccountInfoWritePlatformService, final GLIMAccountInfoRepository glimRepository,
            final LoanRepository loanRepository, final GSIMReadPlatformService gsimReadPlatformService, final RateAssembler rateAssembler,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final LoanCollateralManagementRepository loanCollateralManagementRepository,
            final ClientCollateralManagementRepository clientCollateralManagementRepository,
            final LoanProductPaymentTypeConfigReadPlatformService loanProductPaymentTypeConfigReadPlatformService,
            final DocumentProductConfigReadPlatformService documentProductConfigReadPlatformService,
            final DocumentBusinessRepositoryWrapper documentBusinessRepositoryWrapper,
            final ReadWriteNonCoreDataService readWriteNonCoreDataService,
            final DocumentBusinessWritePlatformService documentWritePlatformService,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApplicationContext applicationContext, final CodeValueRepositoryWrapper codeValueRepository,
            final LoanOtherRepositoryWrapper loanOtherRepositoryWrapper,
            final ConfigurationBusinessDomainService configurationBusinessDomainService) {
        this.context = context;
        this.fromJsonHelper = fromJsonHelper;
        this.loanApplicationTransitionApiJsonValidator = loanApplicationTransitionApiJsonValidator;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.loanProductCommandFromApiJsonDeserializer = loanProductCommandFromApiJsonDeserializer;
        this.aprCalculator = aprCalculator;
        this.loanAssembler = loanAssembler;
        this.loanChargeAssembler = loanChargeAssembler;
        this.loanCollateralAssembler = loanCollateralAssembler;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.noteRepository = noteRepository;
        this.calculationPlatformService = calculationPlatformService;
        this.clientRepository = clientRepository;
        this.loanProductRepository = loanProductRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.loanSummaryWrapper = loanSummaryWrapper;
        this.groupRepository = groupRepository;
        this.loanRepaymentScheduleTransactionProcessorFactory = loanRepaymentScheduleTransactionProcessorFactory;
        this.calendarRepository = calendarRepository;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.savingsAccountAssembler = savingsAccountAssembler;
        this.accountAssociationsRepository = accountAssociationsRepository;
        this.repaymentScheduleInstallmentRepository = repaymentScheduleInstallmentRepository;
        this.loanReadPlatformService = loanReadPlatformService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.configurationDomainService = configurationDomainService;
        this.loanScheduleAssembler = loanScheduleAssembler;
        this.loanUtilService = loanUtilService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.entityDatatableChecksWritePlatformService = entityDatatableChecksWritePlatformService;
        this.globalConfigurationRepository = globalConfigurationRepository;
        this.repository = repository;
        this.fineractEntityRelationRepository = fineractEntityRelationRepository;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.rateAssembler = rateAssembler;
        this.glimAccountInfoWritePlatformService = glimAccountInfoWritePlatformService;
        this.glimRepository = glimRepository;
        this.loanRepository = loanRepository;
        this.gsimReadPlatformService = gsimReadPlatformService;
        this.loanCollateralManagementRepository = loanCollateralManagementRepository;
        this.clientCollateralManagementRepository = clientCollateralManagementRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanOtherRepositoryWrapper = loanOtherRepositoryWrapper;

        Environment environment = applicationContext.getEnvironment();
        this.paymentTypeDeductionId = Long.valueOf(environment.getProperty(PAYMENT_TYPE_DEDUCTION));
        this.paymentTypeChequeId = Long.valueOf(environment.getProperty(PAYMENT_TYPE_CHEQUE));

        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.paymentTypeRepositoryWrapper = paymentTypeRepositoryWrapper;
        this.documentWritePlatformService = documentWritePlatformService;
        this.readWriteNonCoreDataService = readWriteNonCoreDataService;
        this.documentBusinessRepositoryWrapper = documentBusinessRepositoryWrapper;
        this.documentProductConfigReadPlatformService = documentProductConfigReadPlatformService;
        this.loanProductPaymentTypeConfigReadPlatformService = loanProductPaymentTypeConfigReadPlatformService;
        this.configurationBusinessDomainService = configurationBusinessDomainService;
    }

    private LoanLifecycleStateMachine defaultLoanLifecycleStateMachine() {
        final List<LoanStatus> allowedLoanStatuses = Arrays.asList(LoanStatus.values());
        return new DefaultLoanLifecycleStateMachine(allowedLoanStatuses);
    }

    @Transactional
    @Override
    public CommandProcessingResult submitApplication(final JsonCommand command) {
        this.context.authenticatedUser();

        try {
            boolean isMeetingMandatoryForJLGLoans = configurationDomainService.isMeetingMandatoryForJLGLoans();
            final Long productId = this.fromJsonHelper.extractLongNamed("productId", command.parsedJson());
            final LoanProduct loanProduct = this.loanProductRepository.findById(productId)
                    .orElseThrow(() -> new LoanProductNotFoundException(productId));

            final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", command.parsedJson());
            if (clientId != null) {
                Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                officeSpecificLoanProductValidation(productId, client.getOffice().getId());
                loanAgeLimit(client);
            }
            final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", command.parsedJson());
            if (groupId != null) {
                Group group = this.groupRepository.findOneWithNotFoundDetection(groupId);
                officeSpecificLoanProductValidation(productId, group.getOffice().getId());
            }

            this.fromApiJsonDeserializer.validateForCreate(command.json(), isMeetingMandatoryForJLGLoans, loanProduct);

            // Validate If the externalId is already registered
            final String externalId = this.fromJsonHelper.extractStringNamed("externalId", command.parsedJson());
            if (StringUtils.isNotBlank(externalId)) {
                final boolean existByExternalId = this.loanRepositoryWrapper.existLoanByExternalId(externalId);
                if (existByExternalId) {
                    throw new GeneralPlatformDomainRuleException("error.msg.loan.with.externalId.already.used",
                            "Loan with externalId is already registered.");
                }
            }

            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");

            if (loanProduct.useBorrowerCycle()) {
                Integer cycleNumber = 0;
                if (clientId != null) {
                    cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, loanProduct.getId());
                } else if (groupId != null) {
                    cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(),
                            loanProduct.getId());
                }
                this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                        loanProduct, cycleNumber);
            } else {
                this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                        loanProduct);
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            final Loan newLoanApplication = this.loanAssembler.assembleFrom(command);

            checkForProductMixRestrictions(newLoanApplication);

            validateSubmittedOnDate(newLoanApplication);

            final LoanProductRelatedDetail productRelatedDetail = newLoanApplication.repaymentScheduleDetail();

            if (loanProduct.getLoanProductConfigurableAttributes() != null) {
                updateProductRelatedDetails(productRelatedDetail, newLoanApplication);
            }

            this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(newLoanApplication.getTermFrequency(),
                    newLoanApplication.getTermPeriodFrequencyType(), productRelatedDetail.getNumberOfRepayments(),
                    productRelatedDetail.getRepayEvery(), productRelatedDetail.getRepaymentPeriodFrequencyType().getValue(),
                    newLoanApplication);

            if (loanProduct.canUseForTopup() && clientId != null) {
                final Boolean isTopup = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isTopup);
                if (null == isTopup) {
                    newLoanApplication.setIsTopup(false);
                } else {
                    newLoanApplication.setIsTopup(isTopup);
                }

                if (newLoanApplication.isTopup()) {
                    final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                    final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose, clientId);
                    if (loanToClose == null) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                    }
                    if (loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                    }
                    final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                    if (!newLoanApplication.getSubmittedOnDate().isAfter(disbursalDateOfLoanToClose)) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                "Submitted date of this loan application " + newLoanApplication.getSubmittedOnDate()
                                + " should be after the disbursed date of loan to be closed " + disbursalDateOfLoanToClose);
                    }
                    if (!loanToClose.getCurrencyCode().equals(newLoanApplication.getCurrencyCode())) {
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                "loanIdToClose is invalid, Currency code is different.");
                    }
                    final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                    if (newLoanApplication.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)) {
                        throw new GeneralPlatformDomainRuleException(
                                "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                "Disbursal date of this loan application " + newLoanApplication.getDisbursementDate()
                                + " should be after last transaction date of loan to be closed "
                                + lastUserTransactionOnLoanToClose);
                    }
                    BigDecimal loanOutstanding = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(LoanTransactionType.REPAYMENT,
                            loanIdToClose, newLoanApplication.getDisbursementDate()).getAmount();
                    final BigDecimal firstDisbursalAmount = newLoanApplication.getFirstDisbursalAmount();
                    if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
                        throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                    }

                    final LoanTopupDetails topupDetails = new LoanTopupDetails(newLoanApplication, loanIdToClose);
                    newLoanApplication.setTopupLoanDetails(topupDetails);
                }
            }

            this.loanRepositoryWrapper.saveAndFlush(newLoanApplication);

            if (loanProduct.isInterestRecalculationEnabled()) {
                this.fromApiJsonDeserializer.validateLoanForInterestRecalculation(newLoanApplication);
                createAndPersistCalendarInstanceForInterestRecalculation(newLoanApplication);
            }

            // loan account number generation
            String accountNumber = "";
            GroupLoanIndividualMonitoringAccount glimAccount;
            BigDecimal applicationId = BigDecimal.ZERO;
            Boolean isLastChildApplication = false;

            if (newLoanApplication.isAccountNumberRequiresAutoGeneration()) {

                final AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository
                        .findByAccountType(EntityAccountType.LOAN);
                // if application is of GLIM type
                if (newLoanApplication.getLoanType() == 4) {
                    Group group = this.groupRepository.findOneWithNotFoundDetection(groupId);

                    // GLIM specific parameters
                    if (command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("applicationId") != null) {
                        applicationId = command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("applicationId");
                    }

                    if (command.booleanObjectValueOfParameterNamed("lastApplication") != null) {
                        isLastChildApplication = command.booleanPrimitiveValueOfParameterNamed("lastApplication");
                    }

                    if (command.booleanObjectValueOfParameterNamed("isParentAccount") != null) {

                        // empty table check
                        if (glimRepository.count() != 0) {
                            // **************Parent-Not an empty
                            // table********************
                            accountNumber = this.accountNumberGenerator.generate(newLoanApplication, accountNumberFormat);
                            newLoanApplication.updateAccountNo(accountNumber + "1");
                            glimAccountInfoWritePlatformService.addGLIMAccountInfo(accountNumber, group,
                                    command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("totalLoan"), Long.valueOf(1), true,
                                    LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
                            newLoanApplication.setGlim(glimRepository.findOneByAccountNumber(accountNumber));
                            this.loanRepositoryWrapper.save(newLoanApplication);

                        } else {
                            // ************** Parent-empty
                            // table********************

                            accountNumber = this.accountNumberGenerator.generate(newLoanApplication, accountNumberFormat);
                            newLoanApplication.updateAccountNo(accountNumber + "1");
                            glimAccountInfoWritePlatformService.addGLIMAccountInfo(accountNumber, group,
                                    command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("totalLoan"), Long.valueOf(1), true,
                                    LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
                            newLoanApplication.setGlim(glimRepository.findOneByAccountNumber(accountNumber));
                            this.loanRepositoryWrapper.save(newLoanApplication);

                        }

                    } else {

                        if (glimRepository.count() != 0) {
                            // Child-Not an empty table

                            glimAccount = glimRepository.findOneByIsAcceptingChildAndApplicationId(true, applicationId);
                            accountNumber = glimAccount.getAccountNumber() + (glimAccount.getChildAccountsCount() + 1);
                            newLoanApplication.updateAccountNo(accountNumber);
                            this.glimAccountInfoWritePlatformService.incrementChildAccountCount(glimAccount);
                            newLoanApplication.setGlim(glimAccount);
                            this.loanRepositoryWrapper.save(newLoanApplication);

                        } else {
                            // **************Child-empty
                            // table********************
                            // if the glim info is empty set the current account
                            // as parent
                            accountNumber = this.accountNumberGenerator.generate(newLoanApplication, accountNumberFormat);
                            newLoanApplication.updateAccountNo(accountNumber + "1");
                            glimAccountInfoWritePlatformService.addGLIMAccountInfo(accountNumber, group,
                                    command.bigDecimalValueOfParameterNamedDefaultToNullIfZero("totalLoan"), Long.valueOf(1), true,
                                    LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue(), applicationId);
                            newLoanApplication.setGlim(glimRepository.findOneByAccountNumber(accountNumber));
                            this.loanRepositoryWrapper.save(newLoanApplication);

                        }

                        // reset in cases of last child application of glim
                        if (isLastChildApplication) {
                            this.glimAccountInfoWritePlatformService
                                    .resetIsAcceptingChild(glimRepository.findOneByIsAcceptingChildAndApplicationId(true, applicationId));
                        }

                    }
                } else { // for applications other than GLIM
                    newLoanApplication.updateAccountNo(this.accountNumberGenerator.generate(newLoanApplication, accountNumberFormat));
                    this.loanRepositoryWrapper.saveAndFlush(newLoanApplication);
                }
            }

            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            if (StringUtils.isNotBlank(submittedOnNote)) {
                final Note note = Note.loanNote(newLoanApplication, submittedOnNote);
                this.noteRepository.save(note);
            }

            // Save calendar instance
            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            Calendar calendar = null;

            if (calendarId != null && calendarId != 0) {
                calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));

                final CalendarInstance calendarInstance = new CalendarInstance(calendar, newLoanApplication.getId(),
                        CalendarEntityType.LOANS.getValue());
                this.calendarInstanceRepository.save(calendarInstance);
            } else {
                final LoanApplicationTerms loanApplicationTerms = this.loanScheduleAssembler.assembleLoanTerms(command.parsedJson());
                final Integer repaymentFrequencyNthDayType = command.integerValueOfParameterNamed("repaymentFrequencyNthDayType");
                if (loanApplicationTerms.getRepaymentPeriodFrequencyType() == PeriodFrequencyType.MONTHS
                        && repaymentFrequencyNthDayType != null) {
                    final String title = "loan_schedule_" + newLoanApplication.getId();
                    LocalDate calendarStartDate = loanApplicationTerms.getRepaymentsStartingFromLocalDate();
                    if (calendarStartDate == null) {
                        calendarStartDate = loanApplicationTerms.getExpectedDisbursementDate();
                    }
                    final CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.MONTHLY;
                    final Integer frequency = loanApplicationTerms.getRepaymentEvery();
                    final Integer repeatsOnDay = loanApplicationTerms.getWeekDayType().getValue();
                    final Integer repeatsOnNthDayOfMonth = loanApplicationTerms.getNthDay();
                    final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                    final Calendar loanCalendar = Calendar.createRepeatingCalendar(title, calendarStartDate,
                            CalendarType.COLLECTION.getValue(), calendarFrequencyType, frequency, repeatsOnDay, repeatsOnNthDayOfMonth);
                    this.calendarRepository.save(loanCalendar);
                    final CalendarInstance calendarInstance = CalendarInstance.from(loanCalendar, newLoanApplication.getId(),
                            calendarEntityType);
                    this.calendarInstanceRepository.save(calendarInstance);
                }
            }

            // Save linked account information
            SavingsAccount savingsAccount;
            final boolean backdatedTxnsAllowedTill = false;
            AccountAssociations accountAssociations;
            final Long savingsAccountId = command.longValueOfParameterNamed("linkAccountId");
            if (savingsAccountId != null) {
                if (newLoanApplication.getLoanType() == 4) {

                    List<GroupSavingsIndividualMonitoringAccountData> childSavings = (List<GroupSavingsIndividualMonitoringAccountData>) gsimReadPlatformService
                            .findGSIMAccountsByGSIMId(savingsAccountId);
                    // List<SavingsAccountSummaryData>
                    // childSavings=gsimAccount.getChildGSIMAccounts();
                    List<BigDecimal> gsimClientMembers = new ArrayList<BigDecimal>();
                    Map<BigDecimal, BigDecimal> clientAccountMappings = new HashMap<>();
                    for (GroupSavingsIndividualMonitoringAccountData childSaving : childSavings) {
                        gsimClientMembers.add(childSaving.getClientId());
                        clientAccountMappings.put(childSaving.getClientId(), childSaving.getChildAccountId());

                    }

                    if (gsimClientMembers.contains(BigDecimal.valueOf(newLoanApplication.getClientId()))) {
                        savingsAccount = this.savingsAccountAssembler.assembleFrom(
                                clientAccountMappings.get(BigDecimal.valueOf(newLoanApplication.getClientId())).longValue(),
                                backdatedTxnsAllowedTill);

                        this.fromApiJsonDeserializer.validatelinkedSavingsAccount(savingsAccount, newLoanApplication);
                        boolean isActive = true;
                        accountAssociations = AccountAssociations.associateSavingsAccount(newLoanApplication, savingsAccount,
                                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
                        this.accountAssociationsRepository.save(accountAssociations);

                    } else {
                        throw new GroupMemberNotFoundInGSIMException(newLoanApplication.getClientId());
                    }
                } else {

                    savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsAccountId, backdatedTxnsAllowedTill);
                    this.fromApiJsonDeserializer.validatelinkedSavingsAccount(savingsAccount, newLoanApplication);
                    boolean isActive = true;
                    accountAssociations = AccountAssociations.associateSavingsAccount(newLoanApplication, savingsAccount,
                            AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
                    this.accountAssociationsRepository.save(accountAssociations);

                }
            }
            if (command.parameterExists(LoanApiConstants.datatables)) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getCode().longValue(),
                        EntityTables.LOAN.getName(), newLoanApplication.getId(), newLoanApplication.productId(),
                        command.arrayOfParameterNamed(LoanApiConstants.datatables));
            }

            loanRepositoryWrapper.flush();

            this.entityDatatableChecksWritePlatformService.runTheCheckForProduct(newLoanApplication.getId(), EntityTables.LOAN.getName(),
                    StatusEnum.CREATE.getCode().longValue(), EntityTables.LOAN.getForeignKeyColumnNameOnDatatable(),
                    newLoanApplication.productId());

            businessEventNotifierService.notifyPostBusinessEvent(new LoanCreatedBusinessEvent(newLoanApplication));

            loanOtherProcess(command, newLoanApplication, false, null);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(newLoanApplication.getId()) //
                    .withOfficeId(newLoanApplication.getOfficeId()) //
                    .withClientId(newLoanApplication.getClientId()) //
                    .withGroupId(newLoanApplication.getGroupId()) //
                    .withLoanId(newLoanApplication.getId()).withGlimId(newLoanApplication.getGlimId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    protected void loanAgeLimit(Client client) {
        final Integer legalForm = client.getLegalForm();
        if (legalForm != null && LegalForm.fromInt(legalForm).isPerson()) {
            final Integer limitLoanAge = this.configurationBusinessDomainService.isLimitLoanAge();
            //check client age is with age loan limit if enabled
            if (limitLoanAge != null) {
                final LocalDate localDateOfBirth = client.dateOfBirthLocalDate();
                if (localDateOfBirth != null) {
                    LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
                    Period period = Period.between(localDateOfBirth, today);
                    final Integer currentYear = period.getYears();
                    if (currentYear < limitLoanAge) {
                        throw new LoanApplicationDateException("loan.client.dateOfBirth", "Date of birth does not meet current loan age limit.");
                    }
                }
            }
        }
    }

    protected void loanOtherProcess(final JsonCommand command, final Loan loanApplication, final boolean isUpdate,
            final Map<String, Object> changes) {
        LoanOther loanOther;
        CodeValue activationChannel = null;
        LOG.info("command: {}", command.json());
        final Long loanId = loanApplication.getId();
        Long activationChannelId = null;
        final String activationChannelIdParam = LoanBusinessApiConstants.activationChannelIdParam;
        if (this.fromJsonHelper.parameterExists(activationChannelIdParam, command.parsedJson())) {
            activationChannelId = this.fromJsonHelper.extractLongNamed(activationChannelIdParam, command.parsedJson());
            LOG.info("command: activationChannelId {}:", activationChannelId);
        }
        if (isUpdate) {
            LoanOther exitingLoanOther = this.loanOtherRepositoryWrapper.findOneByLoanId(loanId);

            Long existingLoanActivationChannelId = null;
            if (exitingLoanOther != null) {
                existingLoanActivationChannelId = exitingLoanOther.getActivationChannel().getId();
            } else {
                exitingLoanOther = LoanOther.instance(activationChannel, loanApplication);
            }
            if (exitingLoanOther.getActivationChannel() == null
                    && command.isChangeInLongParameterNamed(activationChannelIdParam, existingLoanActivationChannelId)) {
                // only update if ActivationChannel is empty
                final Long newValue = command.longValueOfParameterNamed(activationChannelIdParam);
                changes.put(LoanBusinessApiConstants.activationChannelNameParam, newValue);
                activationChannel = this.loanAssembler.findCodeValueByIdIfProvided(activationChannelId);
                exitingLoanOther.setActivationChannel(activationChannel);
            }
            LOG.info("command: 2");
            loanOther = exitingLoanOther;
        } else {
            LOG.info("command: 1");
            activationChannel = this.loanAssembler.findCodeValueByIdIfProvided(activationChannelId);
            loanOther = LoanOther.instance(activationChannel, loanApplication);
        }
        LOG.info("command: 3");
        this.loanOtherRepositoryWrapper.saveAndFlush(loanOther);
    }

    public void checkForProductMixRestrictions(final Loan loan) {

        final List<Long> activeLoansLoanProductIds;
        final Long productId = loan.loanProduct().getId();

        if (loan.isGroupLoan()) {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByGroup(loan.getGroupId(),
                    LoanStatus.ACTIVE.getValue());
        } else {
            activeLoansLoanProductIds = this.loanRepositoryWrapper.findActiveLoansLoanProductIdsByClient(loan.getClientId(),
                    LoanStatus.ACTIVE.getValue());
        }
        checkForProductMixRestrictions(activeLoansLoanProductIds, productId, loan.loanProduct().productName());
    }

    private void checkForProductMixRestrictions(final List<Long> activeLoansLoanProductIds, final Long productId,
            final String productName) {

        if (!CollectionUtils.isEmpty(activeLoansLoanProductIds)) {
            final Collection<LoanProductData> restrictedPrdouctsList = this.loanProductReadPlatformService
                    .retrieveRestrictedProductsForMix(productId);
            for (final LoanProductData restrictedProduct : restrictedPrdouctsList) {
                if (activeLoansLoanProductIds.contains(restrictedProduct.getId())) {
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.loan.applied.or.to.be.disbursed.can.not.co-exist.with.the.loan.already.active.to.this.client",
                            "This loan could not be applied/disbursed as the loan and `" + restrictedProduct
                            + "` are not allowed to co-exist");
                }
            }
        }
    }

    private void updateProductRelatedDetails(LoanProductRelatedDetail productRelatedDetail, Loan loan) {
        final Boolean amortization = loan.loanProduct().getLoanProductConfigurableAttributes().getAmortizationBoolean();
        final Boolean arrearsTolerance = loan.loanProduct().getLoanProductConfigurableAttributes().getArrearsToleranceBoolean();
        final Boolean graceOnArrearsAging = loan.loanProduct().getLoanProductConfigurableAttributes().getGraceOnArrearsAgingBoolean();
        final Boolean interestCalcPeriod = loan.loanProduct().getLoanProductConfigurableAttributes().getInterestCalcPeriodBoolean();
        final Boolean interestMethod = loan.loanProduct().getLoanProductConfigurableAttributes().getInterestMethodBoolean();
        final Boolean graceOnPrincipalAndInterestPayment = loan.loanProduct().getLoanProductConfigurableAttributes()
                .getGraceOnPrincipalAndInterestPaymentBoolean();
        final Boolean repaymentEvery = loan.loanProduct().getLoanProductConfigurableAttributes().getRepaymentEveryBoolean();
        final Boolean transactionProcessingStrategy = loan.loanProduct().getLoanProductConfigurableAttributes()
                .getTransactionProcessingStrategyBoolean();

        if (!amortization) {
            productRelatedDetail.setAmortizationMethod(loan.loanProduct().getLoanProductRelatedDetail().getAmortizationMethod());
        }
        if (!arrearsTolerance) {
            productRelatedDetail.setInArrearsTolerance(loan.loanProduct().getLoanProductRelatedDetail().getArrearsTolerance());
        }
        if (!graceOnArrearsAging) {
            productRelatedDetail.setGraceOnArrearsAgeing(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnArrearsAgeing());
        }
        if (!interestCalcPeriod) {
            productRelatedDetail.setInterestCalculationPeriodMethod(
                    loan.loanProduct().getLoanProductRelatedDetail().getInterestCalculationPeriodMethod());
        }
        if (!interestMethod) {
            productRelatedDetail.setInterestMethod(loan.loanProduct().getLoanProductRelatedDetail().getInterestMethod());
        }
        if (!graceOnPrincipalAndInterestPayment) {
            productRelatedDetail.setGraceOnInterestPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnInterestPayment());
            productRelatedDetail.setGraceOnPrincipalPayment(loan.loanProduct().getLoanProductRelatedDetail().getGraceOnPrincipalPayment());
        }
        if (!repaymentEvery) {
            productRelatedDetail.setRepayEvery(loan.loanProduct().getLoanProductRelatedDetail().getRepayEvery());
        }
        if (!transactionProcessingStrategy) {
            loan.updateTransactionProcessingStrategy(loan.loanProduct().getRepaymentStrategy());
        }
    }

    private void createAndPersistCalendarInstanceForInterestRecalculation(final Loan loan) {

        LocalDate calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
        Integer repeatsOnDay = null;
        final RecalculationFrequencyType recalculationFrequencyType = loan.loanInterestRecalculationDetails().getRestFrequencyType();
        Integer recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyOnDay();
        if (recalculationFrequencyNthDay == null) {
            recalculationFrequencyNthDay = loan.loanInterestRecalculationDetails().getRestFrequencyNthDay();
            repeatsOnDay = loan.loanInterestRecalculationDetails().getRestFrequencyWeekday();
        }

        Integer frequency = loan.loanInterestRecalculationDetails().getRestInterval();
        CalendarEntityType calendarEntityType = CalendarEntityType.LOAN_RECALCULATION_REST_DETAIL;
        final String title = "loan_recalculation_detail_" + loan.loanInterestRecalculationDetails().getId();

        createCalendar(loan, calendarStartDate, recalculationFrequencyNthDay, repeatsOnDay, recalculationFrequencyType, frequency,
                calendarEntityType, title);

        if (loan.loanInterestRecalculationDetails().getInterestRecalculationCompoundingMethod().isCompoundingEnabled()) {
            LocalDate compoundingStartDate = loan.getExpectedDisbursedOnLocalDate();
            Integer compoundingRepeatsOnDay = null;
            final RecalculationFrequencyType recalculationCompoundingFrequencyType = loan.loanInterestRecalculationDetails()
                    .getCompoundingFrequencyType();
            Integer recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyOnDay();
            if (recalculationCompoundingFrequencyNthDay == null) {
                recalculationCompoundingFrequencyNthDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyNthDay();
                compoundingRepeatsOnDay = loan.loanInterestRecalculationDetails().getCompoundingFrequencyWeekday();
            }

            Integer compoundingFrequency = loan.loanInterestRecalculationDetails().getCompoundingInterval();
            CalendarEntityType compoundingCalendarEntityType = CalendarEntityType.LOAN_RECALCULATION_COMPOUNDING_DETAIL;
            final String compoundingCalendarTitle = "loan_recalculation_detail_compounding_frequency"
                    + loan.loanInterestRecalculationDetails().getId();

            createCalendar(loan, compoundingStartDate, recalculationCompoundingFrequencyNthDay, compoundingRepeatsOnDay,
                    recalculationCompoundingFrequencyType, compoundingFrequency, compoundingCalendarEntityType, compoundingCalendarTitle);
        }

    }

    private void createCalendar(final Loan loan, LocalDate calendarStartDate, Integer recalculationFrequencyNthDay,
            final Integer repeatsOnDay, final RecalculationFrequencyType recalculationFrequencyType, Integer frequency,
            CalendarEntityType calendarEntityType, final String title) {
        CalendarFrequencyType calendarFrequencyType = CalendarFrequencyType.INVALID;
        Integer updatedRepeatsOnDay = repeatsOnDay;
        switch (recalculationFrequencyType) {
            case DAILY:
                calendarFrequencyType = CalendarFrequencyType.DAILY;
                break;
            case MONTHLY:
                calendarFrequencyType = CalendarFrequencyType.MONTHLY;
                break;
            case SAME_AS_REPAYMENT_PERIOD:
                frequency = loan.repaymentScheduleDetail().getRepayEvery();
                calendarFrequencyType = CalendarFrequencyType.from(loan.repaymentScheduleDetail().getRepaymentPeriodFrequencyType());
                calendarStartDate = loan.getExpectedDisbursedOnLocalDate();
                if (updatedRepeatsOnDay == null) {
                    updatedRepeatsOnDay = calendarStartDate.get(ChronoField.DAY_OF_WEEK);
                }
                break;
            case WEEKLY:
                calendarFrequencyType = CalendarFrequencyType.WEEKLY;
                break;
            default:
                break;
        }

        final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                calendarFrequencyType, frequency, updatedRepeatsOnDay, recalculationFrequencyNthDay);
        final CalendarInstance calendarInstance = CalendarInstance.from(calendar, loan.loanInterestRecalculationDetails().getId(),
                calendarEntityType.getValue());
        this.calendarInstanceRepository.save(calendarInstance);
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyApplication(final Long loanId, final JsonCommand command) {

        try {
            AppUser currentUser = getAppUserIfPresent();
            final Loan existingLoanApplication = retrieveLoanBy(loanId);
            if (!existingLoanApplication.isSubmittedAndPendingApproval()) {
                throw new LoanApplicationNotInSubmittedAndPendingApprovalStateCannotBeModified(loanId);
            }

            final String productIdParamName = "productId";
            LoanProduct newLoanProduct = null;
            if (command.isChangeInLongParameterNamed(productIdParamName, existingLoanApplication.loanProduct().getId())) {
                final Long productId = command.longValueOfParameterNamed(productIdParamName);
                newLoanProduct = this.loanProductRepository.findById(productId)
                        .orElseThrow(() -> new LoanProductNotFoundException(productId));
            }

            LoanProduct loanProductForValidations = newLoanProduct == null ? existingLoanApplication.loanProduct() : newLoanProduct;

            this.fromApiJsonDeserializer.validateForModify(command.json(), loanProductForValidations, existingLoanApplication);

            checkClientOrGroupActive(existingLoanApplication);

            final Set<LoanCharge> existingCharges = existingLoanApplication.charges();
            Map<Long, LoanChargeData> chargesMap = new HashMap<>();
            for (LoanCharge charge : existingCharges) {
                LoanChargeData chargeData = new LoanChargeData(charge.getId(), charge.getDueLocalDate(), charge.amountOrPercentage());
                chargesMap.put(charge.getId(), chargeData);
            }
            List<LoanDisbursementDetails> disbursementDetails = this.loanUtilService
                    .fetchDisbursementData(command.parsedJson().getAsJsonObject());

            /**
             * Stores all charges which are passed in during modify loan
             * application
             *
             */
            final Set<LoanCharge> possiblyModifedLoanCharges = this.loanChargeAssembler.fromParsedJson(command.parsedJson(),
                    disbursementDetails);
            /**
             * Boolean determines if any charge has been modified *
             */
            boolean isChargeModified = false;

            Set<Charge> newTrancheChages = this.loanChargeAssembler.getNewLoanTrancheCharges(command.parsedJson());
            for (Charge charge : newTrancheChages) {
                existingLoanApplication.addTrancheLoanCharge(charge);
            }

            /**
             * If there are any charges already present, which are now not
             * passed in as a part of the request, deem the charges as modified
             *
             */
            if (!possiblyModifedLoanCharges.isEmpty()) {
                if (!possiblyModifedLoanCharges.containsAll(existingCharges)) {
                    isChargeModified = true;
                }
            }

            /**
             * If any new charges are added or values of existing charges are
             * modified
             *
             */
            for (LoanCharge loanCharge : possiblyModifedLoanCharges) {
                if (loanCharge.getId() == null) {
                    isChargeModified = true;
                } else {
                    LoanChargeData chargeData = chargesMap.get(loanCharge.getId());
                    if (loanCharge.amountOrPercentage().compareTo(chargeData.amountOrPercentage()) != 0
                            || (loanCharge.isSpecifiedDueDate() && !loanCharge.getDueLocalDate().equals(chargeData.getDueDate()))) {
                        isChargeModified = true;
                    }
                }
            }

            Set<LoanCollateralManagement> possiblyModifedLoanCollateralItems = null;

            if (command.parameterExists("loanType")) {
                final String loanTypeStr = command.stringValueOfParameterNamed("loanType");
                final AccountType loanType = AccountType.fromName(loanTypeStr);

                if (!StringUtils.isBlank(loanTypeStr) && loanType.isIndividualAccount()) {
                    possiblyModifedLoanCollateralItems = this.loanCollateralAssembler.fromParsedJson(command.parsedJson());
                }
            }

            final Map<String, Object> changes = existingLoanApplication.loanApplicationModification(command, possiblyModifedLoanCharges,
                    possiblyModifedLoanCollateralItems, this.aprCalculator, isChargeModified, loanProductForValidations);

            if (changes.containsKey("expectedDisbursementDate")) {
                this.loanAssembler.validateExpectedDisbursementForHolidayAndNonWorkingDay(existingLoanApplication);
            }

            final String clientIdParamName = "clientId";
            if (changes.containsKey(clientIdParamName)) {
                final Long clientId = command.longValueOfParameterNamed(clientIdParamName);
                final Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                if (client.isNotActive()) {
                    throw new ClientNotActiveException(clientId);
                }
                loanAgeLimit(client);

                existingLoanApplication.updateClient(client);
            }

            final String groupIdParamName = "groupId";
            if (changes.containsKey(groupIdParamName)) {
                final Long groupId = command.longValueOfParameterNamed(groupIdParamName);
                final Group group = this.groupRepository.findOneWithNotFoundDetection(groupId);
                if (group.isNotActive()) {
                    throw new GroupNotActiveException(groupId);
                }

                existingLoanApplication.updateGroup(group);
            }

            if (newLoanProduct != null) {
                existingLoanApplication.updateLoanProduct(newLoanProduct);
                if (!changes.containsKey("interestRateFrequencyType")) {
                    existingLoanApplication.updateInterestRateFrequencyType();
                }
                final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
                final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan");
                if (newLoanProduct.useBorrowerCycle()) {
                    final Long clientId = this.fromJsonHelper.extractLongNamed("clientId", command.parsedJson());
                    final Long groupId = this.fromJsonHelper.extractLongNamed("groupId", command.parsedJson());
                    Integer cycleNumber = 0;
                    if (clientId != null) {
                        cycleNumber = this.loanReadPlatformService.retriveLoanCounter(clientId, newLoanProduct.getId());
                    } else if (groupId != null) {
                        cycleNumber = this.loanReadPlatformService.retriveLoanCounter(groupId, AccountType.GROUP.getValue(),
                                newLoanProduct.getId());
                    }
                    this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                            newLoanProduct, cycleNumber);
                } else {
                    this.loanProductCommandFromApiJsonDeserializer.validateMinMaxConstraints(command.parsedJson(), baseDataValidator,
                            newLoanProduct);
                }
                if (newLoanProduct.isLinkedToFloatingInterestRate()) {
                    existingLoanApplication.getLoanProductRelatedDetail().updateForFloatingInterestRates();
                } else {
                    existingLoanApplication.setInterestRateDifferential(null);
                    existingLoanApplication.setIsFloatingInterestRate(null);
                }
                if (!dataValidationErrors.isEmpty()) {
                    throw new PlatformApiDataValidationException(dataValidationErrors);
                }
            }

            existingLoanApplication.updateIsInterestRecalculationEnabled();
            validateSubmittedOnDate(existingLoanApplication);

            final LoanProductRelatedDetail productRelatedDetail = existingLoanApplication.repaymentScheduleDetail();
            if (existingLoanApplication.loanProduct().getLoanProductConfigurableAttributes() != null) {
                updateProductRelatedDetails(productRelatedDetail, existingLoanApplication);
            }

            if (existingLoanApplication.getLoanProduct().canUseForTopup() && existingLoanApplication.getClientId() != null) {
                final Boolean isTopup = command.booleanObjectValueOfParameterNamed(LoanApiConstants.isTopup);
                if (command.isChangeInBooleanParameterNamed(LoanApiConstants.isTopup, existingLoanApplication.isTopup())) {
                    existingLoanApplication.setIsTopup(isTopup);
                    changes.put(LoanApiConstants.isTopup, isTopup);
                }

                if (existingLoanApplication.isTopup()) {
                    final Long loanIdToClose = command.longValueOfParameterNamed(LoanApiConstants.loanIdToClose);
                    LoanTopupDetails existingLoanTopupDetails = existingLoanApplication.getTopupLoanDetails();
                    if (existingLoanTopupDetails == null
                            || (existingLoanTopupDetails != null && !existingLoanTopupDetails.getLoanIdToClose().equals(loanIdToClose))
                            || changes.containsKey("submittedOnDate") || changes.containsKey("expectedDisbursementDate")
                            || changes.containsKey("principal") || changes.containsKey(LoanApiConstants.disbursementDataParameterName)) {
                        Long existingLoanIdToClose = null;
                        if (existingLoanTopupDetails != null) {
                            existingLoanIdToClose = existingLoanTopupDetails.getLoanIdToClose();
                        }
                        final Loan loanToClose = this.loanRepositoryWrapper.findNonClosedLoanThatBelongsToClient(loanIdToClose,
                                existingLoanApplication.getClientId());
                        if (loanToClose == null) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.loanIdToClose.no.active.loan.associated.to.client.found",
                                    "loanIdToClose is invalid, No Active Loan associated with the given Client ID found.");
                        }
                        if (loanToClose.isMultiDisburmentLoan() && !loanToClose.isInterestRecalculationEnabledForProduct()) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.topup.on.multi.tranche.loan.without.interest.recalculation.not.supported",
                                    "Topup on loan with multi-tranche disbursal and without interest recalculation is not supported.");
                        }
                        final LocalDate disbursalDateOfLoanToClose = loanToClose.getDisbursementDate();
                        if (!existingLoanApplication.getSubmittedOnDate().isAfter(disbursalDateOfLoanToClose)) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.submitted.date.should.be.after.topup.loan.disbursal.date",
                                    "Submitted date of this loan application " + existingLoanApplication.getSubmittedOnDate()
                                    + " should be after the disbursed date of loan to be closed " + disbursalDateOfLoanToClose);
                        }
                        if (!loanToClose.getCurrencyCode().equals(existingLoanApplication.getCurrencyCode())) {
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.to.be.closed.has.different.currency",
                                    "loanIdToClose is invalid, Currency code is different.");
                        }
                        final LocalDate lastUserTransactionOnLoanToClose = loanToClose.getLastUserTransactionDate();
                        if (existingLoanApplication.getDisbursementDate().isBefore(lastUserTransactionOnLoanToClose)) {
                            throw new GeneralPlatformDomainRuleException(
                                    "error.msg.loan.disbursal.date.should.be.after.last.transaction.date.of.loan.to.be.closed",
                                    "Disbursal date of this loan application " + existingLoanApplication.getDisbursementDate()
                                    + " should be after last transaction date of loan to be closed "
                                    + lastUserTransactionOnLoanToClose);
                        }
                        BigDecimal loanOutstanding = this.loanReadPlatformService.retrieveLoanPrePaymentTemplate(
                                LoanTransactionType.REPAYMENT, loanIdToClose, existingLoanApplication.getDisbursementDate()).getAmount();
                        final BigDecimal firstDisbursalAmount = existingLoanApplication.getFirstDisbursalAmount();
                        if (loanOutstanding.compareTo(firstDisbursalAmount) > 0) {
                            throw new GeneralPlatformDomainRuleException("error.msg.loan.amount.less.than.outstanding.of.loan.to.be.closed",
                                    "Topup loan amount should be greater than outstanding amount of loan to be closed.");
                        }

                        if (!existingLoanIdToClose.equals(loanIdToClose)) {
                            final LoanTopupDetails topupDetails = new LoanTopupDetails(existingLoanApplication, loanIdToClose);
                            existingLoanApplication.setTopupLoanDetails(topupDetails);
                            changes.put(LoanApiConstants.loanIdToClose, loanIdToClose);
                        }
                    }
                } else {
                    existingLoanApplication.setTopupLoanDetails(null);
                }
            } else {
                if (existingLoanApplication.isTopup()) {
                    existingLoanApplication.setIsTopup(false);
                    existingLoanApplication.setTopupLoanDetails(null);
                    changes.put(LoanApiConstants.isTopup, false);
                }
            }

            final String fundIdParamName = "fundId";
            if (changes.containsKey(fundIdParamName)) {
                final Long fundId = command.longValueOfParameterNamed(fundIdParamName);
                final Fund fund = this.loanAssembler.findFundByIdIfProvided(fundId);

                existingLoanApplication.updateFund(fund);
            }

            final String loanPurposeIdParamName = "loanPurposeId";
            if (changes.containsKey(loanPurposeIdParamName)) {
                final Long loanPurposeId = command.longValueOfParameterNamed(loanPurposeIdParamName);
                final CodeValue loanPurpose = this.loanAssembler.findCodeValueByIdIfProvided(loanPurposeId);
                existingLoanApplication.updateLoanPurpose(loanPurpose);
            }

            final String loanOfficerIdParamName = "loanOfficerId";
            if (changes.containsKey(loanOfficerIdParamName)) {
                final Long loanOfficerId = command.longValueOfParameterNamed(loanOfficerIdParamName);
                final Staff newValue = this.loanAssembler.findLoanOfficerByIdIfProvided(loanOfficerId);
                existingLoanApplication.updateLoanOfficerOnLoanApplication(newValue);
            }

            final String strategyIdParamName = "transactionProcessingStrategyId";
            if (changes.containsKey(strategyIdParamName)) {
                final Long strategyId = command.longValueOfParameterNamed(strategyIdParamName);
                final LoanTransactionProcessingStrategy strategy = this.loanAssembler.findStrategyByIdIfProvided(strategyId);

                existingLoanApplication.updateTransactionProcessingStrategy(strategy);
            }

            /**
             * TODO: Allow other loan types if needed.
             */
            if (command.parameterExists("loanType")) {
                final String loanTypeStr = command.stringValueOfParameterNamed("loanType");
                final AccountType loanType = AccountType.fromName(loanTypeStr);

                if (!StringUtils.isBlank(loanTypeStr) && loanType.isIndividualAccount()) {
                    final String collateralParamName = "collateral";
                    if (changes.containsKey(collateralParamName)) {
                        existingLoanApplication.updateLoanCollateral(possiblyModifedLoanCollateralItems);
                    }
                }
            }

            final String chargesParamName = "charges";
            if (changes.containsKey(chargesParamName)) {
                existingLoanApplication.updateLoanCharges(possiblyModifedLoanCharges);
            }

            if (changes.containsKey("recalculateLoanSchedule")) {
                changes.remove("recalculateLoanSchedule");

                final JsonElement parsedQuery = this.fromJsonHelper.parse(command.json());
                final JsonQuery query = JsonQuery.from(command.json(), parsedQuery, this.fromJsonHelper);

                final LoanScheduleModel loanSchedule = this.calculationPlatformService.calculateLoanSchedule(query, false);
                existingLoanApplication.updateLoanSchedule(loanSchedule);
                existingLoanApplication.recalculateAllCharges();
            }

            // Changes to modify loan rates.
            if (command.hasParameter(LoanProductConstants.RATES_PARAM_NAME)) {
                existingLoanApplication.updateLoanRates(rateAssembler.fromParsedJson(command.parsedJson()));
            }

            this.fromApiJsonDeserializer.validateLoanTermAndRepaidEveryValues(existingLoanApplication.getTermFrequency(),
                    existingLoanApplication.getTermPeriodFrequencyType(), productRelatedDetail.getNumberOfRepayments(),
                    productRelatedDetail.getRepayEvery(), productRelatedDetail.getRepaymentPeriodFrequencyType().getValue(),
                    existingLoanApplication);

            saveAndFlushLoanWithDataIntegrityViolationChecks(existingLoanApplication);

            final String submittedOnNote = command.stringValueOfParameterNamed("submittedOnNote");
            if (StringUtils.isNotBlank(submittedOnNote)) {
                final Note note = Note.loanNote(existingLoanApplication, submittedOnNote);
                this.noteRepository.save(note);
            }

            final Long calendarId = command.longValueOfParameterNamed("calendarId");
            Calendar calendar = null;
            if (calendarId != null && calendarId != 0) {
                calendar = this.calendarRepository.findById(calendarId).orElseThrow(() -> new CalendarNotFoundException(calendarId));
            }

            final List<CalendarInstance> ciList = (List<CalendarInstance>) this.calendarInstanceRepository
                    .findByEntityIdAndEntityTypeId(loanId, CalendarEntityType.LOANS.getValue());
            if (calendar != null) {

                // For loans, allow to attach only one calendar instance per
                // loan
                if (ciList != null && !ciList.isEmpty()) {
                    final CalendarInstance calendarInstance = ciList.get(0);
                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                            calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(),
                            CalendarEntityType.LOANS.getValue().longValue());
                    if (isCalendarAssociatedWithEntity && calendarId == null) {
                        this.calendarRepository.delete(calendarInstance.getCalendar());
                    }
                    if (!calendarInstance.getCalendar().getId().equals(calendar.getId())) {
                        calendarInstance.updateCalendar(calendar);
                        this.calendarInstanceRepository.saveAndFlush(calendarInstance);
                    }
                } else {
                    // attaching new calendar
                    final CalendarInstance calendarInstance = new CalendarInstance(calendar, existingLoanApplication.getId(),
                            CalendarEntityType.LOANS.getValue());
                    this.calendarInstanceRepository.save(calendarInstance);
                }

            } else {
                if (ciList != null && !ciList.isEmpty()) {
                    final CalendarInstance existingCalendarInstance = ciList.get(0);
                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                            existingCalendarInstance.getEntityId(), existingCalendarInstance.getCalendar().getId(),
                            CalendarEntityType.GROUPS.getValue().longValue());
                    if (isCalendarAssociatedWithEntity) {
                        this.calendarInstanceRepository.delete(existingCalendarInstance);
                    }
                }
                if (changes.containsKey("repaymentFrequencyNthDayType") || changes.containsKey("repaymentFrequencyDayOfWeekType")) {
                    if (changes.get("repaymentFrequencyNthDayType") == null) {
                        if (ciList != null && !ciList.isEmpty()) {
                            final CalendarInstance calendarInstance = ciList.get(0);
                            final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService.isCalendarAssociatedWithEntity(
                                    calendarInstance.getEntityId(), calendarInstance.getCalendar().getId(),
                                    CalendarEntityType.LOANS.getValue().longValue());
                            if (isCalendarAssociatedWithEntity) {
                                this.calendarInstanceRepository.delete(calendarInstance);
                                this.calendarRepository.delete(calendarInstance.getCalendar());
                            }
                        }
                    } else {
                        Integer repaymentFrequencyTypeInt = command.integerValueOfParameterNamed("repaymentFrequencyType");
                        if (repaymentFrequencyTypeInt != null) {
                            if (PeriodFrequencyType.fromInt(repaymentFrequencyTypeInt) == PeriodFrequencyType.MONTHS) {
                                final String title = "loan_schedule_" + existingLoanApplication.getId();
                                final Integer typeId = CalendarType.COLLECTION.getValue();
                                final CalendarFrequencyType repaymentFrequencyType = CalendarFrequencyType.MONTHLY;
                                final Integer interval = command.integerValueOfParameterNamed("repaymentEvery");
                                LocalDate startDate = command.localDateValueOfParameterNamed("repaymentsStartingFromDate");
                                if (startDate == null) {
                                    startDate = command.localDateValueOfParameterNamed("expectedDisbursementDate");
                                }
                                final Calendar newCalendar = Calendar.createRepeatingCalendar(title, startDate, typeId,
                                        repaymentFrequencyType, interval, (Integer) changes.get("repaymentFrequencyDayOfWeekType"),
                                        (Integer) changes.get("repaymentFrequencyNthDayType"));
                                if (ciList != null && !ciList.isEmpty()) {
                                    final CalendarInstance calendarInstance = ciList.get(0);
                                    final boolean isCalendarAssociatedWithEntity = this.calendarReadPlatformService
                                            .isCalendarAssociatedWithEntity(calendarInstance.getEntityId(),
                                                    calendarInstance.getCalendar().getId(),
                                                    CalendarEntityType.LOANS.getValue().longValue());
                                    if (isCalendarAssociatedWithEntity) {
                                        final Calendar existingCalendar = calendarInstance.getCalendar();
                                        if (existingCalendar != null) {
                                            String existingRecurrence = existingCalendar.getRecurrence();
                                            if (!existingRecurrence.equals(newCalendar.getRecurrence())) {
                                                existingCalendar.setRecurrence(newCalendar.getRecurrence());
                                                this.calendarRepository.save(existingCalendar);
                                            }
                                        }
                                    }
                                } else {
                                    this.calendarRepository.save(newCalendar);
                                    final Integer calendarEntityType = CalendarEntityType.LOANS.getValue();
                                    final CalendarInstance calendarInstance = new CalendarInstance(newCalendar,
                                            existingLoanApplication.getId(), calendarEntityType);
                                    this.calendarInstanceRepository.save(calendarInstance);
                                }
                            }
                        }
                    }
                }
            }

            // Save linked account information
            final String linkAccountIdParamName = "linkAccountId";
            final boolean backdatedTxnsAllowedTill = false;
            final Long savingsAccountId = command.longValueOfParameterNamed(linkAccountIdParamName);
            AccountAssociations accountAssociations = this.accountAssociationsRepository.findByLoanIdAndType(loanId,
                    AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue());
            boolean isLinkedAccPresent = false;
            if (savingsAccountId == null) {
                if (accountAssociations != null) {
                    if (this.fromJsonHelper.parameterExists(linkAccountIdParamName, command.parsedJson())) {
                        this.accountAssociationsRepository.delete(accountAssociations);
                        changes.put(linkAccountIdParamName, null);
                    } else {
                        isLinkedAccPresent = true;
                    }
                }
            } else {
                isLinkedAccPresent = true;
                boolean isModified = false;
                if (accountAssociations == null) {
                    isModified = true;
                } else {
                    final SavingsAccount savingsAccount = accountAssociations.linkedSavingsAccount();
                    if (savingsAccount == null || !savingsAccount.getId().equals(savingsAccountId)) {
                        isModified = true;
                    }
                }
                if (isModified) {
                    final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsAccountId,
                            backdatedTxnsAllowedTill);
                    this.fromApiJsonDeserializer.validatelinkedSavingsAccount(savingsAccount, existingLoanApplication);
                    if (accountAssociations == null) {
                        boolean isActive = true;
                        accountAssociations = AccountAssociations.associateSavingsAccount(existingLoanApplication, savingsAccount,
                                AccountAssociationType.LINKED_ACCOUNT_ASSOCIATION.getValue(), isActive);
                    } else {
                        accountAssociations.updateLinkedSavingsAccount(savingsAccount);
                    }
                    changes.put(linkAccountIdParamName, savingsAccountId);
                    this.accountAssociationsRepository.save(accountAssociations);
                }
            }

            if (!isLinkedAccPresent) {
                final Set<LoanCharge> charges = existingLoanApplication.charges();
                for (final LoanCharge loanCharge : charges) {
                    if (loanCharge.getChargePaymentMode().isPaymentModeAccountTransfer()) {
                        final String errorMessage = "one of the charges requires linked savings account for payment";
                        throw new LinkedAccountRequiredException("loanCharge", errorMessage);
                    }
                }
            }

            if ((command.longValueOfParameterNamed(productIdParamName) != null)
                    || (command.longValueOfParameterNamed(clientIdParamName) != null)
                    || (command.longValueOfParameterNamed(groupIdParamName) != null)) {
                Long OfficeId = null;
                if (existingLoanApplication.getClient() != null) {
                    OfficeId = existingLoanApplication.getClient().getOffice().getId();
                } else if (existingLoanApplication.getGroup() != null) {
                    OfficeId = existingLoanApplication.getGroup().getOffice().getId();
                }
                officeSpecificLoanProductValidation(existingLoanApplication.getLoanProduct().getId(), OfficeId);
            }

            // updating loan interest recalculation details throwing null
            // pointer exception after saveAndFlush
            // http://stackoverflow.com/questions/17151757/hibernate-cascade-update-gives-null-pointer/17334374#17334374
            this.loanRepositoryWrapper.saveAndFlush(existingLoanApplication);

            if (productRelatedDetail.isInterestRecalculationEnabled()) {
                this.fromApiJsonDeserializer.validateLoanForInterestRecalculation(existingLoanApplication);
                if (changes.containsKey(LoanProductConstants.IS_INTEREST_RECALCULATION_ENABLED_PARAMETER_NAME)) {
                    createAndPersistCalendarInstanceForInterestRecalculation(existingLoanApplication);

                }

            }

            loanOtherProcess(command, existingLoanApplication, true, changes);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(loanId) //
                    .withOfficeId(existingLoanApplication.getOfficeId()) //
                    .withClientId(existingLoanApplication.getClientId()) //
                    .withGroupId(existingLoanApplication.getGroupId()) //
                    .withLoanId(existingLoanApplication.getId()) //
                    .with(changes).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("loan_account_no_UNIQUE")
                || (realCause.getCause() != null && realCause.getCause().getMessage().contains("loan_account_no_UNIQUE"))) {

            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.accountNo",
                    "Loan with accountNo `" + accountNo + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("loan_externalid_UNIQUE")
                || (realCause.getCause() != null && realCause.getCause().getMessage().contains("loan_externalid_UNIQUE"))) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.loan.duplicate.externalId",
                    "Loan with externalId `" + externalId + "` already exists", "externalId", externalId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        LOG.error("Error occured.", dve);
    }

    private Loan retrieveLoanBy(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        loan.setHelpers(defaultLoanLifecycleStateMachine(), this.loanSummaryWrapper, this.loanRepaymentScheduleTransactionProcessorFactory);
        return loan;
    }

    private void validateSubmittedOnDate(final Loan loan) {
        final LocalDate startDate = loan.loanProduct().getStartDate();
        final LocalDate closeDate = loan.loanProduct().getCloseDate();
        final LocalDate expectedFirstRepaymentOnDate = loan.getExpectedFirstRepaymentOnDate();
        final LocalDate submittedOnDate = loan.getSubmittedOnDate();

        String defaultUserMessage = "";
        if (startDate != null && submittedOnDate.isBefore(startDate)) {
            defaultUserMessage = "submittedOnDate cannot be before the loan product startDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.before.the.loan.product.start.date", defaultUserMessage,
                    submittedOnDate.toString(), startDate.toString());
        }

        if (closeDate != null && submittedOnDate.isAfter(closeDate)) {
            defaultUserMessage = "submittedOnDate cannot be after the loan product closeDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.product.close.date", defaultUserMessage,
                    submittedOnDate.toString(), closeDate.toString());
        }

        if (expectedFirstRepaymentOnDate != null && submittedOnDate.isAfter(expectedFirstRepaymentOnDate)) {
            defaultUserMessage = "submittedOnDate cannot be after the loans  expectedFirstRepaymentOnDate.";
            throw new LoanApplicationDateException("submitted.on.date.cannot.be.after.the.loan.expected.first.repayment.date",
                    defaultUserMessage, submittedOnDate.toString(), expectedFirstRepaymentOnDate.toString());
        }
    }

    private void checkClientOrGroupActive(final Loan loan) {
        final Client client = loan.client();
        if (client != null) {
            if (client.isNotActive()) {
                throw new ClientNotActiveException(client.getId());
            }
        }
        final Group group = loan.group();
        if (group != null) {
            if (group.isNotActive()) {
                throw new GroupNotActiveException(group.getId());
            }
        }
    }

    private void saveAndFlushLoanWithDataIntegrityViolationChecks(final Loan loan) {
        try {
            this.loanRepositoryWrapper.saveAndFlush(loan);
        } catch (final JpaSystemException | DataIntegrityViolationException e) {
            final Throwable realCause = e.getCause();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loan.application");
            if (realCause.getMessage().toLowerCase().contains("external_id_unique")) {
                baseDataValidator.reset().parameter("externalId").failWithCode("value.must.be.unique");
            }
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                        dataValidationErrors, e);
            }
        }
    }

    private AppUser getAppUserIfPresent() {
        AppUser user = null;
        if (this.context != null) {
            user = this.context.getAuthenticatedUserIfPresent();
        }
        return user;
    }

    private void officeSpecificLoanProductValidation(final Long productId, final Long officeId) {
        final GlobalConfigurationProperty restrictToUserOfficeProperty = this.globalConfigurationRepository
                .findOneByNameWithNotFoundDetection(FineractEntityAccessConstants.GLOBAL_CONFIG_FOR_OFFICE_SPECIFIC_PRODUCTS);
        if (restrictToUserOfficeProperty.isEnabled()) {
            FineractEntityRelation fineractEntityRelation = fineractEntityRelationRepository
                    .findOneByCodeName(FineractEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.toStr());
            FineractEntityToEntityMapping officeToLoanProductMappingList = this.repository.findListByProductId(fineractEntityRelation,
                    productId, officeId);
            if (officeToLoanProductMappingList == null) {
                throw new NotOfficeSpecificProductException(productId, officeId);
            }

        }
    }

    @Transactional
    @Override
    public CommandProcessingResult submitLoanApproval(Long loanId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForSubmitLoanApproval(command.json());
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final Long loanProductId = loan.productId();

        Long lienSavingsTransactionId = upFrontChargeLienProcess(loan);

        validateDocumentComplete(loanProductId, loanId);

        final JsonElement loanApprovalRequest = this.fromJsonHelper.parse(command.json());
        final Long paymentTypeId = this.fromJsonHelper.extractLongNamed(ClientBusinessApiConstants.paymentTypeIdParamName,
                loanApprovalRequest);
        final PaymentType paymentType = this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);

        // validate repayment method is base on loan product repayment configured
        boolean doNotAllowAfterCheckLoanProductPaymentTypeConfig = false;
        final LoanProductPaymentTypeConfigData loanProductPaymentTypeConfigData = this.loanProductPaymentTypeConfigReadPlatformService
                .retrieveOneViaLoanProduct(loanProductId);
        if (ObjectUtils.isNotEmpty(loanProductPaymentTypeConfigData)) {
            if (BooleanUtils.isNotTrue(loanProductPaymentTypeConfigData.getActive())) {
                throw new LoanProductPaymentTypeConfigNotFoundException(
                        "Loan product " + loanProductPaymentTypeConfigData.getLoanProductData().getName() + " with payment type config "
                        + loanProductPaymentTypeConfigData.getName() + " is not active.");
            }
            final Collection<PaymentTypeData> paymentTypeDatas = loanProductPaymentTypeConfigData.getPaymentTypes();
            if (ObjectUtils.isNotEmpty(paymentTypeDatas)) {
                doNotAllowAfterCheckLoanProductPaymentTypeConfig = paymentTypeDatas.stream()
                        .noneMatch(predicate -> Objects.equals(predicate.getId(), paymentTypeId));
            } else {
                throw new LoanProductPaymentTypeConfigNotFoundException(
                        "Loan product " + loanProductPaymentTypeConfigData.getLoanProductData().getName() + " with payment type config "
                        + loanProductPaymentTypeConfigData.getName() + " does not have a source payment type configuration.");
            }
        } else {
            log.warn("No configuration for Loan Product Payment Type.");
        }

        if (doNotAllowAfterCheckLoanProductPaymentTypeConfig) {
            throw new LoanProductPaymentTypeConfigNotFoundException(
                    "You have selected a wrong payment type " + paymentType.getPaymentName() + " for this loan.");
        }

        String message = "Successful";
        String auth = null;
        Integer statusEnum = LoanInstrumentStatus.PENDING.getValue();
        String data = null;

        final JsonObject jsonObjectLoanInstrument = new JsonObject();
        jsonObjectLoanInstrument.addProperty(ClientBusinessApiConstants.paymentTypeIdParamName, paymentTypeId);
        jsonObjectLoanInstrument.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
        jsonObjectLoanInstrument.addProperty(SavingsApiConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);

        if (Objects.equals(paymentTypeId, this.paymentTypeDeductionId)) {
            // deduction process
            statusEnum = LoanInstrumentStatus.ACTIVE.getValue();
        } else if (Objects.equals(paymentTypeId, this.paymentTypeChequeId)) {
            // cheque process
            statusEnum = LoanInstrumentStatus.ACTIVE.getValue();
            final String name = loanId + "_CHEQUE";
            final String description = loanId + "_CHEQUE";
            final String location = this.fromJsonHelper.extractStringNamed(DocumentConfigApiConstants.locationParam, loanApprovalRequest);
            final String type = this.fromJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, loanApprovalRequest);
            final JsonObject jsonObjectLoanInstrumentCheque = new JsonObject();
            jsonObjectLoanInstrumentCheque.addProperty(DocumentConfigApiConstants.nameParam, name);
            jsonObjectLoanInstrumentCheque.addProperty(DocumentConfigApiConstants.descriptionParam, description);
            jsonObjectLoanInstrumentCheque.addProperty(DocumentConfigApiConstants.locationParam, location);
            jsonObjectLoanInstrumentCheque.addProperty(DocumentConfigApiConstants.typeParam, type);
            this.documentWritePlatformService.createBase64Document(DocumentManagementEntity.LOANS.toString(), loanId,
                    jsonObjectLoanInstrumentCheque.toString());
        } else {
            throw new PlatformDataIntegrityException("error.submit.loan.approval",
                    "Payment type " + paymentType.getPaymentName() + " not yet supported.");
        }

        jsonObjectLoanInstrument.addProperty(LoanBusinessApiConstants.messageParam, message);
        jsonObjectLoanInstrument.addProperty(LoanBusinessApiConstants.statusEnumParam, statusEnum);

        if (StringUtils.isNotBlank(auth)) {
            jsonObjectLoanInstrument.addProperty(LoanBusinessApiConstants.authParam, auth);
        }
        if (StringUtils.isNotBlank(data)) {
            jsonObjectLoanInstrument.addProperty(LoanBusinessApiConstants.dataParam, data);
        }
        // every call is a new LoanInstrument
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createDatatable(LoanBusinessApiConstants.loanInstrumentParam, loanId, null) //
                .withJson(jsonObjectLoanInstrument.toString()) //
                .build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        final Long loanInstrumentId = result.resourceId();

        try {
            // check if approvalCheck is created then perform an update else create new
            final JsonObject jsonObjectApprovalCheck = new JsonObject();
            boolean updateLafApprovalCheck = false;

            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.isLafSignedParam, "");
            jsonObjectApprovalCheck.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
            jsonObjectApprovalCheck.addProperty(SavingsApiConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);
            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.isSentForApprovalParam, false);
            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.internalTransferParam, "");
            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.clientBankIdParam, "");
            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.netPayParam, "");
            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.defaultPaymentMethodIdParam, loanInstrumentId);
            if (lienSavingsTransactionId != null) {
                jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.lienSavingsTransactionIdParam, lienSavingsTransactionId);
            }

            final GenericResultsetData results = this.readWriteNonCoreDataService
                    .retrieveDataTableGenericResultSet(DocumentConfigApiConstants.approvalCheckParam, loanId, null, null);
            if (!ObjectUtils.isEmpty(results) && !CollectionUtils.isEmpty(results.getData())) {
                updateLafApprovalCheck = true;

                final List<ResultsetRowData> resultsetRowDatas = results.getData();
                resultsetRowDatas.stream().forEach(res -> {
                    try {
                        // final Object objectLoanId = res.getRow().get(0);
                        // if (ObjectUtils.isNotEmpty(objectLoanId)) {
                        // final Long loan_id = Long.valueOf(StringUtils.defaultIfBlank(String.valueOf(objectLoanId),
                        // null));
                        // approvalCheckRequest.setLoan_id(loan_id);
                        // }
                        final Object objectSentForApproval = res.getRow().get(1);
                        if (ObjectUtils.isNotEmpty(objectSentForApproval)) {
                            final String isSentForApproval = StringUtils.defaultIfBlank(String.valueOf(objectSentForApproval), "");
                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.isSentForApprovalParam, isSentForApproval);
                        }
                        final Object objectInternalTransfer = res.getRow().get(2);
                        if (ObjectUtils.isNotEmpty(objectInternalTransfer)) {
                            final String internalTransfer = StringUtils.defaultIfBlank(String.valueOf(objectInternalTransfer), "");
                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.internalTransferParam, internalTransfer);
                        }
                        final Object objectClientBankId = res.getRow().get(3);
                        if (ObjectUtils.isNotEmpty(objectClientBankId)) {
                            final String clientBankId = StringUtils.defaultIfBlank(String.valueOf(objectClientBankId), "");
                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.clientBankIdParam, clientBankId);
                        }
                        final Object objectNetPay = res.getRow().get(4);
                        if (ObjectUtils.isNotEmpty(objectNetPay)) {
                            final String netPay = StringUtils.defaultIfBlank(String.valueOf(objectNetPay), "");
                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.netPayParam, netPay);
                        }
                        // final Object objectDefaultPaymentMethodId = res.getRow().get(5);
                        // if (ObjectUtils.isNotEmpty(objectDefaultPaymentMethodId)) {
                        // final String defaultPaymentMethodId =
                        // StringUtils.defaultIfBlank(String.valueOf(objectDefaultPaymentMethodId), "");
                        // jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.defaultPaymentMethodIdParam,
                        // defaultPaymentMethodId);
                        // }
                        final Object objectIsLafSigned = res.getRow().get(6);
                        if (ObjectUtils.isNotEmpty(objectIsLafSigned)) {
                            final String isLafSigned = StringUtils.defaultIfBlank(String.valueOf(objectIsLafSigned), "");
                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.isLafSignedParam, isLafSigned);
                        }
//                        final Object objectLienSavingsTransactionIdParam = res.getRow().get(7);
//                        if (ObjectUtils.isNotEmpty(objectLienSavingsTransactionIdParam)) {
//                            final String lienSavingsTransactionIdDT = StringUtils.defaultIfBlank(String.valueOf(objectLienSavingsTransactionIdParam), null);
//                            jsonObjectApprovalCheck.addProperty(DocumentConfigApiConstants.lienSavingsTransactionIdParam, lienSavingsTransactionIdDT);
//                        }
                    } catch (Exception e) {
                        log.warn("error.approvalCheckRequest.submitLoanApproval: {}", e.getMessage());
                    }
                });
            }
            final String apiRequestBodyAsJson = jsonObjectApprovalCheck.toString();

            CommandWrapper commandRequestApprovalCheck;
            if (updateLafApprovalCheck) {
                // update approvalCheck
                commandRequestApprovalCheck = new CommandWrapperBuilder() //
                        .updateDatatable(DocumentConfigApiConstants.approvalCheckParam, loanId, null) //
                        .withJson(apiRequestBodyAsJson) //
                        .build();
            } else {
                // create approvalCheck
                commandRequestApprovalCheck = new CommandWrapperBuilder() //
                        .createDatatable(DocumentConfigApiConstants.approvalCheckParam, loanId, null) //
                        .withJson(apiRequestBodyAsJson) //
                        .build();
            }
            this.commandsSourceWritePlatformService.logCommandSource(commandRequestApprovalCheck);

        } catch (Exception e) {
            log.warn("submitLoanApproval ApprovalCheck: {}", e);
            throw new PlatformDataIntegrityException("error.submit.loan.approval", "Unable to save loan approval checks.");
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(loanInstrumentId).withSubEntityId(loanId).build();
    }

    protected void validateDocumentComplete(final Long loanProductId, Long loanId) throws PlatformDataIntegrityException {
        // validate document is complete
        final DocumentProductConfigData documentProductConfigData = this.documentProductConfigReadPlatformService
                .retrieveLoanProductDocument(loanProductId);
        int loanProductDocumentCount = 0;
        if (ObjectUtils.isNotEmpty(documentProductConfigData)) {
            final DocumentConfigData documentConfigData = documentProductConfigData.getConfigData();
            if (ObjectUtils.isNotEmpty(documentConfigData)) {
                final Collection<CodeBusinessData> codeBusinessDatas = documentConfigData.getSettings();
                if (!CollectionUtils.isEmpty(codeBusinessDatas)) {
                    loanProductDocumentCount = codeBusinessDatas.size();
                }
            }
        }

        final Long totalLoanDocumentSaved = this.documentBusinessRepositoryWrapper
                .countByParentEntityTypeAndParentEntityId(DocumentManagementEntity.LOANS.toString(), loanId);
        log.info("loanProductDocumentCount:{} - totalLoanDocumentSaved:{}", loanProductDocumentCount, totalLoanDocumentSaved);
        if (totalLoanDocumentSaved < loanProductDocumentCount) {
            Long remaining = (loanProductDocumentCount - totalLoanDocumentSaved);
            throw new PlatformDataIntegrityException("error.submit.loan.approval",
                    "System requires " + remaining + " more document(s) before sending for approval.");
        }
    }

    protected Long upFrontChargeLienProcess(final Loan loan) {
        final Long loanId = loan.getId();
        Long lienSavingsTransactionId = null;
        //check if upFront Charges are set for this loan
        //and validate the total upFront charge amount is available in the client default savings account/wallet
        //lien/hold the amount before sending for approval
        final Set<LoanCharge> loanCharges = loan.charges();
        if (!CollectionUtils.isEmpty(loanCharges)) {
            final Client client = loan.client();
            final Long savingsId = client.savingsAccountId();

            //sum the upfront fees
            BigDecimal sumUpfrontCharges = loanCharges
                    .stream()
                    .filter(chg -> chg.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && chg.isChargePending()
                    && chg.isActive()
                    && (chg.isUpfrontCharge() || chg.isUpfrontHoldCharge()))
                    .map(mapper -> mapper.amountOutstanding())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            sumUpfrontCharges = sumUpfrontCharges.setScale(2, RoundingMode.HALF_EVEN);
            if (sumUpfrontCharges.compareTo(BigDecimal.ZERO) > 0) {
                lienSavingsTransactionId = holdAmount(sumUpfrontCharges, loanId, savingsId,
                        "for upFront charges of loan Id-" + loanId,
                        this.commandsSourceWritePlatformService);

                //put in a note for clarification
                final Note note = Note.loanNote(loan, "Total Loan Upfront Charges " + sumUpfrontCharges + " on hold with savings transaction Id-" + lienSavingsTransactionId);
                this.noteRepository.save(note);
            }
        }
        return lienSavingsTransactionId;
    }

}
