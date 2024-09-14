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
package org.apache.fineract.portfolio.business.metrics.data;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 *
 * People typically use either of the following settings when calculating there interest using the daily method:
 * <ul>
 * <li>Actual or</li>
 * <li>360 or</li>
 * <li>364 or</li>
 * <li>365</li>
 * </ul>
 */
public enum LoanApprovalStatus {

    INVALID(0, "invalid"), //
    DRAFT(10, "draft"), //
    QUEUE(50, "queue"), //
    PENDING(100, "pending"), //
    UNDO(150, "undo"), //
    APPROVED(200, "approved"), //
    ACTIVE(300, "active"), //
    REJECTED(500, "rejected"), //
    REASSIGNED(800, "reassigned"), CLOSED(600, "closed");

    private final Integer value;
    private final String code;

    LoanApprovalStatus(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static Object[] integerValues() {
        final List<Integer> values = new ArrayList<>();
        for (final LoanApprovalStatus enumType : values()) {
            if (enumType.getValue() > 0) {
                values.add(enumType.getValue());
            }
        }

        return values.toArray();
    }

    public static LoanApprovalStatus fromInt(final Integer type) {
        LoanApprovalStatus repaymentFrequencyType = LoanApprovalStatus.INVALID;
        if (type != null) {
            switch (type) {
                case 10 -> repaymentFrequencyType = LoanApprovalStatus.DRAFT;
                case 50 -> repaymentFrequencyType = LoanApprovalStatus.QUEUE;
                case 100 -> repaymentFrequencyType = LoanApprovalStatus.PENDING;
                case 150 -> repaymentFrequencyType = LoanApprovalStatus.UNDO;
                case 300 -> repaymentFrequencyType = LoanApprovalStatus.ACTIVE;
                case 200 -> repaymentFrequencyType = LoanApprovalStatus.APPROVED;
                case 500 -> repaymentFrequencyType = LoanApprovalStatus.REJECTED;
                case 800 -> repaymentFrequencyType = LoanApprovalStatus.REASSIGNED;
                case 600 -> repaymentFrequencyType = LoanApprovalStatus.CLOSED;
            }
        }
        return repaymentFrequencyType;
    }

    public boolean isDraft() {
        return LoanApprovalStatus.DRAFT.getValue().equals(this.value);
    }

    public boolean isQueue() {
        return LoanApprovalStatus.QUEUE.getValue().equals(this.value);
    }

    public boolean isPending() {
        return LoanApprovalStatus.PENDING.getValue().equals(this.value);
    }

    public boolean isUndo() {
        return LoanApprovalStatus.UNDO.getValue().equals(this.value);
    }

    public boolean isApproved() {
        return LoanApprovalStatus.APPROVED.getValue().equals(this.value);
    }

    public boolean isActive() {
        return LoanApprovalStatus.ACTIVE.getValue().equals(this.value);
    }

    public boolean isRejected() {
        return LoanApprovalStatus.REJECTED.getValue().equals(this.value);
    }

    public boolean isReAssigned() {
        return LoanApprovalStatus.REASSIGNED.getValue().equals(this.value);
    }

    public boolean isClosed() {
        return LoanApprovalStatus.CLOSED.getValue().equals(this.value);
    }

    public static EnumOptionData status(final Integer statusId) {
        return status(LoanApprovalStatus.fromInt(statusId));
    }

    public static EnumOptionData status(final LoanApprovalStatus status) {
        EnumOptionData optionData = new EnumOptionData(LoanApprovalStatus.INVALID.getValue().longValue(),
                LoanApprovalStatus.INVALID.getCode(), "Invalid");
        switch (status) {
            case INVALID -> optionData = new EnumOptionData(LoanApprovalStatus.INVALID.getValue().longValue(),
                    LoanApprovalStatus.INVALID.getCode(), "Invalid");
            case PENDING -> optionData = new EnumOptionData(LoanApprovalStatus.PENDING.getValue().longValue(),
                    LoanApprovalStatus.PENDING.getCode(), "Pending");
            case DRAFT -> optionData = new EnumOptionData(LoanApprovalStatus.DRAFT.getValue().longValue(),
                    LoanApprovalStatus.DRAFT.getCode(), "Draft");
            case QUEUE -> optionData = new EnumOptionData(LoanApprovalStatus.QUEUE.getValue().longValue(),
                    LoanApprovalStatus.QUEUE.getCode(), "Queue");
            case UNDO ->
                optionData = new EnumOptionData(LoanApprovalStatus.UNDO.getValue().longValue(), LoanApprovalStatus.UNDO.getCode(), "Undo");
            case REJECTED -> optionData = new EnumOptionData(LoanApprovalStatus.REJECTED.getValue().longValue(),
                    LoanApprovalStatus.REJECTED.getCode(), "Rejected");
            case APPROVED -> optionData = new EnumOptionData(LoanApprovalStatus.APPROVED.getValue().longValue(),
                    LoanApprovalStatus.APPROVED.getCode(), "Approved");
            case ACTIVE -> optionData = new EnumOptionData(LoanApprovalStatus.ACTIVE.getValue().longValue(),
                    LoanApprovalStatus.ACTIVE.getCode(), "Active");
            case REASSIGNED -> optionData = new EnumOptionData(LoanApprovalStatus.REASSIGNED.getValue().longValue(),
                    LoanApprovalStatus.REASSIGNED.getCode(), "ReAssigned");
            case CLOSED -> optionData = new EnumOptionData(LoanApprovalStatus.CLOSED.getValue().longValue(),
                    LoanApprovalStatus.CLOSED.getCode(), "Closed");
        }

        return optionData;
    }

}
