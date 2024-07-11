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
package org.apache.fineract.portfolio.paymenttype.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PaymentTypeGridApiResourceConstants {

    private PaymentTypeGridApiResourceConstants() {

    }

    public static final String RESOURCE_NAME = "PAYMENTTYPEGRID";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String GRID_JSON = "gridJsonObject";
    public static final String ISGRID = "isGrid";
    public static final String ISCOMMISION = "isCommission";
    public static final String PAYMENT_TYPE = "paymentTypeId";
    public static final String CHARGE_DATA = "chargeId";
    public static final String PAYMENTCALCULATIONTYPE = "paymentCalculationType";
    public static final String AMOUNT = "amount";
    public static final String PERCENT = "percent";
    public static final String LOCALE = "locale";

    static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(ID, NAME, GRID_JSON, ISGRID, PAYMENT_TYPE, PAYMENTCALCULATIONTYPE, AMOUNT, PERCENT, ISCOMMISION, CHARGE_DATA));
    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(NAME, GRID_JSON, ISGRID, PAYMENT_TYPE,
            PAYMENTCALCULATIONTYPE, AMOUNT, PERCENT, ISCOMMISION, CHARGE_DATA, LOCALE));
}
