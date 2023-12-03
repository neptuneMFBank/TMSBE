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
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Entity
@Component
@Table(name = "m_product_loan_interest", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "name_UNIQUE_product_loan_interest_config"),
    @UniqueConstraint(columnNames = {"product_id"}, name = "product_id_UNIQUE_product_loan_interest_config")})
public class LoanProductInterest extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "name")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "LoanProductInterest", orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<LoanProductInterestConfig> loanProductInterestConfig = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    protected LoanProductInterest() {
    }

    private LoanProductInterest(String name, LoanProduct loanProduct, Set<LoanProductInterestConfig> loanProductInterestConfig, final boolean active) {
        this.name = name;
        this.loanProduct = loanProduct;
        this.loanProductInterestConfig = loanProductInterestConfig;
        this.active = active;
    }

    public static LoanProductInterest create(String name, LoanProduct loanProduct) {
        Set<LoanProductInterestConfig> loanProductApprovalConfig = new HashSet<>();
        final boolean active = true;
        return new LoanProductInterest(name, loanProduct, loanProductApprovalConfig, active);
    }

    public static LoanProductInterest create(String name, LoanProduct loanProduct,
            Set<LoanProductInterestConfig> loanProductApprovalConfig) {
        final boolean active = true;
        return new LoanProductInterest(name, loanProduct, loanProductApprovalConfig, active);
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public Set<LoanProductInterestConfig> getLoanProductInterestConfig() {
        return loanProductInterestConfig;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

    public void setLoanProductInterestConfig(Set<LoanProductInterestConfig> loanProductApprovalConfig) {
        this.loanProductInterestConfig = loanProductApprovalConfig;
    }

    public void addLoanProductInterestConfig(LoanProductInterestConfig singleLoanProductInterestConfig) {
        singleLoanProductInterestConfig.setLoanProductInterest(this);
        if (!CollectionUtils.isEmpty(this.loanProductInterestConfig)) {

            final boolean rangeExist = this.loanProductInterestConfig.stream()
                    .noneMatch(predicate -> predicate.isNoOtherRangeWithin(this.loanProductInterestConfig));
            if (rangeExist) {
                throw new PlatformDataIntegrityException("error.msg.loanproduct.interest.config.duplicate",
                        "Loan Product Interest config has conflicting range values");
            }
        }

        this.loanProductInterestConfig.add(singleLoanProductInterestConfig);
    }

    public boolean update(Set<LoanProductInterestConfig> loanProductApprovalConfigNew) {
        if (loanProductApprovalConfigNew == null) {
            return false;
        }

        boolean updated = false;
        if (this.loanProductInterestConfig != null) {
            final Set<LoanProductInterestConfig> currentSetOfLoanProductInterestConfig = new HashSet<>(this.loanProductInterestConfig);
            final Set<LoanProductInterestConfig> newSetOfLoanProductInterestConfig = new HashSet<>(loanProductApprovalConfigNew);

            if (!currentSetOfLoanProductInterestConfig.equals(newSetOfLoanProductInterestConfig)) {
                updated = true;
                this.loanProductInterestConfig = loanProductApprovalConfigNew;
            }
        } else {
            updated = true;
            this.loanProductInterestConfig = loanProductApprovalConfigNew;
        }
        return updated;
    }

}
