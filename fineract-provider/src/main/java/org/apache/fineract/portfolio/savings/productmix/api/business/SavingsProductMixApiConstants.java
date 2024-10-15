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
package org.apache.fineract.portfolio.savings.productmix.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class SavingsProductMixApiConstants {

    SavingsProductMixApiConstants() {

    }

    public static final String productIdParamName = "productId";
    public static final String productNameParamName = "productName";
    public static final String restrictedProductsParamName = "restrictedProducts";
    public static final String allowedProductsParamName = "allowedProducts";
    public static final String productOptionsParamName = "productOptions";

    static final Set<String> PRODUCTMIX_DATA_PARAMETERS = new HashSet<>(Arrays.asList(productIdParamName, productNameParamName,
            restrictedProductsParamName, allowedProductsParamName, productOptionsParamName));

    static final Set<String> PRODUCTMIX_LIST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(productIdParamName, productNameParamName));

}
