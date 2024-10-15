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

import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.PAYMENT_TYPE_DEDUCTION;

import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.business.bankTransfer.api.TransferApprovalApiResourceConstants;
import org.apache.fineract.portfolio.business.bankTransfer.data.TransferApprovalDataValidator;
import org.apache.fineract.portfolio.business.bankTransfer.domain.BankTransferType;
import org.apache.fineract.portfolio.business.bankTransfer.domain.TransferApproval;
import org.apache.fineract.portfolio.business.bankTransfer.domain.TransferApprovalRepositoryWrapper;
import org.apache.fineract.portfolio.business.bankTransfer.exception.TransferApprovalNotFoundException;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
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
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final Long paymentTypeDeductionId;

    @Autowired
    public TransferApprovalWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ApplicationContext applicationContext, final TransferApprovalRepositoryWrapper repository,
            final TransferApprovalDataValidator fromApiJsonDataValidator,
            final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.repository = repository;
        this.fromApiJsonDataValidator = fromApiJsonDataValidator;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.savingsAccountRepositoryWrapper = savingsAccountRepositoryWrapper;

        Environment environment = applicationContext.getEnvironment();
        this.paymentTypeDeductionId = Long.valueOf(environment.getProperty(PAYMENT_TYPE_DEDUCTION));
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.context.authenticatedUser();
        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final BigDecimal amount = command.bigDecimalValueOfParameterNamed(TransferApprovalApiResourceConstants.AMOUNT);
            // final Integer status = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.STATUS);

            final Integer transferType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.TRANSFER_TYPE);
            final Long fromAccountId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_ID);
            final Integer fromAccountType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_TYPE);
            final PortfolioAccountType fromPortfolioAccountType = PortfolioAccountType.fromInt(fromAccountType);
            String fromAccountName = null;
            if (!fromPortfolioAccountType.isSavingsAccount()) {
                throw new TransferApprovalNotFoundException("Sender Account type not supported");
            } else {
                final SavingsAccount fromSavingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(fromAccountId);
                fromAccountName = StringUtils.defaultIfBlank(fromSavingsAccount.getClient().getDisplayName(), "N/A");
            }
            final String note = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.noteParameterName);
            final String fromAccountNumber = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_NUMBER);
            final Long toAccountId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_ID);
            String externalTransferInfo = "IntraBank";
            if (toAccountId == null) {
                externalTransferInfo = "InterBank";
            }
            final Integer toAccountType = command.integerValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_TYPE);
            if (toAccountType != null) {
                final PortfolioAccountType toPortfolioAccountType = PortfolioAccountType.fromInt(toAccountType);
                if (!toPortfolioAccountType.isSavingsAccount()) {
                    throw new TransferApprovalNotFoundException("Receiver Account type not supported");
                }
            }
            final String toAccountNumber = command.stringValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER);

            final Long activationChannelId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.ACTIVATION_CHANNEL_ID);
            final CodeValue activationChannel = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(activationChannelId);

            CodeValue toBank = null;
            final Long toBankId = command.longValueOfParameterNamed(TransferApprovalApiResourceConstants.TO_BANK_ID);
            if (toBankId != null) {
                toBank = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(toBankId);
            }

            // create a process to holdOff the customeer Amount from his balance
            final Long holdTransactionId = GeneralConstants.holdAmount(amount, null, fromAccountId,
                    externalTransferInfo + " transfer from " + fromAccountNumber + " to " + toAccountNumber,
                    commandsSourceWritePlatformService);

            final TransferApproval transferApproval = TransferApproval.instance(amount,
                    SavingsAccountStatusType.SUBMITTED_AND_PENDING_APPROVAL.getValue(), transferType, holdTransactionId, fromAccountId,
                    fromAccountType, fromAccountNumber, toAccountId, toAccountType, toAccountNumber, activationChannel, toBank,
                    fromAccountName, note);

            this.repository.saveAndFlush(transferApproval);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(transferApproval.getId()) //
                    .withSubEntityId(holdTransactionId).build();
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
        final String fromAccountNumber = transferApproval.getFromAccountNumber();
        final String toAccountNumber = transferApproval.getToAccountNumber();

        final Map<String, Object> changes = transferApproval.ApproveTransfer(command);
        final Integer transferType = transferApproval.getTransferType();

        if (!changes.isEmpty()) {
            final Integer fromAccountType = transferApproval.getFromAccountType();
            final PortfolioAccountType fromPortfolioAccountType = PortfolioAccountType.fromInt(fromAccountType);
            if (!fromPortfolioAccountType.isSavingsAccount()) {
                throw new TransferApprovalNotFoundException("Sender Account type not supported");
            }

            final BankTransferType bankTransferType = BankTransferType.fromInt(transferType);

            // release Amount
            transferReleaseProcess(transferApproval);

            final Long fromAccountId = transferApproval.getFromAccountId();
            final SavingsAccount fromSavingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(fromAccountId);
            final String note = transferApproval.getNote();
            final BigDecimal amount = transferApproval.getAmount();
            if (bankTransferType.isIntraBank()) {
                // if intraBank
                // call intraBank process

                final Long toAccountId = transferApproval.getToAccountId();
                final Integer toAccountType = transferApproval.getToAccountType();

                final Long fromOfficeId = fromSavingsAccount.officeId();
                final Long fromClientId = fromSavingsAccount.clientId();

                final SavingsAccount toSavingsAccount = this.savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(toAccountId);
                final Long toOfficeId = toSavingsAccount.officeId();
                final Long toClientId = toSavingsAccount.clientId();

                final Long withdrawalId = GeneralConstants.intrabankTransfer(transferApprovalId, amount, fromOfficeId, fromClientId,
                        fromAccountId, fromAccountType, toOfficeId, toClientId, toAccountId, toAccountType, note,
                        commandsSourceWritePlatformService);
                transferApproval.setWithdrawTransactionId(withdrawalId);
            } else {
                // we are withdrawing for now, until we conclude which transfer integration Service will take place (E.g
                // NIBSS NIP/EASYPAY etc)
                final Long withdrawalId = GeneralConstants.withdrawAmount(amount, fromAccountId, note + "-" + transferApprovalId,
                        toAccountNumber, paymentTypeDeductionId, commandsSourceWritePlatformService);
                transferApproval.setWithdrawTransactionId(withdrawalId);

                // other process for interBank
                if (bankTransferType.isInterBankEbills()) {
                    final CodeValue toBank = transferApproval.getToBankId();
                    // call the microservice eBills endPoint
                    // throw new TransferApprovalNotFoundException("Transfer type not supported");
                    changes.put(TransferApprovalApiResourceConstants.TRANSFER_TYPE, transferType);
                    changes.put(TransferApprovalApiResourceConstants.FROM_ACCOUNT_NUMBER, fromAccountNumber);
                    changes.put(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER, toAccountNumber);
                    changes.put("bankCode", toBank);
                }
                // else {
                // throw new TransferApprovalNotFoundException("Transfer type not supported");
                // }
            }
            this.repository.saveAndFlush(transferApproval);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transferApprovalId) //
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
            transferReleaseProcess(transferApproval);
            this.repository.saveAndFlush(transferApproval);
        }
        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transferApproval.getId()) //

                .with(changes) //
                .build();
    }

    protected void transferReleaseProcess(final TransferApproval transferApproval) {
        final Long fromAccountId = transferApproval.getFromAccountId();
        final Long holdTransactionId = transferApproval.getHoldTransactionId();
        // create a process to release the customer Amount back to his balance
        final Long releaseTransactionId = GeneralConstants.releaseAmount(fromAccountId, holdTransactionId,
                commandsSourceWritePlatformService);
        transferApproval.setReleaseTransactionId(releaseTransactionId);
    }

}
