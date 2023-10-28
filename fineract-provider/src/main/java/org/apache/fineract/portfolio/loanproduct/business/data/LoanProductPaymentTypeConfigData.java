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
package org.apache.fineract.portfolio.loanproduct.business.data;

import java.io.Serializable;
import java.util.Collection;
import lombok.Data;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

@SuppressWarnings("unused")
@Data
public class LoanProductPaymentTypeConfigData implements Serializable {

    private boolean active;

    private final Long id;
    private final String name;
    private final String description;
    private final Collection<LoanProductData> loanProductOptions;
    private final LoanProductData loanProductData;
    private final Collection<PaymentTypeData> paymentTypeOptions;

    private Collection<PaymentTypeData> paymentTypes;

    public static LoanProductPaymentTypeConfigData template(Collection<LoanProductData> loanProductOptions, Collection<PaymentTypeData> paymentTypeOptions) {
        Long id = null;
        String name = null;
        LoanProductData loanProductData = null;
        final String description = null;
        return new LoanProductPaymentTypeConfigData(id, name, description, loanProductOptions, loanProductData, paymentTypeOptions);
    }

    public static LoanProductPaymentTypeConfigData lookUp(Long id, String name, final String description, LoanProductData loanProductData) {
        Collection<LoanProductData> loanProductOptions = null;
        Collection<PaymentTypeData> paymentTypeOptions = null;
        return new LoanProductPaymentTypeConfigData(id, name, description, loanProductOptions, loanProductData, paymentTypeOptions);
    }

}
