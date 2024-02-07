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
package org.apache.fineract.portfolio.paymenttype.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class PaymentTypeApiResourceConstants {

    private PaymentTypeApiResourceConstants() {

    }

    public static final String RESOURCE_NAME = "paymenttype";
    public static final String ENTITY_NAME = "PAYMENTTYPE";

    //public static final String resourceNameForPermissions = "PAYMENT_TYPE";
    public static final String resourceNameForPermissions = "PAYMENTTYPE";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String ISCASHPAYMENT = "isCashPayment";
    public static final String POSITION = "position";

    static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, NAME, DESCRIPTION, ISCASHPAYMENT));
}
