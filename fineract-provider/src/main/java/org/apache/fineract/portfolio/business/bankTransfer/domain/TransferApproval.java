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
package org.apache.fineract.portfolio.business.bankTransfer.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.business.bankTransfer.api.TransferApprovalApiResourceConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;

@Entity
@Table(name = "m_transfer_approval")
public class TransferApproval extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "status")
    private Integer status;

    @Column(name = "transfer_type", nullable = false)
    private Integer transferType;

    @Column(name = "hold_transaction_id")
    private Long holdTransactionId;

    @Column(name = "release_transaction_id")
    private Long releaseTransactionId;

    @Column(name = "withdraw_transaction_id")
    private Long withdrawTransactionId;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "from_account_type", nullable = false)
    private Integer fromAccountType;

    @Column(name = "from_account_number")
    private String fromAccountNumber;

    @Column(name = "to_account_id")
    private Long toAccountId;

    @Column(name = "to_account_type")
    private Integer toAccountType;

    @Column(name = "to_account_number")
    private String toAccountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activation_channel_id")
    private CodeValue activationChannelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_bank_id")
    private CodeValue toBankId;

    @Column(name = "reason")
    private String reason;

    private TransferApproval(BigDecimal amount, Integer status, Integer transferType, Long holdTransactionId, Long releaseTransactionId,
            Long withdrawTransactionId, Long fromAccountId, Integer fromAccountType, String fromAccountNumber,
            Long toAccountId, Integer toAccountType, String toAccountNumber, CodeValue activationChannelId, CodeValue toBankId, String reason) {
        this.amount = amount;
        this.status = status;
        this.transferType = transferType;
        this.holdTransactionId = holdTransactionId;
        this.releaseTransactionId = releaseTransactionId;
        this.withdrawTransactionId = withdrawTransactionId;
        this.fromAccountId = fromAccountId;
        this.fromAccountType = fromAccountType;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountType = toAccountType;
        this.toAccountNumber = toAccountNumber;
        this.activationChannelId = activationChannelId;
        this.toBankId = toBankId;
        this.reason = reason;
    }

    public TransferApproval() {
    }

    public static TransferApproval instance(BigDecimal amount, Integer status, Integer transferType, Long holdTransactionId,
            Long fromAccountId, Integer fromAccountType, String fromAccountNumber,
            Long toAccountId, Integer toAccountType, String toAccountNumber, CodeValue activationChannelId, CodeValue toBankId) {
        final Long releaseTransactionId = null;
        final Long withdrawTransactionId = null;
        final String reason = null;
        return new TransferApproval(amount, status, transferType, holdTransactionId, releaseTransactionId,
                withdrawTransactionId, fromAccountId, fromAccountType, fromAccountNumber, toAccountId, toAccountType,
                toAccountNumber, activationChannelId, toBankId, reason);

    }

    public SavingsAccountStatusType status() {
        return SavingsAccountStatusType.fromInt(this.status);
    }

    public boolean isSubmittedAndPendingApproval() {
        return status().isSubmittedAndPendingApproval();
    }

    public Map<String, Object> ApproveTransfer(final JsonCommand command) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(TransferApprovalApiResourceConstants.RESOURCE_NAME);

        checkSubmittedState(dataValidationErrors);

        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        this.status = SavingsAccountStatusType.APPROVED.getValue();
        this.reason = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.REASON);
        actualChanges.put(TransferApprovalApiResourceConstants.STATUS, SavingsEnumerations.status(this.status));
        actualChanges.put(TransferApprovalApiResourceConstants.REASON, this.reason);

