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
package org.apache.fineract.portfolio.business.merchant.security.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.api.AuthenticationApiResource;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistrationRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.apache.fineract.useradministration.domain.business.AppUserExtension;
import org.apache.fineract.useradministration.domain.business.AppUserExtensionRepositoryWrapper;
import org.apache.fineract.useradministration.domain.business.AppUserMerchantMapping;
import org.apache.fineract.useradministration.domain.business.AppUserMerchantMappingRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class MerchantAuthServiceImpl implements MerchantAuthService {

    private final AuthenticationApiResource authenticationApiResource;
    private final FromJsonHelper fromJsonHelper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final AppUserMerchantMappingRepositoryWrapper appUserMerchantMappingRepositoryWrapper;
    private final AppUserRepositoryWrapper appUserRepositoryWrapper;
    private final SelfServiceRegistrationRepository selfServiceRegistrationRepository;
    private final AppUserExtensionRepositoryWrapper appUserExtensionRepositoryWrapper;

    @Autowired
    public MerchantAuthServiceImpl(final AuthenticationApiResource authenticationApiResource, final FromJsonHelper fromJsonHelper,
            final ClientRepositoryWrapper clientRepositoryWrapper,
            final AppUserMerchantMappingRepositoryWrapper appUserMerchantMappingRepositoryWrapper,
            final SelfServiceRegistrationRepository selfServiceRegistrationRepository,
            final AppUserRepositoryWrapper appUserRepositoryWrapper,
            final AppUserExtensionRepositoryWrapper appUserExtensionRepositoryWrapper) {
        this.authenticationApiResource = authenticationApiResource;
        this.fromJsonHelper = fromJsonHelper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.appUserMerchantMappingRepositoryWrapper = appUserMerchantMappingRepositoryWrapper;
        this.appUserRepositoryWrapper = appUserRepositoryWrapper;
        this.selfServiceRegistrationRepository = selfServiceRegistrationRepository;
        this.appUserExtensionRepositoryWrapper = appUserExtensionRepositoryWrapper;
    }

    @Transactional
    @Override
    public String authenticate(String apiRequestBodyAsJson) {
        // check if user exists
        JsonElement element = this.fromJsonHelper.fromJson(apiRequestBodyAsJson, JsonElement.class);
        final String username = this.fromJsonHelper.extractStringNamed(SelfServiceApiConstants.usernameParamName, element);
        final String password = this.fromJsonHelper.extractStringNamed(SelfServiceApiConstants.passwordParamName, element);
        Client client = clientRepositoryWrapper.findByEmailAddress(username);
        if (ObjectUtils.isEmpty(client)) {
            throw new NoAuthorizationException("Your profile does not exist, kindly register as a new user.");
        }

        // check if user is Merchant
        List<AppUserMerchantMapping> appUserIdFromMerchantMappings = appUserMerchantMappingRepositoryWrapper.findByClientId(client.getId());
        if (CollectionUtils.isEmpty(appUserIdFromMerchantMappings)) {
            throw new NoAuthorizationException("Your account has been disabled, contact customer service support.");
        }

        AppUserMerchantMapping appUserMerchantMapping = appUserIdFromMerchantMappings.get(0);
        Long appUserId = appUserMerchantMapping.getAppUser().getId();
        AppUser appUser = this.appUserRepositoryWrapper.findOneWithNotFoundDetection(appUserId);

        AppUserExtension appUserExtension = this.appUserExtensionRepositoryWrapper.findByAppuserId(appUserMerchantMapping.getAppUser());
        // check if user is not merchant
        if (!appUserExtension.isMerchant()) {
            throw new NoAuthorizationException("Your account has been locked, contact customer service support.");
        }

        final String emailAddress = client.emailAddress();
        if (StringUtils.isNotBlank(emailAddress)) {
            final boolean isUsernameStillExistsInRegistrationLog = this.selfServiceRegistrationRepository.existsByUsername(emailAddress);
            if (isUsernameStillExistsInRegistrationLog) {

                this.selfServiceRegistrationRepository.deleteByUsername(emailAddress);
            }
        }

        JsonObject requestObject = new JsonObject();
        requestObject.addProperty(SelfServiceApiConstants.usernameParamName, appUser.getUsername());
        requestObject.addProperty(SelfServiceApiConstants.passwordParamName, password);

        return this.authenticationApiResource.authenticate(requestObject.toString(), true);
    }
}
