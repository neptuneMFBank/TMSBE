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
package org.apache.fineract.portfolio.account.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class AccountTransfersBusinessApiConstants {

    private AccountTransfersBusinessApiConstants() {

    }

    public static final String ACCOUNT_TRANSFER_RESOURCE_NAME = "accounttransfer";

    public static final String toAccountTypeParamName = "toAccountType";
    public static final String fromAccountIdParamName = "fromAccountId";
    public static final String keyParam = "key";
    public static final String valueParam = "value";
    public static final String localeParamName = "locale";

    public static final Set<String> SINGLE_TEMPLATE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(toAccountTypeParamName, fromAccountIdParamName, keyParam, valueParam, localeParamName));
}
