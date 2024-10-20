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
package org.apache.fineract.portfolio.paymenttype.data;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridData;

public class PaymentTypeData implements Serializable {

    @SuppressWarnings("unused")
    private Long id;
    @SuppressWarnings("unused")
    private String name;
    @SuppressWarnings("unused")
    private String description;
    @SuppressWarnings("unused")
    private Boolean isCashPayment;
    @SuppressWarnings("unused")
    private Long position;
    private Collection<PaymentTypeGridData> paymentTypeGridData;

    public PaymentTypeData(final Long id, final String name, final String description, final Boolean isCashPayment, final Long position) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isCashPayment = isCashPayment;
        this.position = position;
    }

    public static PaymentTypeData instance(final Long id, final String name, final String description, final Boolean isCashPayment,
            final Long position) {
        return new PaymentTypeData(id, name, description, isCashPayment, position);
    }

    public static PaymentTypeData instance(final Long id, final String name) {
        String description = null;
        Boolean isCashPayment = null;
        Long position = null;
        return new PaymentTypeData(id, name, description, isCashPayment, position);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof PaymentTypeData)) {
            return false;
        }

        PaymentTypeData that = (PaymentTypeData) o;

        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(description, that.description)
                && Objects.equals(isCashPayment, that.isCashPayment) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, isCashPayment, position);
    }

    public void setPaymentTypeGridData(Collection<PaymentTypeGridData> paymentTypeGridData) {
        this.paymentTypeGridData = paymentTypeGridData;
    }

}
