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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.api.MetricsApiResourceConstants;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.metrics.data.MetricsDataValidator;
import org.apache.fineract.portfolio.business.metrics.domain.Metrics;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistory;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistoryRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.exception.MetricsNotFoundException;
import org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import static org.apache.fineract.simplifytech.data.GeneralConstants.DATEFORMET_DEFAULT;
import static org.apache.fineract.simplifytech.data.GeneralConstants.LOCALE_EN_DEFAULT;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsWriteServiceImpl implements MetricsWriteService {

    private final MetricsDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final MetricsRepositoryWrapper metricsRepositoryWrapper;
    private final MetricsHistoryRepositoryWrapper metricsHistoryRepositoryWrapper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;


    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        //String[] cause = StringUtils.split(realCause.getMessage(), "'");

//        String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
//        if (getCause.contains("name")) {
//            final String name = command.stringValueOfParameterNamed(MetricsApiResourceConstants.NAME);
//            throw new PlatformDataIntegrityException("error.msg.metrics.duplicate", "Metrics with name `" + name + "` already exists",
//                    MetricsApiResourceConstants.NAME, name);
//        } else if (getCause.contains("external_id")) {
//            final String externalId = command.stringValueOfParameterNamed(MetricsApiResourceConstants.EXTERNALID);
//            throw new PlatformDataIntegrityException("error.msg.metrics.duplicate",
//                    "Metrics with externalId `" + externalId + "` already exists", MetricsApiResourceConstants.EXTERNALID, externalId);
//        } else if (getCause.contains("rc_number")) {
//            final String rcNumber = command.stringValueOfParameterNamed(MetricsApiResourceConstants.RCNUMBER);
//            throw new PlatformDataIntegrityException("error.msg.metrics.duplicate.mobileNo",
//                    "Metrics with registration `" + rcNumber + "` already exists", MetricsApiResourceConstants.RCNUMBER, rcNumber);
//        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.metrics.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("MetricsErrorOccured: {}", dve);
    }

    @Override
    public CommandProcessingResult approveLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();

        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        this.fromApiJsonDeserializer.validateForLoanApprovalUndoReject(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.paymentTypeIdParamName, element);

        metricsLoanStateCheck(metrics, loanId);
        try {
            final Loan loan = metrics.getLoan();
            final Integer rank = metrics.getRank();
            if (rank == 0) {
                loanSubmittedPendingApproval(loan, noteText, today);
            }
            //final Integer status = loan.status().getValue();
            final List<Metrics> metricses = this.metricsRepositoryWrapper.findByLoanId(loanId);
            //final Long totalCount = metricses.stream().count();

//            if (totalCount == 1) {
//                log.info("first loanDisbursal");
//                //perform approve and disburse
//                loanDisbursal(loan, noteText, today, loanId, paymentTypeId);
//            } else {
            //order stream by getRank asc and pick the first
            final List<Metrics> metricsesAhead = metricses
                    .stream()
                    .filter(action
                            -> Objects.equals(action.getStatus(), LoanApprovalStatus.QUEUE.getValue())
                    && action.getRank() > rank)
                    .sorted(Comparator.comparingInt(Metrics::getRank))
                    .toList();
            final long totalAheadCount = metricsesAhead.stream().count();
            if (totalAheadCount == 0) {
                log.info("second loanDisbursal");
                loanDisbursal(loan, noteText, today, paymentTypeId);
            } else {
                final Metrics pickTheNextMetricApproval = metricsesAhead.stream().findFirst().orElseThrow(()
                        -> new MetricsNotFoundException("Next approval not found for loan account: {}." + loan.getAccountNumber())
                );

                pickTheNextMetricApproval.setStatus(LoanApprovalStatus.PENDING.getValue());
                this.metricsRepositoryWrapper.saveAndFlush(pickTheNextMetricApproval);
                saveMetricsHistory(pickTheNextMetricApproval, LoanApprovalStatus.PENDING.getValue());
            }

            saveNoteMetrics(noteText, loan);

            metrics.setStatus(LoanApprovalStatus.APPROVED.getValue());
            this.metricsRepositoryWrapper.saveAndFlush(metrics);
            saveMetricsHistory(metrics, LoanApprovalStatus.APPROVED.getValue());
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

    protected void saveMetricsHistory(final Metrics metrics, final Integer status) {
        final MetricsHistory metricsHistory
                = MetricsHistory.instance(metrics, status);
        this.metricsHistoryRepositoryWrapper.saveAndFlush(metricsHistory);
    }

    protected void loanSubmittedPendingApproval(final Loan loan, final String noteText, final LocalDate today) {
        final Long loanId = loan.getId();
//perform first approval
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(LoanApiConstants.approvedLoanAmountParameterName, loan.getProposedPrincipal());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        apiRequestBodyAsJson.addProperty(LoanBusinessApiConstants.expectedDisbursementDateParameterName, today.toString());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.approvedOnDateParameterName, today.toString());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.localeParameterName, LOCALE_EN_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.dateFormatParameterName, DATEFORMET_DEFAULT);
        final JsonArray disbursementData = new JsonArray();
        apiRequestBodyAsJson.add(LoanApiConstants.disbursementDataParameterName, disbursementData);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        final CommandWrapper commandRequest = builder.approveLoanApplication(loanId).build();
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void loanDisbursal(final Loan loan, final String noteText, final LocalDate today, final Integer paymentTypeId) {
        final Long loanId = loan.getId();
        //perform first approval
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(CollectionSheetConstants.actualDisbursementDateParamName, today.toString());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.localeParameterName, LOCALE_EN_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.dateFormatParameterName, DATEFORMET_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        apiRequestBodyAsJson.addProperty(SavingsApiConstants.paymentTypeIdParamName, paymentTypeId);
        apiRequestBodyAsJson.addProperty(SavingsApiConstants.transactionAmountParamName, loan.getApprovedPrincipal());
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        final CommandWrapper commandRequest = builder.disburseLoanApplication(loanId).build();
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void loanReject(final Loan loan, final String noteText, final LocalDate today) {
        final Long loanId = loan.getId();
        //perform first approval
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(LoanApiConstants.rejectedOnDateParameterName, today.toString());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.localeParameterName, LOCALE_EN_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.dateFormatParameterName, DATEFORMET_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        final CommandWrapper commandRequest = builder.rejectLoanApplication(loanId).build();
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void loanUndo(final Loan loan, final String noteText) {
        final Long loanId = loan.getId();
        //perform first approval
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        final CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void metricsLoanStateCheck(final Metrics metrics, final Long loanId) throws PlatformDataIntegrityException {
        if (!Objects.equals(metrics.getStatus(), LoanApprovalStatus.PENDING.getValue()) || !Objects.equals(loanId, metrics.getLoan().getId())) {
            throw new PlatformDataIntegrityException("error.loan.metrics", "Approval does not match or loan approval not in pending state.");
        }
    }

//    protected void approvalUndoRejectFirstProcess(JsonCommand command, final JsonElement element) {
//        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
//        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
//        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
//        saveNoteMetrics(noteText, loan);
//    }
    protected void saveNoteMetrics(final String noteText, final Loan loan) {
        if (StringUtils.isNotBlank(noteText)) {
            final Note note = Note.loanNote(loan, noteText);
            this.noteRepository.save(note);
        }
    }

    @Override
    public CommandProcessingResult undoLoanMetrics(Long metricsId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForLoanApprovalUndoReject(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        metricsLoanStateCheck(metrics, loanId);

        try {
            final Loan loan = metrics.getLoan();
            final Integer status = loan.status().getValue();
            if (Objects.equals(status, LoanStatus.APPROVED.getValue())) {
                //call defaul mifos real Undo if loan is in approved state
                loanUndo(loan, noteText);
            }
            final Integer rank = metrics.getRank();
            if (rank <= 0) {
                throw new PlatformDataIntegrityException("error.msg.metrics", "Undo not allowed for approval level.");
            }
            final List<Metrics> metricses = this.metricsRepositoryWrapper.findByLoanId(loanId);
            //order stream by getRank desc but less than current rank and pick the first
            final List<Metrics> metricsesAhead = metricses
                    .stream()
                    .filter(action
                            -> Objects.equals(action.getStatus(), LoanApprovalStatus.APPROVED.getValue())
                    && action.getRank() < rank)
                    .sorted(Comparator.comparingInt(Metrics::getRank).reversed())
                    .toList();
            final long totalUndoCount = metricsesAhead.stream().count();
            if (totalUndoCount == 0) {
                throw new PlatformDataIntegrityException("error.msg.metrics", "Approval not allowed for undo.");
            } else {
                final Metrics pickTheLastMetricApproval = metricsesAhead.stream().findFirst().orElseThrow(()
                        -> new MetricsNotFoundException("Last approval not found for loan account: {}." + loan.getAccountNumber())
                );

                pickTheLastMetricApproval.setStatus(LoanApprovalStatus.PENDING.getValue());
                this.metricsRepositoryWrapper.saveAndFlush(pickTheLastMetricApproval);
                saveMetricsHistory(pickTheLastMetricApproval, LoanApprovalStatus.PENDING.getValue());

                saveNoteMetrics(noteText, loan);

                metrics.setStatus(LoanApprovalStatus.QUEUE.getValue());
                this.metricsRepositoryWrapper.saveAndFlush(metrics);
                saveMetricsHistory(metrics, LoanApprovalStatus.QUEUE.getValue());
            }
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
        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        this.fromApiJsonDeserializer.validateForLoanApprovalUndoReject(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        metricsLoanStateCheck(metrics, loanId);

        try {
            final Loan loan = metrics.getLoan();
            final Integer status = loan.status().getValue();
            if (!Objects.equals(status, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue())) {
                //update loan to status for rejection
                UpdateLoanStatus(loan, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue());
            }
            loanReject(loan, noteText, today);

            saveNoteMetrics(noteText, loan);

            metrics.setStatus(LoanApprovalStatus.REJECTED.getValue());
            this.metricsRepositoryWrapper.saveAndFlush(metrics);
            saveMetricsHistory(metrics, LoanApprovalStatus.REJECTED.getValue());
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
        this.fromApiJsonDeserializer.validateForLoanAssign(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final Long staffId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.STAFF_DATA, element);
        metricsLoanStateCheck(metrics, loanId);
        try {
            final Loan loan = metrics.getLoan();
            final Staff oldStaff = metrics.getAssignedUser();
            final Staff newStaff = this.staffRepositoryWrapper.findOneWithNotFoundDetection(staffId);
            if (!Objects.equals(oldStaff.getId(), newStaff.getId())) {

                saveNoteMetrics("Reassign loan from " + oldStaff.displayName() + "to a new approval officer" + newStaff.displayName(), loan);

                metrics.setAssignedUser(newStaff);
                this.metricsRepositoryWrapper.saveAndFlush(metrics);
                saveMetricsHistory(metrics, LoanApprovalStatus.REASSIGNED.getValue());

            }

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
