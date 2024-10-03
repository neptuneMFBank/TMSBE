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
package org.apache.fineract.portfolio.savings.productmix.data.business;

import org.apache.fineract.portfolio.savings.data.SavingsProductData;

import java.util.Collection;

public class SavingsProductMixData {

    private final Long productId;
    private final String productName;
    private final Collection<SavingsProductData> restrictedProducts;
    private final Collection<SavingsProductData> allowedProducts;
    @SuppressWarnings("unused")
    private final Collection<SavingsProductData> productOptions;

    public SavingsProductMixData(Long productId, String productName, Collection<SavingsProductData> restrictedProducts, Collection<SavingsProductData> allowedProducts, Collection<SavingsProductData> productOptions) {
        this.productId = productId;
        this.productName = productName;
        this.restrictedProducts = restrictedProducts;
        this.allowedProducts = allowedProducts;
        this.productOptions = productOptions;
    }

    public static SavingsProductMixData template(final Collection<SavingsProductData> productOptions) {
        return new SavingsProductMixData(null, null, null, null, productOptions);
    }

    public static SavingsProductMixData withTemplateOptions(final SavingsProductMixData productMixData,
                                                     final Collection<SavingsProductData> productOptions) {
        return new SavingsProductMixData(productMixData.productId, productMixData.productName, productMixData.restrictedProducts,
                productMixData.allowedProducts, productOptions);
    }

    public static SavingsProductMixData withDetails(final Long productId, final String productName,
                                             final Collection<SavingsProductData> restrictedProducts, final Collection<SavingsProductData> allowedProducts) {
        return new SavingsProductMixData(productId, productName, restrictedProducts, allowedProducts, null);
    }

    public static SavingsProductMixData withRestrictedOptions(final Collection<SavingsProductData> restrictedProducts,
                                                       final Collection<SavingsProductData> allowedProducts) {
        return new SavingsProductMixData(null, null, restrictedProducts, allowedProducts, null);
    }

    public static SavingsProductMixData withIdAndNameAlone(final Long productId, final String productName) {
        return new SavingsProductMixData(productId, productName, null, null, null);
    }
}
