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
package org.apache.fineract.portfolio.business.bankTransfer.domain;

/**
 * Enum representation of loan status states.
 */
public enum BankTransferType {

    INVALID(0, "INVALID"), //
    INTRABANK(1, "INTRABANK"), //
    INTERBANK_EBILLS(2, "INTERBANK.EBILLS");

    private final Integer value;
    private final String code;

    public static BankTransferType fromInt(final Integer statusValue) {

        BankTransferType enumeration = BankTransferType.INVALID;
        switch (statusValue) {
            case 1 -> enumeration = BankTransferType.INTRABANK;
            case 2 -> enumeration = BankTransferType.INTERBANK_EBILLS;
        }
        return enumeration;
    }

    BankTransferType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final BankTransferType state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isIntraBank() {
        return this.value.equals(BankTransferType.INTRABANK.getValue());
    }

    public boolean isInterBankEbills() {
        return this.value.equals(BankTransferType.INTERBANK_EBILLS.getValue());
    }
}
