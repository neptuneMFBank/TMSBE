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
package org.apache.fineract.portfolio.business.overdraft.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;

@Entity
@Table(name = "m_overdraft")
public class Overdraft extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    protected BigDecimal amount;

    @Column(name = "nominal_annual_interest_rate_overdraft", scale = 6, precision = 19, nullable = false)
    protected BigDecimal nominalAnnualInterestRateOverdraft;

    @Column(name = "status_enum", nullable = false)
    private Integer status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "savings_id")
    private SavingsAccount savingsAccount;

    protected Overdraft() {
    }

    public Overdraft(BigDecimal amount, BigDecimal nominalAnnualInterestRateOverdraft, Integer status, LocalDate startDate, LocalDate expiryDate, SavingsAccount savingsAccount) {
        this.amount = amount;
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.status = status;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.savingsAccount = savingsAccount;
    }

    public static Overdraft createOverdraft(BigDecimal amount, BigDecimal nominalAnnualInterestRateOverdraft, Integer status, LocalDate startDate, LocalDate expiryDate, SavingsAccount savingsAccount) {
        return new Overdraft(amount, nominalAnnualInterestRateOverdraft, status, startDate, expiryDate, savingsAccount);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getNominalAnnualInterestRateOverdraft() {
        return nominalAnnualInterestRateOverdraft;
    }

    public void setNominalAnnualInterestRateOverdraft(BigDecimal nominalAnnualInterestRateOverdraft) {
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public SavingsAccount getSavingsAccount() {
        return savingsAccount;
    }

    public void setSavingsAccount(SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
    }

}
