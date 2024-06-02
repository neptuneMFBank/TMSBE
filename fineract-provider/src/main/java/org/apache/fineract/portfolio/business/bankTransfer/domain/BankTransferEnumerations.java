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

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

public final class BankTransferEnumerations {

    private BankTransferEnumerations() {

    }

    public static EnumOptionData status(final Integer statusId) {
        return status(BankTransferType.fromInt(statusId));
    }

    public static EnumOptionData status(final BankTransferType status) {
        EnumOptionData optionData = new EnumOptionData(BankTransferType.INVALID.getValue().longValue(), BankTransferType.INVALID.getCode(),
                "Invalid");
        switch (status) {
            case INVALID ->
                optionData = new EnumOptionData(BankTransferType.INVALID.getValue().longValue(), BankTransferType.INVALID.getCode(), "INVALID");
            case INTRABANK ->
                optionData = new EnumOptionData(BankTransferType.INTRABANK.getValue().longValue(), BankTransferType.INTRABANK.getCode(), "INTRABANK");
            case INTERBANK_EBILLS ->
                optionData = new EnumOptionData(BankTransferType.INTERBANK_EBILLS.getValue().longValue(), BankTransferType.INTERBANK_EBILLS.getCode(), "INTERBANK_EBILLS");
        }

        return optionData;
    }

    public static List<EnumOptionData> bankAccountTransferTypes(final BankTransferType[] accountTransferTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final BankTransferType accountTransferType : accountTransferTypes) {
            if (accountTransferType.getValue() == 0) {
                continue;
            }
            optionDatas.add(bankAccountTransferType(accountTransferType));
        }
        return optionDatas;
    }

    public static EnumOptionData bankAccountTransferType(final BankTransferType accountType) {
        final EnumOptionData optionData = new EnumOptionData(Long.valueOf(accountType.getValue()), accountType.getCode(),
                accountType.toString());
        return optionData;
    }
}
