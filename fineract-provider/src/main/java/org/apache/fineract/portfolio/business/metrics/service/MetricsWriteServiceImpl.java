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
package org.apache.fineract.portfolio.business.metrics.service;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Objects;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.api.MetricsApiResourceConstants;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.metrics.data.MetricsDataValidator;
import org.apache.fineract.portfolio.business.metrics.domain.Metrics;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsWriteServiceImpl implements MetricsWriteService {

    private final MetricsRepositoryWrapper repositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final MetricsDataValidator fromApiJsonDeserializer;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final MetricsRepositoryWrapper metricsRepositoryWrapper;
    private final LoansApiResource loansApiResource;


    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        //String[] cause = StringUtils.split(realCause.getMessage(), "'");

//        String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
//        if (getCause.contains("name")) {
//            final String name = command.stringValueOfParameterNamed(MetricsApiResourceConstants.NAME);
//            throw new PlatformDataIntegrityException("error.msg.employer.duplicate", "Metrics with name `" + name + "` already exists",
//                    MetricsApiResourceConstants.NAME, name);
//        } else if (getCause.contains("external_id")) {
//            final String externalId = command.stringValueOfParameterNamed(MetricsApiResourceConstants.EXTERNALID);
//            throw new PlatformDataIntegrityException("error.msg.employer.duplicate",
//                    "Metrics with externalId `" + externalId + "` already exists", MetricsApiResourceConstants.EXTERNALID, externalId);
//        } else if (getCause.contains("rc_number")) {
//            final String rcNumber = command.stringValueOfParameterNamed(MetricsApiResourceConstants.RCNUMBER);
//            throw new PlatformDataIntegrityException("error.msg.employer.duplicate.mobileNo",
//                    "Metrics with registration `" + rcNumber + "` already exists", MetricsApiResourceConstants.RCNUMBER, rcNumber);
//        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.employer.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("MetricsErrorOccured: {}", dve);
    }

    @Override
    public CommandProcessingResult approveLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForApprovalUndoReject(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        metricsLoanStateCheck(metrics, loanId);
        approvalUndoRejectFirstProcess(command, element);

        final Loan loan = metrics.getLoan();
        final Integer rank = metrics.getRank();
        if (rank==0) {
            //perform first approval
            
//            JsonObject apiRequestBodyAsJson = new JsonObject();
//            // final String commandParam = "disburseToSavingsToBank";
//            // apiRequestBodyAsJson.addProperty("sendToBank", true);
//            // apiRequestBodyAsJson.addProperty("sendToBank", false);
//            apiRequestBodyAsJson.addProperty("transactionAmount", loan.getApprovedPrincipal());
//            apiRequestBodyAsJson.addProperty("note", "Auto Queue by Nx360 Robot");
//            apiRequestBodyAsJson.addProperty("actualDisbursementDate", today.toString());
//            apiRequestBodyAsJson.addProperty("locale", "en");
//            apiRequestBodyAsJson.addProperty("dateFormat", "yyyy-MM-dd");
//            CommandWrapper commandRequest;
//            CommandWrapperBuilder builder;
//            apiRequestBodyAsJson.addProperty("paymentTypeId", 9);
//            apiRequestBodyAsJson.addProperty("accountNumber", String.valueOf(loan.getClientId()));
//            apiRequestBodyAsJson.addProperty("receiptNumber", loan.getClient().getDisplayName());
//
//            if (BooleanUtils.isTrue(businessDisburse)) {
//                // default to Money Transfer
//                builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
//            }
        }
        final Integer status = loan.status().getValue();
        final List<Metrics> metricses = this.metricsRepositoryWrapper.findByLoanId(loanId);
        final Long totalCount = metricses.stream().count();
        final List<Metrics> metricsesAhead = metricses
                .stream()
                .filter(action
                        -> Objects.equals(action.getStatus(), LoanApprovalStatus.QUEUE.getValue())
                && action.getRank() > rank)
                .toList();
        final Long totalAheadCount = metricsesAhead.stream().count();

        if (totalCount == 1) {
            //perform approve and disburse
        } else {
            if (Objects.equals(status, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue())) {

            } else if (Objects.equals(status, LoanStatus.APPROVED.getValue())) {

            } else {
                throw new PlatformDataIntegrityException("error.loan.metrics", "Loan status is invalid for approval.");
            }

        }
        UpdateLoanStatus(loan, status);

        try {
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(metricsId)
                .withLoanId(loanId)
                .build();
    }

    protected void metricsLoanStateCheck(final Metrics metrics, final Long loanId) throws PlatformDataIntegrityException {
        if (!Objects.equals(metrics.getStatus(), LoanApprovalStatus.PENDING.getValue()) || !Objects.equals(loanId, metrics.getLoan().getId())) {
            throw new PlatformDataIntegrityException("error.loan.metrics", "Approval does not match or loan approval not in pending state.");
        }
    }

    protected void approvalUndoRejectFirstProcess(JsonCommand command, final JsonElement element) {
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        saveNoteMetrics(noteText, loan);
    }

    protected void saveNoteMetrics(final String noteText, final Loan loan) {
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }
    }

    @Override
    public CommandProcessingResult undoLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForApprovalUndoReject(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        metricsLoanStateCheck(metrics, loanId);

        approvalUndoRejectFirstProcess(command, element);
        try {
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(metricsId)
                .withLoanId(loanId)
                .build();
    }

    @Override
    public CommandProcessingResult rejectLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForApprovalUndoReject(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        metricsLoanStateCheck(metrics, loanId);

        approvalUndoRejectFirstProcess(command, element);
        try {
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(metricsId)
                .withLoanId(loanId)
                .build();
    }

    @Override
    public CommandProcessingResult assignLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForAssign(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        metricsLoanStateCheck(metrics, loanId);
        try {
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(metricsId)
                .build();
    }

    private void UpdateLoanStatus(final Loan loan, final Integer status) {
        if (status < 300) {
            loan.setLoanStatus(status);
            this.loanRepositoryWrapper.saveAndFlush(loan);
        } else {
            log.warn("Dev check, cannot update loanId {} with status >= 300", loan.getId());
        }
    }

}
