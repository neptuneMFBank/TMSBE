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
package org.apache.fineract.portfolio.business.accounttier.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;

public final class AccountTierData implements Serializable {

    private final Long id;

    private final CodeValueData clientType;

    private final Long parentId;

    private final CodeValueData activationChannel;

    private final BigDecimal dailyWithdrawalLimit;

    private final BigDecimal singleDepositLimit;

    private final BigDecimal cumulativeBalance;

    private final String description;

    private final String name;

    private Collection<AccountTierData> childrenAccountTierData;

    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> channelOptions;

    private AccountTierData(final Long id, final CodeValueData clientType, final Long parentId, final CodeValueData activationChannel,
            final BigDecimal dailyWithdrawalLimit, final BigDecimal singleDepositLimit, final BigDecimal cumulativeBalance, final String description,
            final String name, final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> channelOptions) {
        this.id = id;
        this.clientType = clientType;
        this.parentId = parentId;
        this.activationChannel = activationChannel;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
        this.singleDepositLimit = singleDepositLimit;
        this.cumulativeBalance = cumulativeBalance;
        this.description = description;
        this.name = name;
        this.channelOptions = channelOptions;
        this.clientTypeOptions = clientTypeOptions;
    }

    public static AccountTierData instance(final Long id, final CodeValueData clientType, final Long parentId, final CodeValueData activationChannel,
            final BigDecimal dailyWithdrawalLimit, final BigDecimal singleDepositLimit, final BigDecimal cumulativeBalance, final String description,
            final String name) {
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> channelOptions = null;
        return new AccountTierData(id, clientType, parentId, activationChannel,
                dailyWithdrawalLimit, singleDepositLimit, cumulativeBalance, description, name, clientTypeOptions, channelOptions);

    }

    public Long getParentId() {
        return parentId;
    }

    public void setChildrenAccountTierData(Collection<AccountTierData> childrenAccountTierData) {
        this.childrenAccountTierData = childrenAccountTierData;
    }

    public Long getId() {
        return id;
    }

    public static AccountTierData template(final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> channelOptions) {
        final Long id = null;
        final CodeValueData clientType = null;
        final Long parentId = null;
        final CodeValueData activationChannel = null;
        final BigDecimal dailyWithdrawalLimit = null;
        final BigDecimal singleDepositLimit = null;
        final BigDecimal cumulativeBalance = null;
        final String name = null;
        final String description = null;

        return new AccountTierData(id, clientType, parentId, activationChannel,
                dailyWithdrawalLimit, singleDepositLimit, cumulativeBalance, description, name, clientTypeOptions, channelOptions);
    }

}
