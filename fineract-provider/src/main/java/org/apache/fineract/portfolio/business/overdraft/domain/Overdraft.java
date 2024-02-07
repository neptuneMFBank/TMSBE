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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.overdraft.api.OverdraftApiResourceConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.simplifytech.data.GeneralConstants;

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

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String amountParamName = OverdraftApiResourceConstants.AMOUNT;
        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            actualChanges.put(amountParamName, newValue);
            this.amount = newValue;
        }
        final String daysParamName = OverdraftApiResourceConstants.NUMBER_OF_DAYS;
        Integer numberOfDays = GeneralConstants.numberOfDays(startDate, expiryDate);
        if (command.parameterExists(OverdraftApiResourceConstants.NUMBER_OF_DAYS)) {
            final Integer checkNumberOfDays = command.integerValueOfParameterNamed(OverdraftApiResourceConstants.NUMBER_OF_DAYS);
            numberOfDays = checkNumberOfDays != null && checkNumberOfDays > 0 ? checkNumberOfDays : numberOfDays;
        }
        if (command.isChangeInBigDecimalParameterNamed(daysParamName, this.nominalAnnualInterestRateOverdraft)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(daysParamName);
            actualChanges.put(daysParamName, newValue);
            this.nominalAnnualInterestRateOverdraft = newValue;
        }
        final String interestParamName = OverdraftApiResourceConstants.NOMINALINTEREST;
        if (command.isChangeInBigDecimalParameterNamed(interestParamName, this.nominalAnnualInterestRateOverdraft)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(interestParamName);
            actualChanges.put(interestParamName, newValue);
            this.nominalAnnualInterestRateOverdraft = newValue;
        }

        final String startDateParamName = OverdraftApiResourceConstants.STARTDATE;
        if (command.isChangeInDateParameterNamed(startDateParamName, this.startDate)) {
            final LocalDate newValue = command.dateValueOfParameterNamed(startDateParamName);
            actualChanges.put(startDateParamName, newValue);
            this.startDate = newValue;
            this.expiryDate = this.startDate.plusDays(numberOfDays);
        }

        return actualChanges;
    }

    public static Overdraft createOverdraft(BigDecimal amount, BigDecimal nominalAnnualInterestRateOverdraft, LocalDate startDate, LocalDate expiryDate, SavingsAccount savingsAccount) {
        final Integer status = LoanApprovalStatus.DRAFT.getValue();
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
