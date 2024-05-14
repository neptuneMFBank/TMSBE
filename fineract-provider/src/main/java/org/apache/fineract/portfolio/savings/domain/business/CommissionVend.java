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
package org.apache.fineract.portfolio.savings.domain.business;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_commission_vend")
public class CommissionVend extends AbstractPersistableCustom {

    @Column(name = "accounting_rules")
    private Long accountingRules;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "ref_no")
    private String refNo;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "bank_number")
    private String bankNumber;

    @Column(name = "note")
    private String note;

    @Column(name = "grid_percent")
    private BigDecimal gridPercent;

    @Column(name = "grid_amount")
    private BigDecimal gridAmount;

    @Column(name = "calculation_type")
    private Integer calculationType;

    @Column(name = "payment_type_id")
    private Long paymentTypeId;

    @Column(name = "currency_code")
    private String currencyCode;

    protected CommissionVend() {
    }

    public Long getAccountingRules() {
        return accountingRules;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getRefNo() {
        return refNo;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getBankNumber() {
        return bankNumber;
    }

    public String getNote() {
        return note;
    }

    public BigDecimal getGridPercent() {
        return gridPercent;
    }

    public BigDecimal getGridAmount() {
        return gridAmount;
    }

    public Integer getCalculationType() {
        return calculationType;
    }

    public Long getPaymentTypeId() {
        return paymentTypeId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

}
