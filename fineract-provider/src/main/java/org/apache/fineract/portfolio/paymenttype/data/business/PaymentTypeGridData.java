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
package org.apache.fineract.portfolio.paymenttype.data.business;

import com.google.gson.JsonElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import lombok.Data;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

@Data
public class PaymentTypeGridData implements Serializable {

    private Long id;
    private PaymentTypeData paymentType;
    private String name;
    private String gridJson;
    private JsonElement gridJsonObject;
    private Boolean isGrid;
    private Boolean isCommission;
    private EnumOptionData paymentCalculationType;
    private BigDecimal amount;
    private BigDecimal percent;
    private EnumOptionData chargeData;

//    template 
    private final Collection<ChargeData> chargeOptions;
    private final Collection<PaymentTypeData> paymentTypeOptions;

    public PaymentTypeGridData(final Long id, PaymentTypeData paymentType,
            String name, String gridJson, Boolean isGrid, Boolean isCommission, EnumOptionData paymentCalculationType,
            BigDecimal amount, BigDecimal percent, EnumOptionData chargeData, Collection<ChargeData> chargeOptions,
            Collection<PaymentTypeData> paymentTypeOptions) {
        this.id = id;
        this.paymentType = paymentType;
        this.name = name;
        this.gridJson = gridJson;
        this.isGrid = isGrid;
        this.isCommission = isCommission;
        this.paymentCalculationType = paymentCalculationType;
        this.amount = amount;
        this.percent = percent;
        this.chargeData = chargeData;
        this.chargeOptions = chargeOptions;
        this.paymentTypeOptions = paymentTypeOptions;
    }

    public static PaymentTypeGridData instance(final Long id, final PaymentTypeData paymentType,
            final String name,
            final String gridJson,
            final Boolean isGrid,
            final Boolean isCommission,
            final EnumOptionData paymentCalculationType,
            final BigDecimal amount,
            final BigDecimal percent, EnumOptionData chargeData) {
        final Collection<ChargeData> chargeOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;

        return new PaymentTypeGridData(id, paymentType, name, gridJson, isGrid, isCommission, paymentCalculationType,
                amount, percent, chargeData, chargeOptions, paymentTypeOptions);

    }

    public static PaymentTypeGridData template(final Collection<ChargeData> chargeOptions, final Collection<PaymentTypeData> paymentTypeOptions) {
        final Long id = null;
        final PaymentTypeData paymentType = null;
        final String name = null;
        final String gridJson = null;
        final Boolean isGrid = null;
        final Boolean isCommission = null;
        final EnumOptionData paymentCalculationType = null;
        final BigDecimal amount = null;
        final BigDecimal percent = null;
        EnumOptionData chargeData = null;

        return new PaymentTypeGridData(id, paymentType, name, gridJson, isGrid, isCommission, paymentCalculationType,
                amount, percent, chargeData, chargeOptions, paymentTypeOptions);
    }
}
