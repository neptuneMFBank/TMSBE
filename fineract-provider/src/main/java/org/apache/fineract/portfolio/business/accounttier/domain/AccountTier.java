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
package org.apache.fineract.portfolio.business.accounttier.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.business.accounttier.api.AccountTierApiResouceConstants;

@Entity
@Table(name = "m_account_tier_limit")
public class AccountTier extends AbstractAuditableWithUTCDateTimeCustom {

    @OneToOne
    @JoinColumn(name = "client_type_cv_id", nullable = false)
    private CodeValue clientType;

    @Column(name = "parent_id")
    private Long parentId;

    @OneToOne
    @JoinColumn(name = "activation_channel_id")
    private CodeValue activationChannel;

    @Column(name = "daily_withdrawal_limit")
    private BigDecimal dailyWithdrawalLimit;

    @Column(name = "single_deposit_limit")
    private BigDecimal singleDepositLimit;

    @Column(name = "cumulative_balance")
    private BigDecimal cumulativeBalance;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    public AccountTier() {}

    private AccountTier(CodeValue clientType, Long parentId, CodeValue activationChannel, BigDecimal dailyWithdrawalLimit,
            BigDecimal singleDepositLimit, BigDecimal cumulativeBalance, String description, String name) {
        this.clientType = clientType;
        this.parentId = parentId;
        this.activationChannel = activationChannel;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
        this.singleDepositLimit = singleDepositLimit;
        this.cumulativeBalance = cumulativeBalance;
        this.description = description;
        this.name = name;
    }

    public static AccountTier instance(CodeValue clientType, Long parentId, CodeValue activationChannel, BigDecimal dailyWithdrawalLimit,
            BigDecimal singleDepositLimit, BigDecimal cumulativeBalance, String description, String name) {
        return new AccountTier(clientType, parentId, activationChannel, dailyWithdrawalLimit, singleDepositLimit, cumulativeBalance,
                description, name);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInLongParameterNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, this.activationChannel.getId())) {
            final Long newValue = command.longValueOfParameterNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID);
            actualChanges.put(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, newValue);
        }

        if (command.isChangeInLongParameterNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID, this.clientType.getId())) {
            final String newValue = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID);
            actualChanges.put(AccountTierApiResouceConstants.CLIENT_TYPE_ID, newValue);
        }

        if (command.isChangeInBigDecimalParameterNamed(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, this.cumulativeBalance)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.CUMULATIVE_BALANCE);
            actualChanges.put(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, newValue);
            this.cumulativeBalance = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, this.dailyWithdrawalLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT);
            actualChanges.put(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, newValue);
            this.dailyWithdrawalLimit = newValue;
        }

        if (command.isChangeInStringParameterNamed(AccountTierApiResouceConstants.DESCRIPTION, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.DESCRIPTION);
            actualChanges.put(AccountTierApiResouceConstants.DESCRIPTION, newValue);
            this.description = newValue;
        }

        if (command.isChangeInLongParameterNamed(AccountTierApiResouceConstants.PARENT_ID, this.parentId)) {
            final Long newValue = command.longValueOfParameterNamed(AccountTierApiResouceConstants.PARENT_ID);
            actualChanges.put(AccountTierApiResouceConstants.PARENT_ID, newValue);
            this.parentId = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, this.singleDepositLimit)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT);
            actualChanges.put(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, newValue);
            this.singleDepositLimit = newValue;
        }

        if (command.isChangeInStringParameterNamed(AccountTierApiResouceConstants.NAME, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(AccountTierApiResouceConstants.NAME);
            actualChanges.put(AccountTierApiResouceConstants.NAME, newValue);
            this.name = newValue;
        }
        return actualChanges;
    }

    public void setClientType(CodeValue clientType) {
        this.clientType = clientType;
    }

    public void setActivationChannel(CodeValue activationChannel) {
        this.activationChannel = activationChannel;
    }

    public CodeValue getClientType() {
        return clientType;
    }

}
