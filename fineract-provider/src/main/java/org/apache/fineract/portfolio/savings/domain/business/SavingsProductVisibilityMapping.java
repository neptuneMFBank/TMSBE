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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityConfig;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;

@Entity
@Table(name = "m_savingsproduct_visibility_config_mapping")
public class SavingsProductVisibilityMapping extends AbstractPersistableCustom {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "config_id", nullable = false)
    private ProductVisibilityConfig productVisibilityConfig;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "savingsproduct_id", nullable = false)
    private SavingsProduct savingsProduct;

    public SavingsProductVisibilityMapping() {}

    public SavingsProductVisibilityMapping(ProductVisibilityConfig productVisibilityConfig, SavingsProduct savingsProduct) {
        this.productVisibilityConfig = productVisibilityConfig;
        this.savingsProduct = savingsProduct;
    }

    public SavingsProduct getSavingsProduct() {
        return savingsProduct;
    }

}
