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
package org.apache.fineract.portfolio.products.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"HideUtilityClassConstructor"})
public class DocumentProductConfigApiConstants {
    public static final String resourceName = "DOCUMENT";

    public static final String idParam = "id";

    public static final String loanProductIdsParam = "loanProductIds";
    public static final String savingsProductIdsParam = "savingsProductIds";
    public static final String configDataIdParam = "configDataId";

    public static final String loanProductParam = "loanProduct";
    public static final String savingsProductParam = "savingsProduct";
    public static final String configDataParam = "configData";
    public static final String loanProductDatasParam = "loanProductDatas";
    public static final String documentConfigDatasParam = "documentConfigDatas";
    public static final String savingProductOptionsParam = "savingProductOptions";

    public static final Set<String> DOCUMENT_PRODUCT_CONFIG_TEMPLATE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(loanProductDatasParam, documentConfigDatasParam, savingProductOptionsParam));

    public static final Set<String> DOCUMENT_PRODUCT_CONFIG_CREATE_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(
                    loanProductIdsParam, savingsProductIdsParam, configDataIdParam));

}
