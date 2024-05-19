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
package org.apache.fineract.portfolio.business.bankTransfer.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class TransferApprovalApiResourceConstants {

    private TransferApprovalApiResourceConstants() {

    }

    public static final String RESOURCE_NAME = "TRANSFERAPPROVAL";

    public static final String AMOUNT = "amount";
    public static final String STATUS = "status";
    public static final String TRANSFER_TYPE = "transferType";
    public static final String TRANSFER_TYPE_OPTIONS = "transferTypeOptions";
    public static final String HOLD_TRANSACTION_ID = "holdTransactionId";
    public static final String RELEASE_TRANSACTION_ID = "releaseTransactionId";
    public static final String WITHDRAW_TRANSACTION_ID = "withdrawTransactionId";
    public static final String FROM_ACCOUNT_ID = "fromAccountId";
    public static final String FROM_ACCOUNT_TYPE = "fromAccountType";
    public static final String FROM_ACCOUNT_NUMBER = "fromAccountNumber";
    public static final String TO_ACCOUNT_ID = "toAccountId";
    public static final String TO_ACCOUNT_TYPE = "toAccountType";
    public static final String TO_ACCOUNT_NUMBER = "toAccountNumber";
    public static final String ACTIVATION_CHANNEL_ID = "activationChannelId";
    public static final String TO_BANK_ID = "toBankId";
    public static final String REASON = "reason";

    public static String approvalIdTobeApproved = "approvalTransferId";

    public static final String approvedOnDateParameterName = "approvedOnDate";
    public static final String noteParameterName = "note";
    public static final String localeParameterName = "locale";

    public static final String dateFormatParameterName = "dateFormat";
    public static final String rejectedOnDateParameterName = "rejectedOnDate";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(AMOUNT, STATUS, TRANSFER_TYPE,
            HOLD_TRANSACTION_ID, RELEASE_TRANSACTION_ID, WITHDRAW_TRANSACTION_ID, FROM_ACCOUNT_ID, FROM_ACCOUNT_TYPE, FROM_ACCOUNT_NUMBER,
            TO_ACCOUNT_ID, TO_ACCOUNT_TYPE, TO_ACCOUNT_NUMBER, ACTIVATION_CHANNEL_ID, TO_BANK_ID, REASON, TRANSFER_TYPE_OPTIONS));

    public static final Set<String> RESQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(AMOUNT, TRANSFER_TYPE,
            FROM_ACCOUNT_ID, FROM_ACCOUNT_TYPE, FROM_ACCOUNT_NUMBER,
            TO_ACCOUNT_ID, TO_ACCOUNT_TYPE, TO_ACCOUNT_NUMBER, ACTIVATION_CHANNEL_ID, TO_BANK_ID, localeParameterName));

}
