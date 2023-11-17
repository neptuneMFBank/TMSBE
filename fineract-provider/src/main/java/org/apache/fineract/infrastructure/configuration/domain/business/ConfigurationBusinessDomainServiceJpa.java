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
package org.apache.fineract.infrastructure.configuration.domain.business;

import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainServiceJpa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationBusinessDomainServiceJpa implements ConfigurationBusinessDomainService {

    public static final String LIMIT_LOAN_AGE = "limit-loan-age";
    private final ConfigurationDomainServiceJpa configurationDomainServiceJpa;

    @Autowired
    public ConfigurationBusinessDomainServiceJpa(final ConfigurationDomainServiceJpa configurationDomainServiceJpa) {
        this.configurationDomainServiceJpa = configurationDomainServiceJpa;
    }

    @Override
    public Integer isLimitLoanAge() {
        final GlobalConfigurationPropertyData property = this.configurationDomainServiceJpa.getGlobalConfigurationPropertyData(LIMIT_LOAN_AGE);
        final boolean isLimitLoanAgeEnabled = property.isEnabled();
        final Long value = property.getValue();
        if (isLimitLoanAgeEnabled && value != null) {
            return value.intValue();
        }
        return null;
    }
}
