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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.Role;

@Entity
@Table(name = "m_role_loan_product_approval_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"rlpa_id", "rank"}, name = "rlpa_UNIQUE_rank")})
public class LoanProductApprovalConfig extends AbstractPersistableCustom {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rlpa_id", nullable = false)
    private LoanProductApproval loanProductApproval;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "max_approval_amount", scale = 6, precision = 19)
    private BigDecimal maxApprovalAmount;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    protected LoanProductApprovalConfig() {
    }

    public static LoanProductApprovalConfig create(Role role, BigDecimal maxApprovalAmount, Integer rank) {
        final LoanProductApproval loanProductApproval = null;
        return new LoanProductApprovalConfig(loanProductApproval, role, maxApprovalAmount, rank);
    }

    public LoanProductApprovalConfig(LoanProductApproval loanProductApproval, Role role, BigDecimal maxApprovalAmount, Integer rank) {
        this.loanProductApproval = loanProductApproval;
        this.role = role;
        this.maxApprovalAmount = maxApprovalAmount;
        this.rank = rank;
    }

    public LoanProductApproval getLoanProductApproval() {
        return loanProductApproval;
    }

    public Role getRole() {
        return role;
    }

    public BigDecimal getMaxApprovalAmount() {
        return maxApprovalAmount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setMaxApprovalAmount(BigDecimal maxApprovalAmount) {
        this.maxApprovalAmount = maxApprovalAmount;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public void setLoanProductApproval(LoanProductApproval loanProductApproval) {
        this.loanProductApproval = loanProductApproval;
    }

}
