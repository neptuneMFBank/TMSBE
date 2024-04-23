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
import lombok.Data;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;

@Data
public class PaymentTypeGridData implements Serializable {

    private Long id;
    private PaymentTypeData paymentType;
    private String name;
    private String gridJson;
    private JsonElement gridJsonObject;
    private Boolean isGrid;
    private EnumOptionData paymentCalculationType;
    private BigDecimal amount;
    private BigDecimal percent;

    public static PaymentTypeGridData instance(final Long id, final PaymentTypeData paymentType,
            final String name,
            final String gridJson,
            final Boolean isGrid,
            final EnumOptionData paymentCalculationType,
            final BigDecimal amount,
            final BigDecimal percent) {
        return new PaymentTypeGridData(id, paymentType, name, gridJson, isGrid, paymentCalculationType, amount, percent);

    }

    public PaymentTypeGridData(final Long id, PaymentTypeData paymentType, String name, String gridJson, Boolean isGrid, EnumOptionData paymentCalculationType, BigDecimal amount, BigDecimal percent) {
        this.id = id;
        this.paymentType = paymentType;
        this.name = name;
        this.gridJson = gridJson;
        this.isGrid = isGrid;
        this.paymentCalculationType = paymentCalculationType;
        this.amount = amount;
        this.percent = percent;
    }

}
