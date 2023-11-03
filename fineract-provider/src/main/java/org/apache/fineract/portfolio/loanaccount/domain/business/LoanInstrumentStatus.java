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
package org.apache.fineract.portfolio.loanaccount.domain.business;

/**
 * Enum representation of loan status states.
 */
public enum LoanInstrumentStatus {

    INVALID(0, "loanStatusType.invalid"), //
    PENDING(100, "loanInstrumentStatus.pending"), //
    APPROVED(200, "loanInstrumentStatus.approved"), //
    ACTIVE(300, "loanInstrumentStatus.active"), //
    REJECTED(500, "loanInstrumentStatus.rejected");

    private final Integer value;
    private final String code;

    public static LoanInstrumentStatus fromInt(final Integer statusValue) {

        LoanInstrumentStatus enumeration = LoanInstrumentStatus.INVALID;
        switch (statusValue) {
            case 100 -> enumeration = LoanInstrumentStatus.PENDING;
            case 200 -> enumeration = LoanInstrumentStatus.APPROVED;
            case 300 -> enumeration = LoanInstrumentStatus.ACTIVE;
            case 500 -> enumeration = LoanInstrumentStatus.REJECTED;
        }
        return enumeration;
    }

    LoanInstrumentStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public boolean hasStateOf(final LoanInstrumentStatus state) {
        return this.value.equals(state.getValue());
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public boolean isSubmittedAndPendingApproval() {
        return this.value.equals(LoanInstrumentStatus.PENDING.getValue());
    }

    public boolean isApproved() {
        return this.value.equals(LoanInstrumentStatus.APPROVED.getValue());
    }

    public boolean isActive() {
        return this.value.equals(LoanInstrumentStatus.ACTIVE.getValue());
    }

    public boolean isRejected() {
        return this.value.equals(LoanInstrumentStatus.REJECTED.getValue());
    }

    public boolean isActiveOrAwaitingApprovalOrDisbursal() {
        return isApproved() || isSubmittedAndPendingApproval() || isActive();
    }

}
