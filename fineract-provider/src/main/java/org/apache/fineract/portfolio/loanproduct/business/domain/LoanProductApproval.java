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

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_role_loan_product_approval", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "rlpa_UNIQUE_name"),
    @UniqueConstraint(columnNames = {"loan_product_id"}, name = "rlpa_UNIQUE_loan_product")})
public class LoanProductApproval extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "LoanProductApproval", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();

    protected LoanProductApproval() {
    }

    private LoanProductApproval(String name, LoanProduct loanProduct, Set<LoanProductApprovalConfig> loanProductApprovalConfig) {
        this.name = name;
        this.loanProduct = loanProduct;
        this.loanProductApprovalConfig = loanProductApprovalConfig;
    }

    public static LoanProductApproval create(String name, LoanProduct loanProduct) {
        Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();;
        return new LoanProductApproval(name, loanProduct, loanProductApprovalConfig);
    }

    public static LoanProductApproval create(String name, LoanProduct loanProduct,
            Set<LoanProductApprovalConfig> loanProductApprovalConfig) {
        return new LoanProductApproval(name, loanProduct, loanProductApprovalConfig);
    }

    public String getName() {
        return name;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public Set<LoanProductApprovalConfig> getLoanProductApprovalConfig() {
        return loanProductApprovalConfig;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void setLoanProductApprovalConfig(Set<LoanProductApprovalConfig> loanProductApprovalConfig) {
        this.loanProductApprovalConfig = loanProductApprovalConfig;
    }

    public void addLoanProductApprovalConfig(LoanProductApprovalConfig loanProductApprovalConfig) {
        loanProductApprovalConfig.setLoanProductApproval(this);
        this.loanProductApprovalConfig.add(loanProductApprovalConfig);
    }

    public boolean update(Set<LoanProductApprovalConfig> loanProductApprovalConfigNew) {
        if (loanProductApprovalConfigNew == null) {
            return false;
        }

        boolean updated = false;
        if (this.loanProductApprovalConfig != null) {
            final Set<LoanProductApprovalConfig> currentSetOfLoanProductApprovalConfig = new HashSet<>(this.loanProductApprovalConfig);
            final Set<LoanProductApprovalConfig> newSetOfLoanProductApprovalConfig = new HashSet<>(loanProductApprovalConfigNew);

            if (!currentSetOfLoanProductApprovalConfig.equals(newSetOfLoanProductApprovalConfig)) {
                updated = true;
                this.loanProductApprovalConfig = loanProductApprovalConfigNew;
            }
        } else {
            updated = true;
            this.loanProductApprovalConfig = loanProductApprovalConfigNew;
        }
        for (LoanProductApprovalConfig loanProductApprovalConfig1 : loanProductApprovalConfigNew) {
            addLoanProductApprovalConfig(loanProductApprovalConfig1);
        }
        return updated;
    }
}
