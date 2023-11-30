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
package org.apache.fineract.portfolio.charge.api.business;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

/**
 * Created by Chirag Gupta on 12/01/17.
 */
final class ChargesUpfrontApiResourceSwagger {

    @Schema(description = "GetChargesUpfrontResponse")
    public static final class GetChargesUpfrontResponse {

        private GetChargesUpfrontResponse() {}

        static final class GetChargesUpfrontCurrencyResponse {

            @Schema(example = "USD")
            public String code;
            @Schema(example = "US Dollar")
            public String name;
            @Schema(example = "2")
            public Integer decimalPlaces;
            @Schema(example = "$")
            public String displaySymbol;
            @Schema(example = "currency.USD")
            public String nameCode;
            @Schema(example = "US Dollar ($)")
            public String displayLabel;
        }

        static final class GetChargesUpfrontTimeTypeResponse {

            private GetChargesUpfrontTimeTypeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeTimeType.disbursement")
            public String code;
            @Schema(example = "Disbursement")
            public String description;
        }

        static final class GetChargesUpfrontAppliesToResponse {

            private GetChargesUpfrontAppliesToResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeAppliesTo.loan")
            public String code;
            @Schema(example = "Loan")
            public String description;
        }

        static final class GetChargesUpfrontCalculationTypeResponse {

            private GetChargesUpfrontCalculationTypeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetChargesUpfrontPaymentModeResponse {

            private GetChargesUpfrontPaymentModeResponse() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargepaymentmode.accounttransfer")
            public String code;
            @Schema(example = "Account Transfer")
            public String description;
        }

        @Schema(example = "1")
        public Long id;
        @Schema(example = "Loan Service fee")
        public String name;
        @Schema(example = "true")
        public String active;
        @Schema(example = "false")
        public String penalty;
        public GetChargesUpfrontCurrencyResponse currency;
        @Schema(example = "230.56")
        public Float amount;
        public GetChargesUpfrontTimeTypeResponse chargeTimeType;
        public GetChargesUpfrontAppliesToResponse chargeAppliesTo;
        public GetChargesUpfrontCalculationTypeResponse chargeCalculationType;
        public GetChargesUpfrontPaymentModeResponse chargePaymentMode;
    }

    @Schema(description = "PostChargesUpfrontRequest")
    public static final class PostChargesUpfrontRequest {

        private PostChargesUpfrontRequest() {}

        @Schema(example = "Loan Service fee")
        public String name;
        @Schema(example = "1")
        public Integer chargeAppliesTo;
        @Schema(example = "USD")
        public String currencyCode;
        @Schema(example = "en")
        public String locale;
        @Schema(example = "230.56")
        public Float amount;
        @Schema(example = "1")
        public Integer chargeTimeType;
        @Schema(example = "1")
        public Integer chargeCalculationType;
        @Schema(example = "1")
        public Integer chargePaymentMode;
        @Schema(example = "true")
        public String active;
        @Schema(example = "dd MMMM")
        public String monthDayFormat;
        @Schema(example = "false")
        public String penalty;
    }

    @Schema(description = "PostChargesUpfrontResponse")
    public static final class PostChargesUpfrontResponse {

        private PostChargesUpfrontResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutChargesUpfrontChargeIdRequest")
    public static final class PutChargesUpfrontChargeIdRequest {

        private PutChargesUpfrontChargeIdRequest() {}

        @Schema(example = "Loan service fee(changed)")
        public String name;
    }

    @Schema(description = "PutChargesUpfrontChargeIdResponse")
    public static final class PutChargesUpfrontChargeIdResponse {

        private PutChargesUpfrontChargeIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public PutChargesUpfrontChargeIdRequest changes;
    }

    @Schema(description = "DeleteChargesUpfrontChargeIdResponse")
    public static final class DeleteChargesUpfrontChargeIdResponse {

        private DeleteChargesUpfrontChargeIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "GetChargesUpfrontTemplateResponse")
    public static final class GetChargesUpfrontTemplateResponse {

        private GetChargesUpfrontTemplateResponse() {}

        static final class GetChargesUpfrontTemplateLoanChargeCalculationTypeOptions {

            private GetChargesUpfrontTemplateLoanChargeCalculationTypeOptions() {}

            @Schema(example = "1")
            public Integer id;
            @Schema(example = "chargeCalculationType.flat")
            public String code;
            @Schema(example = "Flat")
            public String description;
        }

        static final class GetChargesUpfrontTemplateLoanChargeTimeTypeOptions {

            private GetChargesUpfrontTemplateLoanChargeTimeTypeOptions() {}

            @Schema(example = "2")
            public Integer id;
            @Schema(example = "chargeTimeType.specifiedDueDate")
            public String code;
            @Schema(example = "Specified due date")
            public String description;
        }

        static final class GetChargesUpfrontTemplateFeeFrequencyOptions {

            private GetChargesUpfrontTemplateFeeFrequencyOptions() {}

            @Schema(example = "0")
            public Integer id;
            @Schema(example = "loanTermFrequency.periodFrequencyType.days")
            public String code;
            @Schema(example = "Days")
            public String description;
        }

        @Schema(example = "false")
        public String active;
        @Schema(example = "false")
        public String penalty;
        public Set<GetChargesUpfrontResponse.GetChargesUpfrontCurrencyResponse> currencyOptions;
        public Set<GetChargesUpfrontResponse.GetChargesUpfrontCalculationTypeResponse> chargeCalculationTypeOptions;
        public Set<GetChargesUpfrontResponse.GetChargesUpfrontAppliesToResponse> chargeAppliesToOptions;
        public Set<GetChargesUpfrontResponse.GetChargesUpfrontTimeTypeResponse> chargeTimeTypeOptions;
        public Set<GetChargesUpfrontResponse.GetChargesUpfrontPaymentModeResponse> chargePaymentModeOptions;
        public Set<GetChargesUpfrontTemplateLoanChargeCalculationTypeOptions> loanChargeCalculationTypeOptions;
        public Set<GetChargesUpfrontTemplateLoanChargeTimeTypeOptions> loanChargeTimeTypeOptions;
        public Set<GetChargesUpfrontTemplateLoanChargeCalculationTypeOptions> savingsChargeCalculationTypeOptions;
        public Set<GetChargesUpfrontTemplateLoanChargeTimeTypeOptions> savingsChargeTimeTypeOptions;
        public Set<GetChargesUpfrontTemplateFeeFrequencyOptions> feeFrequencyOptions;
    }
}