//        final Locale locale = command.extractLocale();
//
//        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
//        LocalDate approvedOn = command.localDateValueOfParameterNamed(TransferApprovalApiResourceConstants.approvedOnDateParameterName);
//
//        actualChanges.put("locale", locale);
//        actualChanges.put(TransferApprovalApiResourceConstants.approvalIdTobeApproved, command.entityId());
//        actualChanges.put("dateFormat", command.dateFormat());
//        actualChanges.put("approvedOnDate", approvedOn.format(fmt));
//
//        final OffsetDateTime submittalDate = this.getCreatedDate().orElse(OffsetDateTime.now(ZoneId.systemDefault()));
//        OffsetDateTime approvedOnOffSet = OffsetDateTime.of(approvedOn.atTime(LocalTime.MAX), ZoneOffset.UTC);
//
//        if (approvedOnOffSet.isBefore(submittalDate)) {
//            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.approvedOnDateParameterName).value(approvedOn)
//                    .failWithCode("must.be.after.createdOn.date");
//            if (!dataValidationErrors.isEmpty()) {
//                throw new PlatformApiDataValidationException(dataValidationErrors);
//            }
//        }
        return actualChanges;

    }

    public Map<String, Object> RejectTransfer(final JsonCommand command) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        checkSubmittedState(dataValidationErrors);
        //final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
        //      .resource(TransferApprovalApiResourceConstants.RESOURCE_NAME);
        final Map<String, Object> actualChanges = new LinkedHashMap<>();

        this.status = SavingsAccountStatusType.REJECTED.getValue();
        this.reason = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.REASON);
        actualChanges.put(TransferApprovalApiResourceConstants.STATUS, SavingsEnumerations.status(this.status));
        actualChanges.put(TransferApprovalApiResourceConstants.REASON, this.reason);

        //final Locale locale = command.extractLocale();
        //final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
        //LocalDate rejectedOn = command.localDateValueOfParameterNamed(TransferApprovalApiResourceConstants.rejectedOnDateParameterName);
        //actualChanges.put("locale", command.locale());
        //actualChanges.put(TransferApprovalApiResourceConstants.approvalIdTobeApproved, command.entityId());
        //actualChanges.put("dateFormat", command.dateFormat());
        //actualChanges.put(TransferApprovalApiResourceConstants.rejectedOnDateParameterName, rejectedOn.format(fmt));
//        final OffsetDateTime submittalDate = this.getCreatedDate().orElse(OffsetDateTime.now(ZoneId.systemDefault()));
//        OffsetDateTime rejectedOnOffSet = OffsetDateTime.of(rejectedOn.atTime(LocalTime.MAX), ZoneOffset.UTC);
//
//        if (rejectedOnOffSet.isBefore(submittalDate)) {
//            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.rejectedOnDateParameterName).value(rejectedOn)
//                    .failWithCode("must.be.after.createdOn.date");
//            if (!dataValidationErrors.isEmpty()) {
//                throw new PlatformApiDataValidationException(dataValidationErrors);
//            }
//        }
        return actualChanges;

    }

    private void checkSubmittedState(final List<ApiParameterError> dataValidationErrors) throws PlatformApiDataValidationException {
        if (!isSubmittedAndPendingApproval()) {
            final String defaultUserMessage = "Transfer Approval is not allowed. Transfer Approval is not in submitted and pending approval state.";
            final ApiParameterError error = ApiParameterError
                    .generalError("error.msg.transfer.approval.is.not.submitted.and.pending.state", defaultUserMessage);
            dataValidationErrors.add(error);
        }

        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public Long getHoldTransactionId() {
        return holdTransactionId;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setReleaseTransactionId(Long releaseTransactionId) {
        this.releaseTransactionId = releaseTransactionId;
    }

    public void setWithdrawTransactionId(Long withdrawTransactionId) {
        this.withdrawTransactionId = withdrawTransactionId;
    }

    public Integer getTransferType() {
        return transferType;
    }

    public Integer getFromAccountType() {
        return fromAccountType;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public Integer getToAccountType() {
        return toAccountType;
    }

    public String getReason() {
        return reason;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public CodeValue getToBankId() {
        return toBankId;
    }

}
