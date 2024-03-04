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
package org.apache.fineract.portfolio.business.overdraft.service;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.overdraft.api.OverdraftApiResourceConstants;
import org.apache.fineract.portfolio.business.overdraft.data.OverdraftDataValidator;
import org.apache.fineract.portfolio.business.overdraft.domain.Overdraft;
import org.apache.fineract.portfolio.business.overdraft.domain.OverdraftRepositoryWrapper;
import org.apache.fineract.portfolio.business.overdraft.exception.OverdraftNotFoundException;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdraftWriteServiceImpl implements OverdraftWriteService {

    private final OverdraftDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final OverdraftRepositoryWrapper overdraftRepositoryWrapper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final LoanChargeRepository loanChargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        // String[] cause = StringUtils.split(realCause.getMessage(), "'");

        // String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
        // if (getCause.contains("name")) {
        // final String name = command.stringValueOfParameterNamed(OverdraftApiResourceConstants.NAME);
        // throw new PlatformDataIntegrityException("error.msg.overdraft.duplicate", "Overdraft with name `" + name + "`
        // already exists",
        // OverdraftApiResourceConstants.NAME, name);
        // } else if (getCause.contains("external_id")) {
        // final String externalId = command.stringValueOfParameterNamed(OverdraftApiResourceConstants.EXTERNALID);
        // throw new PlatformDataIntegrityException("error.msg.overdraft.duplicate",
        // "Overdraft with externalId `" + externalId + "` already exists", OverdraftApiResourceConstants.EXTERNALID,
        // externalId);
        // } else if (getCause.contains("rc_number")) {
        // final String rcNumber = command.stringValueOfParameterNamed(OverdraftApiResourceConstants.RCNUMBER);
        // throw new PlatformDataIntegrityException("error.msg.overdraft.duplicate.mobileNo",
        // "Overdraft with registration `" + rcNumber + "` already exists", OverdraftApiResourceConstants.RCNUMBER,
        // rcNumber);
        // }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.overdraft.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("OverdraftErrorOccured: {}", dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult submitOverdraft(JsonCommand command) {

        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());
            final JsonElement element = this.fromApiJsonHelper.parse(command.json());

            final Long savingsId = this.fromApiJsonHelper.extractLongNamed(OverdraftApiResourceConstants.SAVINGS_ID, element);
            final SavingsAccount savingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(savingsId);
            if (!savingsAccount.isActive()) {
                throw new OverdraftNotFoundException("Attached savings is not active.");
            }
            if (!savingsAccount.isAllowOverdraft()) {
                throw new OverdraftNotFoundException("Savings account does not support overdraft.");
            }

            List<Overdraft> overdrafts = this.overdraftRepositoryWrapper.findBySavingsAccountId(savingsId);
            if (!CollectionUtils.isEmpty(overdrafts)) {
                final boolean doNotAllowMultiple = overdrafts.stream().anyMatch(predicate -> overdraftStatusCheck(predicate));
                if (doNotAllowMultiple) {
                    throw new OverdraftNotFoundException("Close all pending Overdraft on the system.");
                }
            }

            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.AMOUNT,
                    element);

            final BigDecimal nominalAnnualInterestRateOverdraft = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.NOMINALINTEREST, element);

            final Long numberOfDays = this.fromApiJsonHelper.extractLongNamed(OverdraftApiResourceConstants.NUMBER_OF_DAYS, element);

            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(OverdraftApiResourceConstants.STARTDATE, element);
            final LocalDate expiryDate = startDate.plusDays(numberOfDays);

            final Overdraft overdraft = Overdraft.createOverdraft(amount, nominalAnnualInterestRateOverdraft, startDate, expiryDate,
                    savingsAccount);

            this.overdraftRepositoryWrapper.saveAndFlush(overdraft);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(overdraft.getId())
                    .withSavingsId(savingsId).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult modifyOverdraft(Long overdraftId, JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftId);
            if (Objects.equals(overdraft.getStatus(), LoanApprovalStatus.ACTIVE.getValue())
                    || Objects.equals(overdraft.getStatus(), LoanApprovalStatus.APPROVED.getValue())
                    || Objects.equals(overdraft.getStatus(), LoanApprovalStatus.CLOSED.getValue())
                    || Objects.equals(overdraft.getStatus(), LoanApprovalStatus.REJECTED.getValue())) {
                throw new OverdraftNotFoundException("Overdraft cannot be modified in it current status.");
            }

            final Map<String, Object> changes = overdraft.update(command);
            if (!changes.isEmpty()) {
                this.overdraftRepositoryWrapper.saveAndFlush(overdraft);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(overdraft.getId())
                    .withSavingsId(overdraft.getSavingsAccount().getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteOverdraft(Long overdraftId) {
        this.context.authenticatedUser();
        final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftId);
        if (Objects.equals(overdraft.getStatus(), LoanApprovalStatus.DRAFT.getValue())) {
            this.overdraftRepositoryWrapper.deleteById(overdraftId);
        } else {
            throw new OverdraftNotFoundException("Overdraft cannot be removed in it current status.");
        }
        return new CommandProcessingResultBuilder().withEntityId(overdraftId).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult stopOverdraft(Long overdraftId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForStop(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftId);
        if (Objects.equals(overdraft.getStatus(), LoanApprovalStatus.ACTIVE.getValue())) {
            final SavingsAccount savingsAccount = overdraft.getSavingsAccount();
            if (!savingsAccount.isActive()) {
                throw new OverdraftNotFoundException("Attached savings is not active.");
            }
            final Long savingsAccountId = savingsAccount.getId();
            String sql = "UPDATE m_savings_account ms SET ms.overdraft_limit=?, ms.nominal_annual_interest_rate_overdraft=? WHERE ms.id=?";
            this.jdbcTemplate.update(sql, 0, 0, savingsAccountId);

            final String noteText = this.fromApiJsonHelper.extractStringNamed(SavingsApiConstants.noteParamName, element);
            if (StringUtils.isNotBlank(noteText)) {
                final Note note = Note.savingNote(savingsAccount, noteText + "-" + overdraftId);
                this.noteRepository.save(note);
            }

            overdraft.setStatus(LoanApprovalStatus.CLOSED.getValue());
            this.overdraftRepositoryWrapper.saveAndFlush(overdraft);
        } else {
            throw new OverdraftNotFoundException("Overdraft cannot be removed in it current status.");
        }
        return new CommandProcessingResultBuilder().withEntityId(overdraftId).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult sendOverdraftForApproval(Long overdraftId) {
        this.context.authenticatedUser();
        final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftId);
        if (Objects.equals(overdraft.getStatus(), LoanApprovalStatus.DRAFT.getValue())) {
            overdraft.setStatus(LoanApprovalStatus.QUEUE.getValue());
            this.overdraftRepositoryWrapper.saveAndFlush(overdraft);
        } else {
            throw new OverdraftNotFoundException("Overdraft cannot be sent for approval in it current status.");
        }
        return new CommandProcessingResultBuilder().withEntityId(overdraftId).build();
    }

    private boolean overdraftStatusCheck(Overdraft predicate) {
        final Collection<Integer> overdraftStatus = new ArrayList<>(
                Arrays.asList(LoanApprovalStatus.APPROVED.getValue(), LoanApprovalStatus.DRAFT.getValue(),
                        LoanApprovalStatus.QUEUE.getValue(), LoanApprovalStatus.PENDING.getValue(), LoanApprovalStatus.ACTIVE.getValue()));
        return overdraftStatus.stream().anyMatch(val -> Objects.equals(val, predicate.getStatus()));
    }
}
