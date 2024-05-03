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
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.springframework.util.CollectionUtils;

@Entity
@Table(name = "m_role_loan_product_approval", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "rlpa_UNIQUE_name"),
    @UniqueConstraint(columnNames = {"loan_product_id"}, name = "rlpa_UNIQUE_loan_product")})
public class LoanProductApproval extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "name", nullable = false)
    private String name;

    @OneToOne
    @JoinColumn(name = "savings_product_id", nullable = false)
    private SavingsProduct savingsProduct;

    @OneToOne
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "LoanProductApproval", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();

    protected LoanProductApproval() {
    }

    private LoanProductApproval(String name, LoanProduct loanProduct, Set<LoanProductApprovalConfig> loanProductApprovalConfig, SavingsProduct savingsProduct) {
        this.name = name;
        this.loanProduct = loanProduct;
        this.savingsProduct = savingsProduct;
        this.loanProductApprovalConfig = loanProductApprovalConfig;
    }

    public static LoanProductApproval create(String name, LoanProduct loanProduct, SavingsProduct savingsProduct) {
        Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();
        return new LoanProductApproval(name, loanProduct, loanProductApprovalConfig, savingsProduct);
    }

    public static LoanProductApproval create(String name, LoanProduct loanProduct,
            Set<LoanProductApprovalConfig> loanProductApprovalConfig, SavingsProduct savingsProduct) {
        return new LoanProductApproval(name, loanProduct, loanProductApprovalConfig, savingsProduct);
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

    public SavingsProduct getSavingsProduct() {
        return savingsProduct;
    }

    public void setSavingsProduct(SavingsProduct savingsProduct) {
        this.savingsProduct = savingsProduct;
    }

    public void setLoanProductApprovalConfig(Set<LoanProductApprovalConfig> loanProductApprovalConfig) {
        this.loanProductApprovalConfig = loanProductApprovalConfig;
    }

    public void addLoanProductApprovalConfig(LoanProductApprovalConfig singleLoanProductApprovalConfig) {
        singleLoanProductApprovalConfig.setLoanProductApproval(this);
        if (!CollectionUtils.isEmpty(this.loanProductApprovalConfig)) {
            final Integer rank = singleLoanProductApprovalConfig.getRank();
            final boolean exist = this.loanProductApprovalConfig.stream()
                    .anyMatch(action -> !Objects.equals(singleLoanProductApprovalConfig.getId(), action.getId())
                    && Objects.equals(action.getRank(), rank));
            if (exist) {
                throw new PlatformDataIntegrityException("error.msg.product.approval.config.duplicate",
                        "Loan Product Approval config with index `" + rank + "` already exists");
            }
        }

        this.loanProductApprovalConfig.add(singleLoanProductApprovalConfig);
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
        return updated;
    }
}
