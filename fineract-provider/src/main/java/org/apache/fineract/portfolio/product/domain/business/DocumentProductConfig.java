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
package org.apache.fineract.portfolio.product.domain.business;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.client.domain.business.ClientDocumentConfig;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.springframework.stereotype.Component;

@Entity
@Component
@Table(name = "m_document_config_product", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "loan_product_id" }, name = "loan_product_UNIQUE_document_config"),
        @UniqueConstraint(columnNames = { "savings_product_id" }, name = "savings_product_UNIQUE_document_config") })
public class DocumentProductConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "m_document_client_config_id", nullable = false)
    private ClientDocumentConfig clientDocumentConfig;

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = true)
    private LoanProduct loanProduct;

    @OneToOne
    @JoinColumn(name = "savings_product_id", nullable = true)
    private SavingsProduct savingsProduct;

    protected DocumentProductConfig() {}

    public static DocumentProductConfig instanceLoanProduct(final ClientDocumentConfig clientDocumentConfig,
            final LoanProduct loanProduct) {
        final SavingsProduct savingsProduct = null;
        return new DocumentProductConfig(clientDocumentConfig, loanProduct, savingsProduct);
    }

    public static DocumentProductConfig instanceSavingsProduct(final ClientDocumentConfig clientDocumentConfig,
            final SavingsProduct savingsProduct) {
        final LoanProduct loanProduct = null;
        return new DocumentProductConfig(clientDocumentConfig, loanProduct, savingsProduct);
    }

    public DocumentProductConfig(ClientDocumentConfig clientDocumentConfig, LoanProduct loanProduct, SavingsProduct savingsProduct) {
        this.clientDocumentConfig = clientDocumentConfig;
        this.loanProduct = loanProduct;
        this.savingsProduct = savingsProduct;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public SavingsProduct getSavingsProduct() {
        return savingsProduct;
    }

}
