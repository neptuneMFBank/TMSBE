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

import static org.apache.fineract.simplifytech.data.GeneralConstants.DATEFORMET_DEFAULT;
import static org.apache.fineract.simplifytech.data.GeneralConstants.LOCALE_EN_DEFAULT;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import org.apache.fineract.infrastructure.dataqueries.data.GenericResultsetData;
import org.apache.fineract.infrastructure.dataqueries.data.ResultsetRowData;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.business.metrics.api.MetricsApiResourceConstants;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.metrics.data.MetricsDataValidator;
import org.apache.fineract.portfolio.business.metrics.domain.Metrics;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistory;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistoryRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.exception.MetricsNotFoundException;
import org.apache.fineract.portfolio.businessevent.domain.loan.business.LoanMetricsApprovalBusinessEvent;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.collectionsheet.CollectionSheetConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import static org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants.expectedDisbursementDateParameterName;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanproduct.exception.LinkedAccountRequiredException;
import org.apache.fineract.portfolio.note.domain.Note;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import static org.apache.fineract.simplifytech.data.GeneralConstants.holdAmount;
import static org.apache.fineract.simplifytech.data.GeneralConstants.releaseAmount;
import static org.apache.fineract.simplifytech.data.GeneralConstants.withdrawAmount;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final LoanChargeRepository loanChargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        // String[] cause = StringUtils.split(realCause.getMessage(), "'");

        // String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
        // if (getCause.contains("name")) {
        // final String name = command.stringValueOfParameterNamed(MetricsApiResourceConstants.NAME);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate", "Metrics with name `" + name + "`
        // already exists",
        // MetricsApiResourceConstants.NAME, name);
        // } else if (getCause.contains("external_id")) {
        // final String externalId = command.stringValueOfParameterNamed(MetricsApiResourceConstants.EXTERNALID);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate",
        // "Metrics with externalId `" + externalId + "` already exists", MetricsApiResourceConstants.EXTERNALID,
        // externalId);
        // } else if (getCause.contains("rc_number")) {
        // final String rcNumber = command.stringValueOfParameterNamed(MetricsApiResourceConstants.RCNUMBER);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate.mobileNo",
        // "Metrics with registration `" + rcNumber + "` already exists", MetricsApiResourceConstants.RCNUMBER,
        // rcNumber);
        // }
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

        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        this.fromApiJsonDeserializer.validateForLoanApprovalUndoReject(command.json());
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Metrics metrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(metricsId);
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
        final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);

        if (this.fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, element)) {
            today = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
        }

        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.paymentTypeIdParamName, element);

        metricsLoanStateCheck(metrics, loanId);
        try {
            boolean sendMetricsApproval = false;
            final Loan loan = metrics.getLoan();
            final Integer rank = metrics.getRank();
            if (rank == 0) {
                loanSubmittedPendingApproval(loan, noteText, today);
                sendMetricsApproval = true;
            }
            // final Integer status = loan.status().getValue();
            final List<Metrics> metricses = this.metricsRepositoryWrapper.findByLoanId(loanId);
            // final Long totalCount = metricses.stream().count();

            // if (totalCount == 1) {
            // log.info("first loanDisbursal");
            // //perform approve and disburse
            // loanDisbursal(loan, noteText, today, loanId, paymentTypeId);
            // } else {
            // order stream by getRank asc and pick the first
            final List<Metrics> metricsesAhead = metricses.stream()
                    .filter(action -> Objects.equals(action.getStatus(), LoanApprovalStatus.QUEUE.getValue()) && action.getRank() > rank)
                    .sorted(Comparator.comparingInt(Metrics::getRank)).toList();
            final long totalAheadCount = metricsesAhead.stream().count();
            if (totalAheadCount == 0) {
                log.info("second loanDisbursal");
                loanDisbursal(loan, noteText, today, paymentTypeId);
            } else {
                final Metrics pickTheNextMetricApproval = metricsesAhead.stream().findFirst().orElseThrow(
                        () -> new MetricsNotFoundException("Next approval not found for loan account: {}." + loan.getAccountNumber()));

                pickTheNextMetricApproval.setStatus(LoanApprovalStatus.PENDING.getValue());
                this.metricsRepositoryWrapper.saveAndFlush(pickTheNextMetricApproval);
                saveMetricsHistory(pickTheNextMetricApproval, LoanApprovalStatus.PENDING.getValue());
                sendMetricsApproval = true;
            }

            saveNoteMetrics(noteText, loan);

            metrics.setStatus(LoanApprovalStatus.APPROVED.getValue());
            this.metricsRepositoryWrapper.saveAndFlush(metrics);
            saveMetricsHistory(metrics, LoanApprovalStatus.APPROVED.getValue());

            if (sendMetricsApproval) {
                businessEventNotifierService.notifyPostBusinessEvent(new LoanMetricsApprovalBusinessEvent(loan));
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
                .withEntityId(metricsId).withLoanId(loanId).build();
    }

    protected void saveMetricsHistory(final Metrics metrics, final Integer status) {
        final MetricsHistory metricsHistory = MetricsHistory.instance(metrics, status);
        this.metricsHistoryRepositoryWrapper.saveAndFlush(metricsHistory);
    }

    protected void loanSubmittedPendingApproval(final Loan loan, final String noteText, final LocalDate today) {
        final Long loanId = loan.getId();
        // perform first approval
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

        if (loan.isSubmittedAndPendingApproval()) {
            loanSubmittedPendingApproval(loan, noteText, today);
        }
        final Long loanId = loan.getId();

        loanDisbursalAccountLien(loanId, loan, today);

        // perform first approval
        // future checks=> check if tokenization is set, Repayment Method Done
        // future checks=> check if laf is set
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(CollectionSheetConstants.actualDisbursementDateParamName, today.toString());
        apiRequestBodyAsJson.addProperty(LoanApiConstants.localeParameterName, LOCALE_EN_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.dateFormatParameterName, DATEFORMET_DEFAULT);
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        apiRequestBodyAsJson.addProperty(SavingsApiConstants.transactionAmountParamName, loan.getApprovedPrincipal());

        if (paymentTypeId != null) {
            apiRequestBodyAsJson.addProperty(SavingsApiConstants.paymentTypeIdParamName, paymentTypeId);
        }
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        CommandWrapper commandRequest;
        if (paymentTypeId != null) {
            commandRequest = builder.disburseLoanApplication(loanId).build();
        } else {
            commandRequest = builder.disburseLoanToSavingsApplication(loanId).build();
        }
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void loanDisbursalAccountLien(final Long loanId, final Loan loan, final LocalDate today) {
        final Set<LoanCharge> loanCharges = loan.charges();
        if (!CollectionUtils.isEmpty(loanCharges)) {
            //check if upFront iswithdrawal is available else leave Fee onHoldAmount
            long countUpfrontCharges = loanCharges
                    .stream()
                    .filter(chg -> chg.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && chg.isChargePending()
                    && chg.isActive()
                    && (chg.isUpfrontCharge() || chg.isUpfrontWithdrawalCharge())).count();

            if (countUpfrontCharges <= 0) {
                return;
            }

            final String accountNumber = loan.getAccountNumber();
            final Long savingsId = getLoanLinkedSavingsId(loanId);

            //withdraw upFrontWithdrawal Fees/Interest
            BigDecimal sumUpfrontWithdrawalCharges = loanCharges
                    .stream()
                    .filter(chg -> chg.getChargePaymentMode().isPaymentModeAccountTransfer()
                    && chg.isChargePending()
                    && chg.isActive()
                    && chg.isUpfrontWithdrawalCharge())
                    .map(mapper -> mapper.amountOutstanding())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            sumUpfrontWithdrawalCharges = sumUpfrontWithdrawalCharges.setScale(2, RoundingMode.HALF_EVEN);
            if (sumUpfrontWithdrawalCharges.compareTo(BigDecimal.ZERO) > 0) {
                final Long withdrawAmountId = withdrawAmount(sumUpfrontWithdrawalCharges, savingsId, "Withdraw Loan Upfront Charges", accountNumber, 1L, commandsSourceWritePlatformService);
                saveNoteMetrics("Withdraw Loan Interest/Fees Upfront Withdrawal Charges " + sumUpfrontWithdrawalCharges + " with savings transaction Id-" + withdrawAmountId, loan);
            }

            //check if upFront Charges via
            //withdraw upFront charges and lien the upFront hold Fee before disbursal
            final Long lienSavingsTransactionId = getLienSavingsTransactionIdLinkToLoan(loanId);

            //we need to update the charges status to paid
            if (lienSavingsTransactionId != null) {

                //release the lien/hold amount
                //CommandWrapperBuilder builder = new CommandWrapperBuilder().withNoJsonBody();
                //CommandWrapper commandRequest = builder.releaseAmount(savingsId, lienSavingsTransactionId).build();
                //this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
                releaseAmount(savingsId, lienSavingsTransactionId, this.commandsSourceWritePlatformService);

                //withdraw upFrontFees
                BigDecimal sumUpfrontCharges = loanCharges
                        .stream()
                        .filter(chg -> chg.getChargePaymentMode().isPaymentModeAccountTransfer()
                        && chg.isChargePending()
                        && chg.isActive()
                        && chg.isUpfrontCharge())
                        .map(mapper -> mapper.amountOutstanding())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                sumUpfrontCharges = sumUpfrontCharges.setScale(2, RoundingMode.HALF_EVEN);
                if (sumUpfrontCharges.compareTo(BigDecimal.ZERO) > 0) {
                    final Long withdrawAmountId = withdrawAmount(sumUpfrontCharges, savingsId, "Withdraw Loan Upfront Charges", accountNumber, 1L, commandsSourceWritePlatformService);
                    saveNoteMetrics("Withdraw Loan Upfront Charges " + sumUpfrontCharges + " with savings transaction Id-" + withdrawAmountId, loan);
                }
                //then hold the rest
                BigDecimal sumUpfrontChargesHold = loanCharges
                        .stream()
                        .filter(chg -> chg.getChargePaymentMode().isPaymentModeAccountTransfer()
                        && chg.isChargePending()
                        && chg.isActive()
                        && chg.isUpfrontHoldCharge())
                        .map(mapper -> mapper.amountOutstanding())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                sumUpfrontChargesHold = sumUpfrontChargesHold.setScale(2, RoundingMode.HALF_EVEN);
                if (sumUpfrontChargesHold.compareTo(BigDecimal.ZERO) > 0) {
                    final Long lienTransactionId = holdAmount(sumUpfrontChargesHold, loanId, savingsId,
                            "lien amount for upFront charges of loan Id-" + loanId,
                            this.commandsSourceWritePlatformService);
                    saveNoteMetrics("Lien Loan Upfront Charges " + sumUpfrontChargesHold + " with savings transaction Id-" + lienTransactionId, loan);
                }
            }
            List<LoanCharge> charges = new ArrayList<>();
            for (LoanCharge loanCharge : loanCharges) {
                if (loanCharge.isUpfrontCharge() || loanCharge.isUpfrontHoldCharge() || loanCharge.isUpfrontWithdrawalCharge()) {
                    loanCharge.markAsFullyPaid();
                    charges.add(loanCharge);
                }
                if (!CollectionUtils.isEmpty(charges)) {
                    loanChargeRepository.saveAll(charges);
                }
            }

        }
    }

    protected Long getLoanLinkedSavingsId(final Long loanId) {
        final PortfolioAccountData portfolioAccountData = this.accountAssociationsReadPlatformService
                .retriveLoanLinkedAssociation(loanId);
        if (portfolioAccountData == null) {
            final String errorMessage = "Loan with id:" + loanId + " is not linked to savings account";
            throw new LinkedAccountRequiredException("loan.link.to.savings", errorMessage, loanId);
        }
        return portfolioAccountData.accountId();
    }

    protected Long getLienSavingsTransactionIdLinkToLoan(final Long loanId) {
        Long lienSavingsTransactionId = null;
        final GenericResultsetData results = this.readWriteNonCoreDataService
                .retrieveDataTableGenericResultSet(DocumentConfigApiConstants.approvalCheckParam, loanId, null, null);
        if (!ObjectUtils.isEmpty(results) && !CollectionUtils.isEmpty(results.getData())) {
            final List<ResultsetRowData> resultsetRowDatas = results.getData();
            for (ResultsetRowData res : resultsetRowDatas) {
                try {
                    final Object objectLienSavingsTransactionIdParam = res.getRow().get(7);
                    if (ObjectUtils.isNotEmpty(objectLienSavingsTransactionIdParam)) {
                        final String lienSavingsTransactionIdDT = StringUtils.defaultIfBlank(String.valueOf(objectLienSavingsTransactionIdParam), null);
                        lienSavingsTransactionId = Long.valueOf(lienSavingsTransactionIdDT);
                    }
                } catch (NumberFormatException e) {
                    log.warn("error.approvalCheckRequest.loanDisbursal: {}", e.getMessage());
                }
            }
        }
        return lienSavingsTransactionId;
    }

    protected void loanReject(final Loan loan, final String noteText, final LocalDate today) {
        final Long loanId = loan.getId();
        // perform first approval
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
        // perform first approval
        JsonObject apiRequestBodyAsJson = new JsonObject();
        apiRequestBodyAsJson.addProperty(LoanApiConstants.noteParameterName, noteText);
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson.toString());
        final CommandWrapper commandRequest = builder.undoLoanApplicationApproval(loanId).build();
        this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
    }

    protected void metricsLoanStateCheck(final Metrics metrics, final Long loanId) throws PlatformDataIntegrityException {
        if (!Objects.equals(metrics.getStatus(), LoanApprovalStatus.PENDING.getValue())
                || !Objects.equals(loanId, metrics.getLoan().getId())) {
            throw new PlatformDataIntegrityException("error.loan.metrics",
                    "Approval does not match or loan approval not in pending state.");
        }
    }

    // protected void approvalUndoRejectFirstProcess(JsonCommand command, final JsonElement element) {
    // final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
    // final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
    // final String noteText = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
    // saveNoteMetrics(noteText, loan);
    // }
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
                // call defaul mifos real Undo if loan is in approved state
                loanUndo(loan, noteText);
            }
            final Integer rank = metrics.getRank();
            if (rank <= 0) {
                throw new PlatformDataIntegrityException("error.msg.metrics", "Undo not allowed for approval level.");
            }
            final List<Metrics> metricses = this.metricsRepositoryWrapper.findByLoanId(loanId);
            // order stream by getRank desc but less than current rank and pick the first
            final List<Metrics> metricsesAhead = metricses.stream()
                    .filter(action -> Objects.equals(action.getStatus(), LoanApprovalStatus.APPROVED.getValue()) && action.getRank() < rank)
                    .sorted(Comparator.comparingInt(Metrics::getRank).reversed()).toList();
            final long totalUndoCount = metricsesAhead.stream().count();
            if (totalUndoCount == 0) {
                throw new PlatformDataIntegrityException("error.msg.metrics", "Approval not allowed for undo.");
            } else {

                Metrics undoToMetrics = null;
                if (this.fromApiJsonHelper.parameterExists(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element)) {
                    final Long undoToMetricsId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element);
                    undoToMetrics = this.metricsRepositoryWrapper.findOneWithNotFoundDetection(undoToMetricsId);
                    undoToMetricsStateCheck(undoToMetrics, loanId);
                }

                Metrics pickTheLastMetricApproval;
                if (undoToMetrics != null) {
                    pickTheLastMetricApproval = undoToMetrics;
                } else {
                    pickTheLastMetricApproval = metricsesAhead.stream().findFirst().orElseThrow(
                            () -> new MetricsNotFoundException("Last approval not found for loan account: {}." + loan.getAccountNumber()));
                }
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
                .withEntityId(metricsId).withLoanId(loanId).build();
    }

    protected void undoToMetricsStateCheck(Metrics undoToMetrics, final Long loanId) throws PlatformDataIntegrityException {
        if (!Objects.equals(undoToMetrics.getStatus(), LoanApprovalStatus.APPROVED.getValue())
                || !Objects.equals(loanId, undoToMetrics.getLoan().getId())) {
            throw new PlatformDataIntegrityException("error.loan.metrics",
                    "Loan Approval cannot be returned to selected state.");
        }
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
                // update loan to status for rejection
                UpdateLoanStatus(loan, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL.getValue());
            }
            loanReject(loan, noteText, today);

            saveNoteMetrics(noteText, loan);

            final Long lienSavingsTransactionId = getLienSavingsTransactionIdLinkToLoan(loanId);
            if (lienSavingsTransactionId != null) {
                //unLien all held Amount
                final Long savingsId = getLoanLinkedSavingsId(loanId);
                //release Amount if loan is rejected
                releaseAmount(savingsId, lienSavingsTransactionId, this.commandsSourceWritePlatformService);
            }
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
                .withEntityId(metricsId).withLoanId(loanId).build();
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

                saveNoteMetrics("Reassign loan from " + oldStaff.displayName() + "to a new approval officer" + newStaff.displayName(),
                        loan);

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
                .withEntityId(metricsId).build();
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
