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

import java.util.Collection;
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

/**
 * Immutable data object representing a user document being managed on the
 * platform.
 */
public class DocumentProductConfigData {

    private final Long id;
    private final LoanProductData loanProduct;
    private final SavingsProductData savingsProduct;
    private final DocumentConfigData configData;
    private Collection<LoanProductData> loanProductDatas;
    private Collection<DocumentConfigData> documentConfigDatas;
    private Collection<SavingsProductData> savingProductOptions;

    public static DocumentProductConfigData template(final Collection<LoanProductData> loanProductDatas,
            final Collection<DocumentConfigData> documentConfigDatas, final Collection<SavingsProductData> savingProductOptions) {
        final Long id = null;
        final LoanProductData loanProduct = null;
        final SavingsProductData savingsProduct = null;
        final DocumentConfigData configData = null;

        return new DocumentProductConfigData(id, loanProduct, savingsProduct, configData, loanProductDatas, documentConfigDatas, savingProductOptions);
    }

    public DocumentProductConfigData(Long id, LoanProductData loanProduct, SavingsProductData savingsProduct, DocumentConfigData configData, Collection<LoanProductData> loanProductDatas, Collection<DocumentConfigData> documentConfigDatas, Collection<SavingsProductData> savingProductOptions) {
        this.id = id;
        this.loanProduct = loanProduct;
        this.savingsProduct = savingsProduct;
        this.configData = configData;
        this.loanProductDatas = loanProductDatas;
        this.documentConfigDatas = documentConfigDatas;
        this.savingProductOptions = savingProductOptions;
    }

}
