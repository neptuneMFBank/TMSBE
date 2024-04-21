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
package org.apache.fineract.portfolio.products.domain.business;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductVisibilityMapping;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductVisibilityMapping;

@Entity
@Table(name = "m_product_visibility_config")
public class ProductVisibilityConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "product_type", nullable = false)
    private Integer productType;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<SavingsProductVisibilityMapping> savingsProductVisibilityMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductVisibilityMapping> loanProductVisibilityMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductVisibilityClientclassificationMapping> productVisibilityClientclassificationMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductVisibilityClienttypeMapping> productVisibilityClienttypeMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<ProductVisibilityLegalenumMapping> productVisibilityLegalenumMapping = new HashSet<>();

    private ProductVisibilityConfig(final String name, final String description, final Collection<LoanProduct> loanProducts,
            final Collection<CodeValue> clientclassifications, final Collection<CodeValue> clientTypes,
            final Collection<Integer> legalEnums, final Integer productType, final Collection<SavingsProduct> savingsProducts) {
        this.name = name;
        this.description = description;
        this.productType = productType;
        this.savingsProductVisibilityMapping = associateSavingsProductsWithThisConfig(savingsProducts);
        this.loanProductVisibilityMapping = associateProductsWithThisConfig(loanProducts);
        this.productVisibilityClientclassificationMapping = associateClientclassificationsWithThisConfig(clientclassifications);
        this.productVisibilityClienttypeMapping = associateClienttypesWithThisConfig(clientTypes);
        this.productVisibilityLegalenumMapping = associateLegalenumsWithThisConfig(legalEnums);

    }

    public ProductVisibilityConfig() {}

    public static ProductVisibilityConfig createConfig(final String name, final String description,
            final Collection<LoanProduct> loanProducts, final Collection<CodeValue> clientclassifications,
            final Collection<CodeValue> clientTypes, final Collection<Integer> legalEnums, final Integer productType,
            final Collection<SavingsProduct> savingsProducts) {
        return new ProductVisibilityConfig(name, description, loanProducts, clientclassifications, clientTypes, legalEnums, productType,
                savingsProducts);
    }

    private Set<LoanProductVisibilityMapping> associateProductsWithThisConfig(final Collection<LoanProduct> loanProducts) {
        Set<LoanProductVisibilityMapping> newLoanProductVisibilityMapping = null;
        if (loanProducts != null && !loanProducts.isEmpty()) {
            newLoanProductVisibilityMapping = new HashSet<>();
            for (LoanProduct loanProduct : loanProducts) {
                newLoanProductVisibilityMapping.add(new LoanProductVisibilityMapping(this, loanProduct));
            }
        }
        return newLoanProductVisibilityMapping;
    }

    private Set<ProductVisibilityClientclassificationMapping> associateClientclassificationsWithThisConfig(
            final Collection<CodeValue> clientclassifications) {
        Set<ProductVisibilityClientclassificationMapping> newLoanproductVisibilityClientclassificationMapping = null;
        if (clientclassifications != null && !clientclassifications.isEmpty()) {
            newLoanproductVisibilityClientclassificationMapping = new HashSet<>();
            for (CodeValue clientclassification : clientclassifications) {
                newLoanproductVisibilityClientclassificationMapping
                        .add(new ProductVisibilityClientclassificationMapping(this, clientclassification));
            }
        }
        return newLoanproductVisibilityClientclassificationMapping;
    }

    private Set<ProductVisibilityClienttypeMapping> associateClienttypesWithThisConfig(final Collection<CodeValue> clientTypes) {
        Set<ProductVisibilityClienttypeMapping> newLoanproductVisibilityClienttypeMapping = null;
        if (clientTypes != null && !clientTypes.isEmpty()) {
            newLoanproductVisibilityClienttypeMapping = new HashSet<>();
            for (CodeValue clientType : clientTypes) {
                newLoanproductVisibilityClienttypeMapping.add(new ProductVisibilityClienttypeMapping(this, clientType));
            }
        }
        return newLoanproductVisibilityClienttypeMapping;
    }

    private Set<ProductVisibilityLegalenumMapping> associateLegalenumsWithThisConfig(final Collection<Integer> legalEnums) {
        Set<ProductVisibilityLegalenumMapping> newLoanproductVisibilityLegalenumMapping = null;
        if (legalEnums != null && !legalEnums.isEmpty()) {
            newLoanproductVisibilityLegalenumMapping = new HashSet<>();
            for (Integer legalEnum : legalEnums) {
                newLoanproductVisibilityLegalenumMapping.add(new ProductVisibilityLegalenumMapping(this, legalEnum));
            }
        }
        return newLoanproductVisibilityLegalenumMapping;
    }

    private Set<SavingsProductVisibilityMapping> associateSavingsProductsWithThisConfig(final Collection<SavingsProduct> savingsProducts) {
        Set<SavingsProductVisibilityMapping> newSavingsProductVisibilityMapping = null;
        if (savingsProducts != null && !savingsProducts.isEmpty()) {
            newSavingsProductVisibilityMapping = new HashSet<>();
            for (SavingsProduct savingsProduct : savingsProducts) {
                newSavingsProductVisibilityMapping.add(new SavingsProductVisibilityMapping(this, savingsProduct));
            }
        }
        return newSavingsProductVisibilityMapping;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLoanProductVisibilityMapping(Collection<LoanProduct> loanProducts) {
        this.loanProductVisibilityMapping = associateProductsWithThisConfig(loanProducts);
    }

    public void setProductVisibilityClientclassificationMapping(Collection<CodeValue> clientclassifications) {
        this.productVisibilityClientclassificationMapping = associateClientclassificationsWithThisConfig(clientclassifications);

    }

    public void setProductVisibilityClienttypeMapping(Collection<CodeValue> clientTypes) {
        this.productVisibilityClienttypeMapping = associateClienttypesWithThisConfig(clientTypes);
    }

    public void setProductVisibilityLegalenumMapping(Collection<Integer> legalEnums) {
        this.productVisibilityLegalenumMapping = associateLegalenumsWithThisConfig(legalEnums);
    }

    public Set<ProductVisibilityClientclassificationMapping> getProductVisibilityClientclassificationMapping() {
        return productVisibilityClientclassificationMapping;
    }

    public Set<LoanProductVisibilityMapping> getLoanProductVisibilityMapping() {
        return loanProductVisibilityMapping;
    }

    public Set<ProductVisibilityClienttypeMapping> getProductVisibilityClienttypeMapping() {
        return productVisibilityClienttypeMapping;
    }

    public Set<ProductVisibilityLegalenumMapping> getProductVisibilityLegalenumMapping() {
        return productVisibilityLegalenumMapping;
    }

    public void setSavingsProductVisibilityMapping(Collection<SavingsProduct> savingsProducts) {
        this.savingsProductVisibilityMapping = associateSavingsProductsWithThisConfig(savingsProducts);
    }

    public Set<SavingsProductVisibilityMapping> getSavingsProductVisibilityMapping() {
        return savingsProductVisibilityMapping;
    }

}
