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
package org.apache.fineract.portfolio.paymenttype.domain.business;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.paymenttype.api.business.PaymentTypeGridApiResourceConstants;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;

@Entity
@Table(name = "m_payment_type_grid")
public class PaymentTypeGrid extends AbstractAuditableWithUTCDateTimeCustom {

    @OneToOne
    @JoinColumn(name = "payment_type_id", nullable = false)
    private PaymentType paymentType;

    @Column(name = "name", nullable = true)
    private String name;

    @Column(name = "grid_json", nullable = true)
    private String gridJson;

    @Column(name = "is_grid")
    private Boolean isGrid;

    @Column(name = "is_commission")
    private Boolean isCommission;

    @Column(name = "calculation_type", nullable = true)
    private Integer paymentCalculationType;

    @Column(name = "amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amount;

    @Column(name = "percent", scale = 6, precision = 19, nullable = true)
    private BigDecimal percent;

    @JoinColumn(name = "charge_id")
    private Charge charge;

    public PaymentTypeGrid() {}

    private PaymentTypeGrid(PaymentType paymentType, String name, String gridJson, Boolean isGrid, Boolean isCommission,
            Integer paymentCalculationType, BigDecimal amount, BigDecimal percent, Charge chargeId) {
        this.paymentType = paymentType;
        this.name = name;
        this.gridJson = gridJson;
        this.isGrid = isGrid;
        this.isCommission = isCommission;
        this.paymentCalculationType = paymentCalculationType;
        this.amount = amount;
        this.percent = percent;
        this.charge = chargeId;
    }

    public static PaymentTypeGrid instance(PaymentType paymentType, String name, String gridJson, Boolean isGrid, Boolean isCommission,
            Integer paymentCalculationType, BigDecimal amount, BigDecimal percent, Charge charge) {

        return new PaymentTypeGrid(paymentType, name, gridJson, isGrid, isCommission, paymentCalculationType, amount, percent, charge);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInStringParameterNamed(PaymentTypeGridApiResourceConstants.NAME, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(PaymentTypeGridApiResourceConstants.NAME);
            actualChanges.put(PaymentTypeGridApiResourceConstants.NAME, newValue);
            this.name = newValue;
        }

        Long paymentTypeId = this.paymentType.getId();
        if (command.isChangeInLongParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE, paymentTypeId)) {
            final Long newValue = command.longValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE);
            actualChanges.put(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE, newValue);
        }

        if (command.isChangeInStringParameterNamed(PaymentTypeGridApiResourceConstants.GRID_JSON, this.gridJson)) {
            final String newValue = command.stringValueOfParameterNamed(PaymentTypeGridApiResourceConstants.GRID_JSON);
            actualChanges.put(PaymentTypeGridApiResourceConstants.GRID_JSON, newValue);
            this.gridJson = newValue;
        }

        if (command.isChangeInBooleanParameterNamed(PaymentTypeGridApiResourceConstants.ISGRID, this.isGrid)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(PaymentTypeGridApiResourceConstants.ISGRID);
            actualChanges.put(PaymentTypeGridApiResourceConstants.ISGRID, newValue);
            this.isGrid = newValue;
        }
        if (command.isChangeInBooleanParameterNamed(PaymentTypeGridApiResourceConstants.ISCOMMISION, this.isCommission)) {
            final Boolean newValue = command.booleanObjectValueOfParameterNamed(PaymentTypeGridApiResourceConstants.ISCOMMISION);
            actualChanges.put(PaymentTypeGridApiResourceConstants.ISCOMMISION, newValue);
            this.isCommission = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE,
                this.paymentCalculationType)) {
            final Integer newValue = command.integerValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE);
            actualChanges.put(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE, newValue);
            this.paymentCalculationType = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(PaymentTypeGridApiResourceConstants.AMOUNT, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(PaymentTypeGridApiResourceConstants.AMOUNT);
            actualChanges.put(PaymentTypeGridApiResourceConstants.AMOUNT, newValue);
            this.amount = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamed(PaymentTypeGridApiResourceConstants.PERCENT, this.percent)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PERCENT);
            actualChanges.put(PaymentTypeGridApiResourceConstants.PERCENT, newValue);
            this.percent = newValue;
        }

        Long chargeId = this.charge.getId();
        if (command.isChangeInLongParameterNamed(PaymentTypeGridApiResourceConstants.CHARGE_DATA, chargeId)) {
            final Long newValue = command.longValueOfParameterNamed(PaymentTypeGridApiResourceConstants.CHARGE_DATA);
            actualChanges.put(PaymentTypeGridApiResourceConstants.CHARGE_DATA, newValue);
        }

        return actualChanges;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

}
