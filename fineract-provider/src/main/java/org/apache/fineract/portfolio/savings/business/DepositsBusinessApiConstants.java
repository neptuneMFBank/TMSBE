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
package org.apache.fineract.portfolio.savings.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;

public final class DepositsBusinessApiConstants {

    private DepositsBusinessApiConstants() {

    }

    // Deposit products
    public static final String RESOURCE_NAME = "DEPSOITS";

    private static final String IDPARAM = "id";
    private static final String accountNoPARAM = "accountNo";
    private static final String depositTypePARAM = "depositType";
    private static final String statusPARAM = "status";

    private static final String clientIdPARAM = "clientId";
    private static final String clientNamePARAM = "clientName";

    private static final String officeIdPARAM = "officeId";
    private static final String officeNamePARAM = "officeName";

    private static final String depositProductIdPARAM = "depositProductId";
    private static final String depositProductNamePARAM = "depositProductName";
    private static final String availableBalancePARAM = "availableBalance";
    private static final String minRequiredBalancePARAM = "minRequiredBalance";
    private static final String ledgerBalancePARAM = "ledgerBalance";

    private static final String createdOnPARAM = "createdOn";
    private static final String activatedOnPARAM = "activatedOn";
    private static final String lastTransactionOnPARAM = "lastTransactionOn";

    private static final String externalIdPARAM = "externalId";

    public static final String savingsProductExtensionParam = "savingsProductExtension";

    public static final Set<String> DEPOSIT_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(IDPARAM, accountNoPARAM,
            depositTypePARAM, statusPARAM, clientIdPARAM, clientNamePARAM, officeIdPARAM, officeNamePARAM, depositProductIdPARAM,
            depositProductNamePARAM, availableBalancePARAM, ledgerBalancePARAM, createdOnPARAM, activatedOnPARAM, lastTransactionOnPARAM,
            externalIdPARAM, minRequiredBalancePARAM, ClientBusinessApiConstants.bvnParamName, ClientApiConstants.clientTypeParamName));
}
