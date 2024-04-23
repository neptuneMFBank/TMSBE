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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
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

}
