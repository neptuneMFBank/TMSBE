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
package org.apache.fineract.portfolio.self.savings.api.business;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SelfFixedDepositAccountsBusinessApiResourceSwagger {

    private SelfFixedDepositAccountsBusinessApiResourceSwagger() {}

    @Schema(description = "GetFixedDepositAccountsFixedDepositAccountIdTransactionsTemplateResponse")
    public static final class GetFixedDepositAccountsFixedDepositAccountIdTransactionsTemplateResponse {

        private GetFixedDepositAccountsFixedDepositAccountIdTransactionsTemplateResponse() {}

        static final class GetSelfFixedTransactionType {

            private GetSelfFixedTransactionType() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "savingsAccountTransactionType.deposit")
            public String code;
            @Schema(example = "Deposit")
            public String description;
            @Schema(example = "true")
            public Boolean deposit;
            @Schema(example = "false")
            public Boolean withdrawal;
            @Schema(example = "false")
            public Boolean interestPosting;
            @Schema(example = "false")
            public Boolean feeDeduction;
            @Schema(example = "false")
            public Boolean initiateTransfer;
            @Schema(example = "false")
            public Boolean approveTransfer;
            @Schema(example = "false")
            public Boolean withdrawTransfer;
            @Schema(example = "false")
            public Boolean rejectTransfer;
            @Schema(example = "false")
            public Boolean overdraftInterest;
            @Schema(example = "false")
            public Boolean writtenoff;
            @Schema(example = "true")
            public Boolean overdraftFee;
        }

        static final class GetSelfFixedCurrency {

            private GetSelfFixedCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "4")
            public Integer decimalPlaces;
            @Schema(example = "100")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        @Schema(example = "1")
        public Integer id;
        public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetFixedDepositAccountsFixedDepositAccountIdTransactionsTemplateResponse.GetSelfFixedTransactionType transactionType;
        @Schema(example = "1")
        public Integer accountId;
        @Schema(example = "000000001")
        public String accountNo;
        @Schema(example = "[2014, 6, 25]")
        public LocalDate date;
        public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetFixedDepositAccountsFixedDepositAccountIdTransactionsTemplateResponse.GetSelfFixedCurrency currency;
        @Schema(example = "100000.000000")
        public BigDecimal amount;
        @Schema(example = "false")
        public Boolean reversed;
        @Schema(example = "[]")
        public List<Integer> paymentTypeOptions;
    }

    @Schema(description = "GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse")
    public static final class GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse {

        private GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse() {}

        static final class GetSelfFixedTransactionsCurrency {

            private GetSelfFixedTransactionsCurrency() {}

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "0")
            public Integer inMultiplesOf;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetSelfFixedTransactionsTransactionType {

            private GetSelfFixedTransactionsTransactionType() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "savingsAccountTransactionType.withdrawal")
            public String code;
            @Schema(example = "Withdrawal")
            public String description;
            @Schema(example = "false")
            public Boolean deposit;
            @Schema(example = "true")
            public Boolean withdrawal;
            @Schema(example = "false")
            public Boolean interestPosting;
            @Schema(example = "false")
            public Boolean feeDeduction;
        }

        static final class GetSelfFixedPaymentDetailData {

            private GetSelfFixedPaymentDetailData() {}

            static final class GetSelfFixedPaymentType {

                private GetSelfFixedPaymentType() {}

                @Schema(example = "11")
                public Integer id;
                @Schema(example = "cash")
                public String name;
            }

            @Schema(example = "62")
            public Integer id;
            public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse.GetSelfFixedPaymentDetailData.GetSelfFixedPaymentType paymentType;
            @Schema(example = "")
            public Integer accountNumber;
            @Schema(example = "")
            public Integer checkNumber;
            @Schema(example = "")
            public Integer routingCode;
            @Schema(example = "")
            public Integer receiptNumber;
            @Schema(example = "")
            public Integer bankNumber;
        }

        @Schema(example = "1")
        public Integer id;
        public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse.GetSelfFixedTransactionsTransactionType transactionType;
        @Schema(example = "1")
        public Integer accountId;
        @Schema(example = "000000001")
        public String accountNo;
        @Schema(example = "[2013, 8, 7]")
        public LocalDate date;
        public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse.GetSelfFixedTransactionsCurrency currency;
        public SelfFixedDepositAccountsBusinessApiResourceSwagger.GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse.GetSelfFixedPaymentDetailData paymentDetailData;
        @Schema(example = "5000")
        public Float amount;
        @Schema(example = "0")
        public Integer runningBalance;
        @Schema(example = "true")
        public Boolean reversed;
    }
}
