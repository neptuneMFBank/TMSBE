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
package org.apache.fineract.portfolio.paymenttype.domain.business;

public enum PaymentCalculationType {

    INVALID(0, "paymentCommissionType.invalid"), //
    FLAT(1, "paymentCommissionType.flat"), //
    PERCENT(2, "paymentCommissionType.percent"), //
    CAPPED(3, "paymentCommissionType.capped"), //
    ;

    private final Integer value;
    private final String code;

    PaymentCalculationType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] validValues() {
        return new Integer[] { PaymentCalculationType.FLAT.getValue(), PaymentCalculationType.PERCENT.getValue(),
                PaymentCalculationType.CAPPED.getValue() };
    }

    public static PaymentCalculationType fromInt(final Integer chargeCalculation) {
        PaymentCalculationType chargeCalculationType = PaymentCalculationType.INVALID;
        chargeCalculationType = switch (chargeCalculation) {
            case 1 -> FLAT;
            case 2 -> PERCENT;
            case 3 -> CAPPED;
            default -> INVALID;
        };
        return chargeCalculationType;
    }

    public boolean isPercentage() {
        return this.value.equals(PaymentCalculationType.PERCENT.getValue());
    }

    public boolean isCapped() {
        return this.value.equals(PaymentCalculationType.CAPPED.getValue());
    }

    public boolean isFlat() {
        return this.value.equals(PaymentCalculationType.FLAT.getValue());
    }

}
