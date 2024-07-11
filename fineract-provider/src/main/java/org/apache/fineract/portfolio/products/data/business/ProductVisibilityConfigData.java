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
package org.apache.fineract.portfolio.products.data.business;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

@SuppressWarnings("unused")
@Data
public class ProductVisibilityConfigData implements Serializable {

    private final Long id;
    private final String name;
    private final String description;
    private Collection<Long> clientClassification;
    private Collection<Long> clientType;
    private Collection<Long> legalEnum;
    private Collection<Long> loanProduct;
    private Collection<Long> savingsProduct;

    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> clientClassificationOptions;
    private final List<EnumOptionData> clientLegalFormOptions;
    private final Collection<LoanProductData> loanProductOptions;
    private final Collection<SavingsProductData> savingsProductOptions;

    private ProductVisibilityConfigData(final Long id, final String name, final String description, final Collection<Long> loanProduct,
            final Collection<Long> clientClassification, final Collection<Long> clientType, final Collection<Long> legalEnum,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions,
            final List<EnumOptionData> clientLegalFormOptions, final Collection<LoanProductData> loanProductOptions,
            final Collection<SavingsProductData> savingsProductOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.clientClassification = clientClassification;
        this.clientType = clientType;
        this.legalEnum = legalEnum;
        this.loanProduct = loanProduct;
        this.clientTypeOptions = clientTypeOptions;
        this.clientClassificationOptions = clientClassificationOptions;
        this.clientLegalFormOptions = clientLegalFormOptions;
        this.loanProductOptions = loanProductOptions;
        this.savingsProductOptions = savingsProductOptions;
    }

    public static ProductVisibilityConfigData template(final Collection<LoanProductData> loanProductOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions,
            final List<EnumOptionData> clientLegalFormOptions, final Collection<SavingsProductData> savingsProductOptions) {
        final Long id = null;
        final String name = null;
        final String description = null;
        final Collection<Long> loanProduct = null;
        final Collection<Long> clientClassification = null;
        final Collection<Long> clientType = null;
        final Collection<Long> legalEnum = null;
        return new ProductVisibilityConfigData(id, name, description, loanProduct, clientClassification, clientType, legalEnum,
                clientTypeOptions, clientClassificationOptions, clientLegalFormOptions, loanProductOptions, savingsProductOptions);
    }

    public static ProductVisibilityConfigData instance(Long id, String name, String description) {
        final Collection<Long> loanProduct = null;
        final Collection<Long> clientClassification = null;
        final Collection<Long> clientType = null;
        final Collection<Long> legalEnum = null;
        final Collection<LoanProductData> loanProductOptions = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<SavingsProductData> savingsProductOptions = null;
        final List<EnumOptionData> clientLegalFormOptions = null;
        return new ProductVisibilityConfigData(id, name, description, loanProduct, clientClassification, clientType, legalEnum,
                clientTypeOptions, clientClassificationOptions, clientLegalFormOptions, loanProductOptions, savingsProductOptions);
    }

}
