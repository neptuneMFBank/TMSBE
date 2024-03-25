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
package org.apache.fineract.portfolio.loanproduct.business.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_loanproduct_visibility_config_mapping")
public class LoanProductVisibilityMapping extends AbstractPersistableCustom {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "config_id", nullable = false)
    private LoanProductVisibilityConfig loanProductVisibilityConfig;

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    @JoinColumn(name = "loanproduct_id", nullable = false)
    private LoanProduct loanProduct;

    public LoanProductVisibilityMapping() {
    }

    public LoanProductVisibilityMapping(LoanProductVisibilityConfig loanProductVisibilityConfig, LoanProduct loanProduct) {
        this.loanProductVisibilityConfig = loanProductVisibilityConfig;
        this.loanProduct = loanProduct;
    }

    public LoanProduct getLoanProduct() {
        return loanProduct;
    }

}
