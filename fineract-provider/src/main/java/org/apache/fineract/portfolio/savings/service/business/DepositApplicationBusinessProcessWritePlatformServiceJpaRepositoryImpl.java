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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.MathContext;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.isCalendarInheritedParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.recurringFrequencyParamName;
import static org.apache.fineract.portfolio.savings.DepositsApiConstants.recurringFrequencyTypeParamName;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.domain.AccountAssociationsRepository;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.calendar.domain.Calendar;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.domain.CalendarFrequencyType;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarType;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.DepositAccountDataValidator;
import org.apache.fineract.portfolio.savings.domain.DepositAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccount;
import org.apache.fineract.portfolio.savings.domain.FixedDepositAccountRepository;
import org.apache.fineract.portfolio.savings.domain.RecurringDepositAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountChargeAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.service.DepositApplicationProcessWritePlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountApplicationTransitionApiJsonValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositApplicationBusinessProcessWritePlatformServiceJpaRepositoryImpl implements DepositApplicationBusinessProcessWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(DepositApplicationBusinessProcessWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final SavingsAccountRepositoryWrapper savingAccountRepository;
    private final FixedDepositAccountRepository fixedDepositAccountRepository;
    private final DepositAccountAssembler depositAccountAssembler;
    private final DepositAccountDataValidator depositAccountDataValidator;
    private final AccountNumberGenerator accountNumberGenerator;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    private final SavingsProductRepository savingsProductRepository;
    private final NoteRepository noteRepository;
    private final StaffRepositoryWrapper staffRepository;
    private final SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator;
    private final SavingsAccountChargeAssembler savingsAccountChargeAssembler;
    private final AccountAssociationsRepository accountAssociationsRepository;
    private final FromJsonHelper fromJsonHelper;
    private final CalendarInstanceRepository calendarInstanceRepository;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService;

    @Autowired
    public DepositApplicationBusinessProcessWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final SavingsAccountRepositoryWrapper savingAccountRepository, final DepositAccountAssembler depositAccountAssembler,
            final DepositAccountDataValidator depositAccountDataValidator, final AccountNumberGenerator accountNumberGenerator,
            final ClientRepositoryWrapper clientRepository, final GroupRepository groupRepository,
            final SavingsProductRepository savingsProductRepository, final NoteRepository noteRepository,
            final StaffRepositoryWrapper staffRepository,
            final SavingsAccountApplicationTransitionApiJsonValidator savingsAccountApplicationTransitionApiJsonValidator,
            final SavingsAccountChargeAssembler savingsAccountChargeAssembler,
            final FixedDepositAccountRepository fixedDepositAccountRepository,
            final AccountAssociationsRepository accountAssociationsRepository, final FromJsonHelper fromJsonHelper,
            final CalendarInstanceRepository calendarInstanceRepository, final ConfigurationDomainService configurationDomainService,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository,
            final BusinessEventNotifierService businessEventNotifierService, final DepositApplicationProcessWritePlatformService depositApplicationProcessWritePlatformService) {
        this.context = context;
        this.savingAccountRepository = savingAccountRepository;
        this.depositAccountAssembler = depositAccountAssembler;
        this.accountNumberGenerator = accountNumberGenerator;
        this.depositAccountDataValidator = depositAccountDataValidator;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.savingsProductRepository = savingsProductRepository;
        this.noteRepository = noteRepository;
        this.staffRepository = staffRepository;
        this.savingsAccountApplicationTransitionApiJsonValidator = savingsAccountApplicationTransitionApiJsonValidator;
        this.savingsAccountChargeAssembler = savingsAccountChargeAssembler;
        this.fixedDepositAccountRepository = fixedDepositAccountRepository;
        this.accountAssociationsRepository = accountAssociationsRepository;
        this.fromJsonHelper = fromJsonHelper;
        this.calendarInstanceRepository = calendarInstanceRepository;
        this.configurationDomainService = configurationDomainService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.businessEventNotifierService = businessEventNotifierService;
        this.depositApplicationProcessWritePlatformService = depositApplicationProcessWritePlatformService;
    }

    private CalendarInstance getCalendarInstance(final JsonCommand command, RecurringDepositAccount account) {
        CalendarInstance calendarInstance = null;
        final boolean isCalendarInherited = command.booleanPrimitiveValueOfParameterNamed(isCalendarInheritedParamName);

        if (isCalendarInherited) {
            Set<Group> groups = account.getClient().getGroups();
            Long groupId = null;
            if (groups.isEmpty()) {
                final String defaultUserMessage = "Client does not belong to group/center. Cannot follow group/center meeting frequency.";
                throw new GeneralPlatformDomainRuleException(
                        "error.msg.recurring.deposit.account.cannot.create.not.belongs.to.any.groups.to.follow.meeting.frequency",
                        defaultUserMessage, account.clientId());
            } else if (groups.size() > 1) {
                final String defaultUserMessage = "Client belongs to more than one group. Cannot support recurring deposit.";
                throw new GeneralPlatformDomainRuleException("error.msg.recurring.deposit.account.cannot.create.belongs.to.multiple.groups",
                        defaultUserMessage, account.clientId());
            } else {
                Group group = groups.iterator().next();
                Group parent = group.getParent();
                Integer entityType = CalendarEntityType.GROUPS.getValue();
                if (parent != null) {
                    groupId = parent.getId();
                    entityType = CalendarEntityType.CENTERS.getValue();
                } else {
                    groupId = group.getId();
                }
                CalendarInstance parentCalendarInstance = this.calendarInstanceRepository
                        .findByEntityIdAndEntityTypeIdAndCalendarTypeId(groupId, entityType, CalendarType.COLLECTION.getValue());
                if (parentCalendarInstance == null) {
                    final String defaultUserMessage = "Meeting frequency is not attached to the Group/Center to which the client belongs to.";
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.meeting.frequency.not.attached.to.group.to.which.client.belongs.to", defaultUserMessage,
                            account.clientId());
                }
                calendarInstance = CalendarInstance.from(parentCalendarInstance.getCalendar(), account.getId(),
                        CalendarEntityType.SAVINGS.getValue());
            }
        } else {
            LocalDate calendarStartDate = account.depositStartDate();
            final Integer frequencyType = command.integerValueSansLocaleOfParameterNamed(recurringFrequencyTypeParamName);
            final PeriodFrequencyType periodFrequencyType = PeriodFrequencyType.fromInt(frequencyType);
            final Integer frequency = command.integerValueSansLocaleOfParameterNamed(recurringFrequencyParamName);

            final Integer repeatsOnDay = calendarStartDate.get(ChronoField.DAY_OF_WEEK);
            final String title = "recurring_savings_" + account.getId();

            final Calendar calendar = Calendar.createRepeatingCalendar(title, calendarStartDate, CalendarType.COLLECTION.getValue(),
                    CalendarFrequencyType.from(periodFrequencyType), frequency, repeatsOnDay, null);
            calendarInstance = CalendarInstance.from(calendar, account.getId(), CalendarEntityType.SAVINGS.getValue());
        }
        if (calendarInstance == null) {
            final String defaultUserMessage = "No valid recurring details available for recurring depost account creation.";
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.recurring.deposit.account.cannot.create.no.valid.recurring.details.available", defaultUserMessage,
                    account.clientId());
        }
        return calendarInstance;
    }

    @Transactional
    @Override
    public JsonElement calculateMaturityRDApplication(final String json) {
        final AppUser submittedBy = this.context.authenticatedUser();
        final JsonElement parsedCommand = this.fromJsonHelper.parse(json);
        final JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromJsonHelper);
        this.depositAccountDataValidator.validateRecurringDepositForSubmit(json);
        try {

            final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                    .isSavingsInterestPostingAtCurrentPeriodEnd();
            final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

            final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(command,
                    submittedBy, DepositAccountType.RECURRING_DEPOSIT);

            final CalendarInstance calendarInstance = getCalendarInstance(command, account);

            // FIXME: Avoid save separately (Calendar instance requires account
            // details)
            final MathContext mc = MathContext.DECIMAL64;
            final Calendar calendar = calendarInstance.getCalendar();
            final PeriodFrequencyType frequencyType = CalendarFrequencyType.from(CalendarUtils.getFrequency(calendar.getRecurrence()));
            Integer frequency = CalendarUtils.getInterval(calendar.getRecurrence());
            frequency = frequency == -1 ? 1 : frequency;
            account.generateSchedule(frequencyType, frequency, calendar);
            final boolean isPreMatureClosure = false;
            account.updateMaturityDateAndAmount(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);
            account.validateApplicableInterestRate();

            final BigDecimal nominalAnnualInterestRate = account.getNominalAnnualInterestRate();
            final BigDecimal depositAmount = account.getDepositAmount() == null ? BigDecimal.ZERO : account.getDepositAmount();
            final BigDecimal maturityAmount = account.maturityAmount() == null ? BigDecimal.ZERO : account.maturityAmount();
            final BigDecimal expectedInterestAmount = maturityAmount.subtract(depositAmount);
            final String maturityDate = account.maturityDate() == null ? null : account.maturityDate().toString();
            Integer depositPeriod = null;
            Integer depositPeriodFrequency = null;
            if (account.getAccountTermAndPreClosure() != null) {
                depositPeriod = account.getAccountTermAndPreClosure().depositPeriod();
                depositPeriodFrequency = account.getAccountTermAndPreClosure().depositPeriodFrequency();
            }

            return resultJsonMaturity(expectedInterestAmount, depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, nominalAnnualInterestRate);
        } catch (final Exception dve) {
            LOG.error("calculateMaturityRDApplication: {}", dve);
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.recurring.deposit.account.calculate.maturity", dve.getMessage());
        }
    }

    protected JsonElement resultJsonMaturity(final BigDecimal expectedInterestAmount, final BigDecimal depositAmount, final BigDecimal maturityAmount,
            final String maturityDate, Integer depositPeriod, Integer depositPeriodFrequency, final BigDecimal nominalAnnualInterestRate) {
        final JsonObject jsonObjectRD = new JsonObject();
        jsonObjectRD.addProperty("expectedInterestAmount", expectedInterestAmount);
        jsonObjectRD.addProperty("depositAmount", depositAmount);
        jsonObjectRD.addProperty("maturityAmount", maturityAmount);
        jsonObjectRD.addProperty("maturityDate", maturityDate);
        jsonObjectRD.addProperty("depositPeriod", depositPeriod);
        jsonObjectRD.addProperty("depositPeriodFrequency", depositPeriodFrequency);
        jsonObjectRD.addProperty("nominalAnnualInterestRate", nominalAnnualInterestRate);
        return jsonObjectRD;
    }

    @Override
    public CommandProcessingResult businessAllowModifyActiveRDApplication(final Long accountId, final JsonCommand command) {
        this.depositAccountDataValidator.validateRecurringDepositForUpdate(command.json());
        final RecurringDepositAccount account = (RecurringDepositAccount) this.depositAccountAssembler.assembleFrom(accountId,
                DepositAccountType.RECURRING_DEPOSIT);
        account.setStatus(SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue());
        this.savingAccountRepository.save(account);
        return this.depositApplicationProcessWritePlatformService.modifyRDApplication(accountId, command);
    }

    @Override
    public JsonElement calculateMaturityFDApplication(String json) {
        final AppUser submittedBy = this.context.authenticatedUser();
        final JsonElement parsedCommand = this.fromJsonHelper.parse(json);
        final JsonCommand command = JsonCommand.from(json, parsedCommand, this.fromJsonHelper);
        this.depositAccountDataValidator.validateFixedDepositForSubmit(json);
        try {

            final boolean isSavingsInterestPostingAtCurrentPeriodEnd = this.configurationDomainService
                    .isSavingsInterestPostingAtCurrentPeriodEnd();
            final Integer financialYearBeginningMonth = this.configurationDomainService.retrieveFinancialYearBeginningMonth();

            final FixedDepositAccount account = (FixedDepositAccount) this.depositAccountAssembler.assembleFrom(command, submittedBy,
                    DepositAccountType.FIXED_DEPOSIT);

            final MathContext mc = MathContext.DECIMAL64;
            final boolean isPreMatureClosure = false;

            account.updateMaturityDateAndAmountBeforeAccountActivation(mc, isPreMatureClosure, isSavingsInterestPostingAtCurrentPeriodEnd,
                    financialYearBeginningMonth);

            final BigDecimal nominalAnnualInterestRate = account.getNominalAnnualInterestRate();
            final BigDecimal depositAmount = account.getDepositAmount() == null ? BigDecimal.ZERO : account.getDepositAmount();
            final BigDecimal maturityAmount = account.maturityAmount() == null ? BigDecimal.ZERO : account.maturityAmount();
            final BigDecimal expectedInterestAmount = maturityAmount.subtract(depositAmount);
            final String maturityDate = account.maturityDate() == null ? null : account.maturityDate().toString();
            Integer depositPeriod = null;
            Integer depositPeriodFrequency = null;
            if (account.getAccountTermAndPreClosure() != null) {
                depositPeriod = account.getAccountTermAndPreClosure().depositPeriod();
                depositPeriodFrequency = account.getAccountTermAndPreClosure().depositPeriodFrequency();
            }

            return resultJsonMaturity(expectedInterestAmount, depositAmount, maturityAmount, maturityDate, depositPeriod, depositPeriodFrequency, nominalAnnualInterestRate);
        } catch (final Exception dve) {
            LOG.error("calculateMaturityFDApplication: {}", dve);
            throw new GeneralPlatformDomainRuleException(
                    "error.msg.fixed.deposit.account.calculate.maturity", dve.getMessage());
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult lockDepositAccount(Long savingsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.depositAccountDataValidator.unLockDepositAccountValicator(command.json(), true);
        final SavingsAccount account = this.savingAccountRepository.findOneWithNotFoundDetection(savingsId);

        final Map<String, Object> changes = new LinkedHashMap<>(20);
        account.updateLockedInUntilDate(command, changes);
        if (!changes.isEmpty()) {
            this.savingAccountRepository.save(account);
            saveNoteSavingsAction(command, account);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .with(changes)
                .build();
    }

    protected void saveNoteSavingsAction(JsonCommand command, final SavingsAccount account) {
        final String noteText = command.stringValueOfParameterNamed("note");
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.savingNote(account, noteText);
            this.noteRepository.save(note);
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult unLockDepositAccount(Long savingsId, final JsonCommand command) {
        this.context.authenticatedUser();
        this.depositAccountDataValidator.unLockDepositAccountValicator(command.json(), false);
        final SavingsAccount account = this.savingAccountRepository.findOneWithNotFoundDetection(savingsId);
        account.removeLockedInUntilDate();
        this.savingAccountRepository.save(account);

        saveNoteSavingsAction(command, account);

        return new CommandProcessingResultBuilder() //
                .withEntityId(savingsId) //
                .withOfficeId(account.officeId()) //
                .withClientId(account.clientId()) //
                .withGroupId(account.groupId()) //
                .withSavingsId(savingsId) //
                .build();
    }

}
