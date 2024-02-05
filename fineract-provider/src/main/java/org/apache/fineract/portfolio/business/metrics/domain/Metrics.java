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
package org.apache.fineract.portfolio.business.metrics.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.business.overdraft.domain.Overdraft;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

@Entity
@Table(name = "m_metrics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"`loan_id", "rank`"}, name = "metrics_UNIQUE_rank_loan"),
    @UniqueConstraint(columnNames = {"`savings_id", "rank`"}, name = "metrics_UNIQUE_rank_saving"),
    @UniqueConstraint(columnNames = {"`overdraft_id", "rank`"}, name = "metrics_UNIQUE_rank_overdraft"),})
public class Metrics extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "assigned_user_id", nullable = false)
    private Staff assignedUser;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "rank", nullable = false)
    private Integer rank;

    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "savings_id")
    private SavingsAccount savingsAccount;

    @ManyToOne
    @JoinColumn(name = "overdraft_id")
    private Overdraft overdraft;

    protected Metrics() {
    }

    public Metrics(Staff assignedUser, Integer status, Integer rank, Loan loan, SavingsAccount savingsAccount, Overdraft overdraft) {
        this.assignedUser = assignedUser;
        this.status = status;
        this.rank = rank;
        this.loan = loan;
        this.savingsAccount = savingsAccount;
        this.overdraft = overdraft;
    }

    public static Metrics createLoanMetrics(Staff assignedUser, Integer status, Integer rank, Loan loan) {
        final SavingsAccount savingsAccount = null;
        final Overdraft overdraft = null;
        return new Metrics(assignedUser, status, rank, loan, savingsAccount, overdraft);
    }

    public static Metrics createSavingsMetrics(Staff assignedUser, Integer status, Integer rank, SavingsAccount savingsAccount) {
        final Loan loan = null;
        final Overdraft overdraft = null;
        return new Metrics(assignedUser, status, rank, loan, savingsAccount, overdraft);
    }

    public static Metrics createOverdraftMetrics(Staff assignedUser, Integer status, Integer rank, Overdraft overdraft, final SavingsAccount savingsAccount) {
        final Loan loan = null;
        return new Metrics(assignedUser, status, rank, loan, savingsAccount, overdraft);
    }

    public Staff getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(Staff assignedUser) {
        this.assignedUser = assignedUser;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }

    public SavingsAccount getSavingsAccount() {
        return savingsAccount;
    }

    public void setSavingsAccount(SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
    }

    public Overdraft getOverdraft() {
        return overdraft;
    }

    public void setOverdraft(Overdraft overdraft) {
        this.overdraft = overdraft;
    }

}
