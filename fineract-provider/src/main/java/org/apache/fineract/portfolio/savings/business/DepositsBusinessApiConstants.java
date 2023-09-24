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

public final class DepositsBusinessApiConstants {

    private DepositsBusinessApiConstants() {

    }

    // Deposit products
    public static final String RESOURCE_NAME = "DEPSOITS";

    protected static final String IDPARAM = "id";
    protected static final String accountNoPARAM = "accountNo";
    protected static final String depositTypePARAM = "depositType";
    protected static final String statusPARAM = "status";

    protected static final String clientIdPARAM = "clientId";
    protected static final String clientNamePARAM = "clientName";

    protected static final String officeIdPARAM = "officeId";
    protected static final String officeNamePARAM = "officeName";

    protected static final String depositProductIdPARAM = "depositProductId";
    protected static final String depositProductNamePARAM = "depositProductName";
    protected static final String availableBalancePARAM = "availableBalance";
    protected static final String ledgerBalancePARAM = "ledgerBalance";

    protected static final String createdOnPARAM = "createdOn";
    protected static final String activatedOnPARAM = "activatedOn";
    protected static final String lastTransactionOnPARAM = "lastTransactionOn";

    protected static final String externalIdPARAM = "externalId";

    public static final Set<String> DEPOSIT_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(IDPARAM, accountNoPARAM, depositTypePARAM, statusPARAM, clientIdPARAM, clientNamePARAM, officeIdPARAM, officeNamePARAM,
                    depositProductIdPARAM, depositProductNamePARAM, availableBalancePARAM, ledgerBalancePARAM, createdOnPARAM, activatedOnPARAM, lastTransactionOnPARAM, externalIdPARAM));
}
