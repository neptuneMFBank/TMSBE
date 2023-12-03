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

import java.math.BigDecimal;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.Role;

@Entity
@Table(name = "m_product_loan_interest_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"rlpa_id", "rank"}, name = "rlpa_UNIQUE_rank")})
public class LoanProductInterestConfig extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rlpi_id", nullable = false)
    private LoanProductInterest loanProductInterest;

    @Column(name = "min_tenor", scale = 6, precision = 19)
    private BigDecimal minTenor;

    @Column(name = "max_tenor", scale = 6, precision = 19)
    private BigDecimal maxTenor;

    @Column(name = "nominal_interest_rate_per_period", scale = 6, precision = 19, nullable = true)
    private BigDecimal nominalInterestRatePerPeriod;

    protected LoanProductInterestConfig() {
    }

    public static LoanProductInterestConfig create(Role role, BigDecimal minTenor, BigDecimal maxTenor, BigDecimal nominalInterestRatePerPeriod) {
        final LoanProductInterest loanProductInterest = null;
        return new LoanProductInterestConfig(loanProductInterest, minTenor, maxTenor, nominalInterestRatePerPeriod);
    }

    public LoanProductInterestConfig(LoanProductInterest loanProductInterest, BigDecimal minTenor, BigDecimal maxTenor, BigDecimal nominalInterestRatePerPeriod) {
        this.loanProductInterest = loanProductInterest;
        this.minTenor = minTenor;
        this.maxTenor = maxTenor;
        this.nominalInterestRatePerPeriod = nominalInterestRatePerPeriod;
    }

    public BigDecimal getMinTenor() {
        return minTenor;
    }

    public void setMinTenor(BigDecimal minTenor) {
        this.minTenor = minTenor;
    }

    public BigDecimal getMaxTenor() {
        return maxTenor;
    }

    public void setMaxTenor(BigDecimal maxTenor) {
        this.maxTenor = maxTenor;
    }

    public BigDecimal getNominalInterestRatePerPeriod() {
        return nominalInterestRatePerPeriod;
    }

    public void setNominalInterestRatePerPeriod(BigDecimal nominalInterestRatePerPeriod) {
        this.nominalInterestRatePerPeriod = nominalInterestRatePerPeriod;
    }

    public LoanProductInterest getLoanProductInterest() {
        return loanProductInterest;
    }

    public BigDecimal getMaxApprovalAmount() {
        return maxTenor;
    }

    public void setMaxApprovalAmount(BigDecimal maxTenor) {
        this.maxTenor = maxTenor;
    }

    public void setLoanProductInterest(LoanProductInterest loanProductInterest) {
        this.loanProductInterest = loanProductInterest;
    }

    public BigDecimal getMinApprovalAmount() {
        return minTenor;
    }

    public void setMinApprovalAmount(BigDecimal minTenor) {
        this.minTenor = minTenor;
    }

    // Check if no other range exists within this range
    public boolean isNoOtherRangeWithin(Set<LoanProductInterestConfig> ranges) {
        for (LoanProductInterestConfig otherRange : ranges) {
            if (otherRange != this && isWithin(otherRange)) {
                return false;
            }
        }
        return true;
    }

    // Check if another range is within this range
    private boolean isWithin(LoanProductInterestConfig otherRange) {
        return this.minTenor.compareTo(otherRange.minTenor) <= 0 && this.maxTenor.compareTo(otherRange.maxTenor) >= 0;
    }
}
