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
package org.apache.fineract.portfolio.business.kyc.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@Table(name = "m_kyc_config")
public class KycConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kyc_tier_cv_id")
    private CodeValue KycTier;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "kycConfig", orphanRemoval = true)
    @JoinColumn(name = "kyc_config_mapping")
    private Set<KycConfigMapping> kycConfigMapping = new HashSet<>();

    protected KycConfig() {}

    private KycConfig(final String description, final Collection<CodeValue> kycParams, CodeValue KycTier) {
        this.KycTier = KycTier;
        this.description = description;
        this.kycConfigMapping = associateKycParamsWithThisConfig(kycParams);

    }

    public static KycConfig createConfig(final String description, final Collection<CodeValue> kycParams, CodeValue KycTier) {
        return new KycConfig(description, kycParams, KycTier);
    }

    private Set<KycConfigMapping> associateKycParamsWithThisConfig(final Collection<CodeValue> kycParams) {
        Set<KycConfigMapping> newKycConfigMapping = null;
        if (kycParams != null && !kycParams.isEmpty()) {
            newKycConfigMapping = new HashSet<>();
            for (CodeValue kycParam : kycParams) {
                newKycConfigMapping.add(new KycConfigMapping(this, kycParam));
            }
        }
        return newKycConfigMapping;
    }

    public void setKycConfigMapping(Collection<CodeValue> kycParams) {
        this.kycConfigMapping = associateKycParamsWithThisConfig(kycParams);
    }
}
