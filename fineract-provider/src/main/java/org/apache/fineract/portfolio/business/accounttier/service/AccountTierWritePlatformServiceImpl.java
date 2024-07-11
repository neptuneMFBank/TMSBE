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
package org.apache.fineract.portfolio.business.accounttier.service;

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
import org.apache.fineract.portfolio.business.accounttier.api.AccountTierApiResouceConstants;
import org.apache.fineract.portfolio.business.accounttier.data.AccountTierDataValidator;
import org.apache.fineract.portfolio.business.accounttier.domain.AccountTier;
import org.apache.fineract.portfolio.business.accounttier.domain.AccountTierRepository;
import org.apache.fineract.portfolio.business.accounttier.execption.AccountTierNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTierWritePlatformServiceImpl implements AccountTierWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(AccountTierWritePlatformServiceImpl.class);

    private final AccountTierDataValidator accountTierDataValidator;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final AccountTierRepository accountTierRepository;
    private final PlatformSecurityContext context;

    @Autowired
    public AccountTierWritePlatformServiceImpl(final AccountTierDataValidator accountTierDataValidator,
            final CodeValueRepositoryWrapper codeValueRepository, final AccountTierRepository accountTierRepository,
            final PlatformSecurityContext context) {
        this.accountTierDataValidator = accountTierDataValidator;
        this.codeValueRepository = codeValueRepository;
        this.accountTierRepository = accountTierRepository;
        this.context = context;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.context.authenticatedUser();
        try {
            this.accountTierDataValidator.validateForCreate(command.json());

            final Long activationChannelId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID);
            CodeValue activationChannel = null;
            if (activationChannelId != null) {
                activationChannel = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        AccountTierApiResouceConstants.ACTIVATION_CHANNEL, activationChannelId);
            }

            final BigDecimal cumulativeBalance = command.bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.CUMULATIVE_BALANCE);
            final BigDecimal singleDepositLimit = command
                    .bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT);
            final BigDecimal dailyWithdrawalLimit = command
                    .bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT);
            final String description = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.DESCRIPTION);
            final String name = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.NAME);
            final Long parentId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.PARENT_ID);

            CodeValue clientType = null;
            if (parentId != null) {
                AccountTier parentAccountTier = this.accountTierRepository.findById(parentId)
                        .orElseThrow(() -> new AccountTierNotFoundException(parentId));
                clientType = parentAccountTier.getClientType();
            } else {
                final Long clientTypeId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID);
                if (clientTypeId != null) {
                    clientType = this.codeValueRepository
                            .findOneByCodeNameAndIdWithNotFoundDetection(AccountTierApiResouceConstants.CLIENT_TYPE, clientTypeId);
                }
            }

            AccountTier accountTier = AccountTier.instance(clientType, parentId, activationChannel, dailyWithdrawalLimit,
                    singleDepositLimit, cumulativeBalance, description, name);
            this.accountTierRepository.saveAndFlush(accountTier);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(accountTier.getId()) //
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

    @Transactional
    @Override
    public CommandProcessingResult update(final Long accountTierId, final JsonCommand command) {

        try {
            this.context.authenticatedUser();
            final AccountTier accountTier = this.accountTierRepository.findById(accountTierId)
                    .orElseThrow(() -> new AccountTierNotFoundException(accountTierId));

            this.accountTierDataValidator.validateForUpdate(command.json());

            final Map<String, Object> changes = accountTier.update(command);

            if (changes.containsKey(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID)) {
                final Long activationChannelId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID);
                CodeValue activationChannel = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        AccountTierApiResouceConstants.ACTIVATION_CHANNEL, activationChannelId);
                accountTier.setActivationChannel(activationChannel);
            }

            if (changes.containsKey(AccountTierApiResouceConstants.CLIENT_TYPE_ID)) {
                final Long clientTypeId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID);
                CodeValue clientType = this.codeValueRepository
                        .findOneByCodeNameAndIdWithNotFoundDetection(AccountTierApiResouceConstants.CLIENT_TYPE, clientTypeId);
                accountTier.setClientType(clientType);
            }
            final Long parentId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.PARENT_ID);

            if (changes.containsKey(AccountTierApiResouceConstants.PARENT_ID) && parentId != null) {

                AccountTier parentAccountTier = this.accountTierRepository.findById(parentId)
                        .orElseThrow(() -> new AccountTierNotFoundException(parentId));

                accountTier.setClientType(parentAccountTier.getClientType());
            }
            if (!changes.isEmpty()) {
                this.accountTierRepository.saveAndFlush(accountTier);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(accountTier.getId()) //
                    .with(changes).build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long accountTierId) {

        this.context.authenticatedUser();
        final AccountTier accountTier = this.accountTierRepository.findById(accountTierId)
                .orElseThrow(() -> new AccountTierNotFoundException(accountTierId));

        this.accountTierRepository.delete(accountTier);

        return new CommandProcessingResultBuilder() //
                .withEntityId(accountTier.getId()) //
                .build();
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dae) {

        if (realCause.getMessage().contains("tier_UNIQUE_parent_channel")) {

            final Long ParentId = command.longValueOfParameterNamed(AccountTierApiResouceConstants.PARENT_ID);
            final Long channel = command.longValueOfParameterNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID);

            throw new PlatformDataIntegrityException("error.msg.account.tier.duplicate.",
                    "Account tier  with parent Id  `" + ParentId + "` for channel " + channel + " already exist ", "name", ParentId);
        }
        if (realCause.getMessage().contains("m_account_tier_limit_name")) {

            final String name = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.NAME);

            throw new PlatformDataIntegrityException("error.msg.account.tier.duplicate.name",
                    "Account tier  with name  " + name + " already exist ", "name", name);
        }

        logAsErrorUnexpectedDataIntegrityException(dae);
        throw new PlatformDataIntegrityException("error.msg.account.tier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dae) {
        LOG.error("Error occured.", dae);
    }
}
