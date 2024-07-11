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
package org.apache.fineract.portfolio.business.accounttier.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AccountTierApiResouceConstants {

    private AccountTierApiResouceConstants() {

    }

    public static final String RESOURCE_NAME = "ACCOUNTTIER";

    public static final String ID = "id";
    public static final String PARENT_ID = "parentId";
    public static final String CLIENT_TYPE_ID = "clientTypeId";
    public static final String ACTIVATION_CHANNEL_ID = "ActivationChannelId";
    public static final String DALIY_WITHDRAWAL_LIMIT = "dailyWithdrawalLimit";
    public static final String SINGLE_DEPOSIT_LIMIT = "singleDepositLimit";
    public static final String CUMULATIVE_BALANCE = "cumulativeBalance";
    public static final String DESCRIPTION = "description";
    public static final String LOCALE = "locale";
    public static final String NAME = "name";

    public static final String CLIENT_TYPE = "ClientType";
    public static final String CLIENT_CLASSIFICATION = "ClientClassification";
    public static final String ACTIVATION_CHANNEL = "ActivationChannel";

    static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, PARENT_ID, CLIENT_TYPE_ID, ACTIVATION_CHANNEL_ID,
            DALIY_WITHDRAWAL_LIMIT, SINGLE_DEPOSIT_LIMIT, CUMULATIVE_BALANCE, DESCRIPTION, NAME));
    public static final Set<String> PARENT_REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(PARENT_ID, CLIENT_TYPE_ID,
            ACTIVATION_CHANNEL_ID, DALIY_WITHDRAWAL_LIMIT, SINGLE_DEPOSIT_LIMIT, CUMULATIVE_BALANCE, DESCRIPTION, LOCALE, NAME));
    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(PARENT_ID, ACTIVATION_CHANNEL_ID,
            DALIY_WITHDRAWAL_LIMIT, SINGLE_DEPOSIT_LIMIT, CUMULATIVE_BALANCE, DESCRIPTION, LOCALE, NAME));

}
