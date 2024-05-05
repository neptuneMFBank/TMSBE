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
package org.apache.fineract.portfolio.business.bankTransfer.service;

import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.bankTransfer.api.TransferApprovalApiResourceConstants;
import org.apache.fineract.portfolio.business.bankTransfer.data.TransferApprovalDataValidator;
import org.apache.fineract.portfolio.business.bankTransfer.domain.TransferApproval;
import org.apache.fineract.portfolio.business.bankTransfer.domain.TransferApprovalRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferApprovalWritePlatformServiceJpaRepositoryImpl implements TransferApprovalWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(TransferApprovalWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final TransferApprovalRepositoryWrapper repository;
    private final TransferApprovalDataValidator fromApiJsonDataValidator;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public TransferApprovalWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final TransferApprovalRepositoryWrapper repository,
            final TransferApprovalDataValidator fromApiJsonDataValidator,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.context = context;
        this.repository = repository;
        this.fromApiJsonDataValidator = fromApiJsonDataValidator;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.context.authenticatedUser();
        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final BigDecimal amount = command.bigDecimalValueOfParameterNamed(TransferApprovalApiResourceConstants.AMOUNT);
//            final Integer status = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.STATUS);

            final Integer transferType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.TRANSFER_TYPE);
            final Integer holdTransactionId = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.HOLD_TRANSACTION_ID);
            final Integer releaseTransactionId = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.RELEASE_TRANSACTION_ID);
            final Integer withdrawTransactionId = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.WITHDRAW_TRANSACTION_ID);
            final Integer fromAccountId = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_ID);
            final Integer fromAccountType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_TYPE);
            final String fromAccountNumber = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_NUMBER);
            final Integer toAccountId = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_ID);
            final Integer toAccountType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_TYPE);
            final String toAccountNumber = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER);

            CodeValue activationChannel = null;
            final Long activationChannelId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.ACTIVATION_CHANNEL_ID);
            if (activationChannelId != null) {
                activationChannel = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(activationChannelId);
            }

            CodeValue toBank = null;
            final Long toBankId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_BANK_ID);
            if (toBankId != null) {
                toBank = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(toBankId);
            }

            final String reason = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.REASON);

            final TransferApproval transferApproval = TransferApproval.instance(amount, SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(), transferType, holdTransactionId, releaseTransactionId,
                    withdrawTransactionId, fromAccountId, fromAccountType, fromAccountNumber, toAccountId, toAccountType,
                    toAccountNumber, activationChannel, toBank, reason);

            this.repository.saveAndFlush(transferApproval);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(transferApproval.getId()) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dae) {

        logAsErrorUnexpectedDataIntegrityException(dae);
        throw new PlatformDataIntegrityException("error.msg.Transfer.Approval.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dae) {
        LOG.error("Error occured.", dae);
    }

    @Transactional
    @Override
    public CommandProcessingResult approve(final JsonCommand command, final Long transferApprovalId) {
        this.context.authenticatedUser();
        this.fromApiJsonDataValidator.validateApproval(command.json());

        final TransferApproval transferApproval = this.repository.findOneWithNotFoundDetection(transferApprovalId);

        final Map<String, Object> changes = transferApproval.ApproveTransfer(command);

        if (!changes.isEmpty()) {
            if (changes.containsKey(TransferApprovalApiResourceConstants.STATUS)) {

            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transferApproval.getId()) //

                .with(changes) //
                .build();

    }

    @Transactional
    @Override
    public CommandProcessingResult reject(final JsonCommand command, final Long transferApprovalId) {
        this.context.authenticatedUser();

        this.fromApiJsonDataValidator.validateRejection(command.json());

        final TransferApproval transferApproval = this.repository.findOneWithNotFoundDetection(transferApprovalId);

        final Map<String, Object> changes = transferApproval.RejectTransfer(command);

        if (!changes.isEmpty()) {
            if (changes.containsKey(TransferApprovalApiResourceConstants.STATUS)) {

            }
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transferApproval.getId()) //

                .with(changes) //
                .build();
    }

}
