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
package org.apache.fineract.portfolio.products.domain.business;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_product_visibility_legalenum_mapping")
public class ProductVisibilityLegalenumMapping extends AbstractPersistableCustom {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "config_id", nullable = false)
    private ProductVisibilityConfig productVisibilityConfig;

    @Column(name = "legalenum_id", nullable = false)
    private Integer legalEnum;

    public ProductVisibilityLegalenumMapping() {}

    public ProductVisibilityLegalenumMapping(ProductVisibilityConfig productVisibilityConfig, Integer legalEnum) {
        this.productVisibilityConfig = productVisibilityConfig;
        this.legalEnum = legalEnum;
    }

    public Integer getLegalEnum() {
        return legalEnum;
    }

}
