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
package org.apache.fineract.portfolio.loanproduct.domain.business;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.springframework.stereotype.Component;

@Entity
@Component
@Table(name = "m_product_loan_payment_type_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "name_UNIQUE_product_loan_payment_type_config")})
public class LoanProductPaymentTypeConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_product_loan_payment_type_config_code", joinColumns = @JoinColumn(name = "m_product_loan_payment_type_config_id"), inverseJoinColumns = @JoinColumn(name = "payment_type_id"))
    private Set<PaymentType> paymentTypes = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private LoanProduct loanProduct;

    protected LoanProductPaymentTypeConfig() {
    }

    public static LoanProductPaymentTypeConfig instance(final String name, final LoanProduct loanProduct, final String description,
            final boolean active) {
        return new LoanProductPaymentTypeConfig(name, loanProduct, description, active);
    }

    public LoanProductPaymentTypeConfig(final String name, final LoanProduct loanProduct, final String description, final boolean active) {
        this.loanProduct = loanProduct;
        this.description = description;
        this.active = active;
        this.name = name;
    }

    public boolean updatePaymentType(final PaymentType paymentType, final boolean isSelected) {
        boolean changed;
        if (isSelected) {
            changed = addPaymentType(paymentType);
        } else {
            changed = removePaymentType(paymentType);
        }
        return changed;
    }

    private boolean addPaymentType(final PaymentType paymentType) {
        return this.paymentTypes.add(paymentType);
    }

    private boolean removePaymentType(final PaymentType paymentType) {
        return this.paymentTypes.remove(paymentType);
    }

    public Set<PaymentType> getPaymentTypes() {
        return this.paymentTypes;
    }

    public void setPaymentTypes(Set<PaymentType> paymentTypes) {
        this.paymentTypes = paymentTypes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

    public void setLoanProduct(LoanProduct loanProduct) {
        this.loanProduct = loanProduct;
    }

}
