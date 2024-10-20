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
package org.apache.fineract.portfolio.savings.api.business;

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.accountNoParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.idParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.noteParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.paymentDetailDataParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.reversedParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.runningBalanceParamName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SavingsBusinessApiSetConstants {

    public static final String RESOURCE_NAME = "savingsaccount";
    public static final String COMMAND_UNDO_BULK_TRANSACTION = "undoBulk";

    public static final String startPeriodParameterName = "startPeriod";
    public static final String endPeriodParameterName = "endPeriod";
    public static final String refNoParamName = "refNo";

    public static final String dataParamName = "data";
    public static final String savingsIdParamName = "savingsId";
    public static final String walletIdParamName = "walletId";

    protected static final Set<String> SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(idParamName, "accountId", accountNoParamName, "currency", "amount", dateParamName, paymentDetailDataParamName,
                    runningBalanceParamName, reversedParamName, noteParamName, "chargeData", "submittedOnDateTime", refNoParamName));
}
