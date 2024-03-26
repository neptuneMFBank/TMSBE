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
package org.apache.fineract.portfolio.loanproduct.business.domain;

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
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_loanproduct_visibility_config")
public class LoanProductVisibilityConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProductVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductVisibilityMapping> loanProductVisibilityMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProductVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanproductVisibilityClientclassificationMapping> loanproductVisibilityClientclassificationMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProductVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanproductVisibilityClienttypeMapping> loanproductVisibilityClienttypeMapping = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanProductVisibilityConfig", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanproductVisibilityLegalenumMapping> loanproductVisibilityLegalenumMapping = new HashSet<>();

    private LoanProductVisibilityConfig(final String name, final String description, final Collection<LoanProduct> loanProducts,
            final Collection<CodeValue> clientclassifications, final Collection<CodeValue> clientTypes, final Collection<Integer> legalEnums) {
        this.name = name;
        this.description = description;

        this.loanProductVisibilityMapping = associateProductsWithThisConfig(loanProducts);
        this.loanproductVisibilityClientclassificationMapping = associateClientclassificationsWithThisConfig(clientclassifications);
        this.loanproductVisibilityClienttypeMapping = associateClienttypesWithThisConfig(clientTypes);
        this.loanproductVisibilityLegalenumMapping = associateLegalenumsWithThisConfig(legalEnums);

    }

    public LoanProductVisibilityConfig() {
    }

    public static LoanProductVisibilityConfig createConfig(final String name, final String description, final Collection<LoanProduct> loanProducts,
            final Collection<CodeValue> clientclassifications, final Collection<CodeValue> clientTypes, final Collection<Integer> legalEnums) {
        return new LoanProductVisibilityConfig(name, description, loanProducts, clientclassifications, clientTypes, legalEnums);
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

    private Set<LoanproductVisibilityClientclassificationMapping> associateClientclassificationsWithThisConfig(final Collection<CodeValue> clientclassifications) {
        Set<LoanproductVisibilityClientclassificationMapping> newLoanproductVisibilityClientclassificationMapping = null;
        if (clientclassifications != null && !clientclassifications.isEmpty()) {
            newLoanproductVisibilityClientclassificationMapping = new HashSet<>();
            for (CodeValue clientclassification : clientclassifications) {
                newLoanproductVisibilityClientclassificationMapping.add(new LoanproductVisibilityClientclassificationMapping(this, clientclassification));
            }
        }
        return newLoanproductVisibilityClientclassificationMapping;
    }

    private Set<LoanproductVisibilityClienttypeMapping> associateClienttypesWithThisConfig(final Collection<CodeValue> clientTypes) {
        Set<LoanproductVisibilityClienttypeMapping> newLoanproductVisibilityClienttypeMapping = null;
        if (clientTypes != null && !clientTypes.isEmpty()) {
            newLoanproductVisibilityClienttypeMapping = new HashSet<>();
            for (CodeValue clientType : clientTypes) {
                newLoanproductVisibilityClienttypeMapping.add(new LoanproductVisibilityClienttypeMapping(this, clientType));
            }
        }
        return newLoanproductVisibilityClienttypeMapping;
    }

    private Set<LoanproductVisibilityLegalenumMapping> associateLegalenumsWithThisConfig(final Collection<Integer> legalEnums) {
        Set<LoanproductVisibilityLegalenumMapping> newLoanproductVisibilityLegalenumMapping = null;
        if (legalEnums != null && !legalEnums.isEmpty()) {
            newLoanproductVisibilityLegalenumMapping = new HashSet<>();
            for (Integer legalEnum : legalEnums) {
                newLoanproductVisibilityLegalenumMapping.add(new LoanproductVisibilityLegalenumMapping(this, legalEnum));
            }
        }
        return newLoanproductVisibilityLegalenumMapping;
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

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLoanProductVisibilityMapping(Collection<LoanProduct> loanProducts) {
        this.loanProductVisibilityMapping = associateProductsWithThisConfig(loanProducts);
    }

    public void setLoanproductVisibilityClientclassificationMapping(Collection<CodeValue> clientclassifications) {
        this.loanproductVisibilityClientclassificationMapping = associateClientclassificationsWithThisConfig(clientclassifications);

    }

    public void setLoanproductVisibilityClienttypeMapping(Collection<CodeValue> clientTypes) {
        this.loanproductVisibilityClienttypeMapping = associateClienttypesWithThisConfig(clientTypes);
    }

    public void setLoanproductVisibilityLegalenumMapping(Collection<Integer> legalEnums) {
        this.loanproductVisibilityLegalenumMapping = associateLegalenumsWithThisConfig(legalEnums);
    }

    public Set<LoanproductVisibilityClientclassificationMapping> getLoanproductVisibilityClientclassificationMapping() {
        return loanproductVisibilityClientclassificationMapping;
    }

    public Set<LoanProductVisibilityMapping> getLoanProductVisibilityMapping() {
        return loanProductVisibilityMapping;
    }

    public Set<LoanproductVisibilityClienttypeMapping> getLoanproductVisibilityClienttypeMapping() {
        return loanproductVisibilityClienttypeMapping;
    }

    public Set<LoanproductVisibilityLegalenumMapping> getLoanproductVisibilityLegalenumMapping() {
        return loanproductVisibilityLegalenumMapping;
    }

}
