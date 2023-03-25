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
package org.apache.fineract.portfolio.self.security.service;

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
import org.apache.fineract.useradministration.domain.AppUserClientMapping;
import org.apache.fineract.useradministration.domain.AppUserClientMappingRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Gasper Thompson
 */
@Service
public class SelfAuthServiceImpl implements SelfAuthService {

    private final AuthenticationApiResource authenticationApiResource;
    private final FromJsonHelper fromJsonHelper;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final AppUserClientMappingRepositoryWrapper appUserClientMappingRepositoryWrapper;
    private final AppUserRepositoryWrapper appUserRepositoryWrapper;
    private final SelfServiceRegistrationRepository selfServiceRegistrationRepository;

    @Autowired
    public SelfAuthServiceImpl(final AuthenticationApiResource authenticationApiResource, final FromJsonHelper fromJsonHelper,
            final ClientRepositoryWrapper clientRepositoryWrapper,
            final AppUserClientMappingRepositoryWrapper appUserClientMappingRepositoryWrapper,
            final SelfServiceRegistrationRepository selfServiceRegistrationRepository, final AppUserRepositoryWrapper appUserRepositoryWrapper) {
        this.authenticationApiResource = authenticationApiResource;
        this.fromJsonHelper = fromJsonHelper;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.appUserClientMappingRepositoryWrapper = appUserClientMappingRepositoryWrapper;
        this.appUserRepositoryWrapper = appUserRepositoryWrapper;
        this.selfServiceRegistrationRepository = selfServiceRegistrationRepository;
    }

    @Transactional
    @Override
    public String authenticate(String apiRequestBodyAsJson) {
        // check if user exists
        JsonElement element = this.fromJsonHelper.fromJson(apiRequestBodyAsJson, JsonElement.class);
        final String username = this.fromJsonHelper.extractStringNamed(SelfServiceApiConstants.usernameParamName, element);
        final String password = this.fromJsonHelper.extractStringNamed(SelfServiceApiConstants.passwordParamName, element);
        Client client = clientRepositoryWrapper.findByMobileNoOrEmailAddress(username);
        if (ObjectUtils.isEmpty(client)) {
            throw new NoAuthorizationException("Your profile does not exist, kindly register as a new user.");
        }

        // check if user is self signed on
        List<AppUserClientMapping> appUserIdFromClientMappings = appUserClientMappingRepositoryWrapper.findByClientId(client.getId());
        if (CollectionUtils.isEmpty(appUserIdFromClientMappings)) {
            throw new NoAuthorizationException("Your account has been disabled, contact customer service support.");
        }

        AppUserClientMapping appUserClientMapping = appUserIdFromClientMappings.get(0);
        Long appUserId = appUserClientMapping.getAppUser().getId();
        AppUser appUser = this.appUserRepositoryWrapper.findOneWithNotFoundDetection(appUserId);

        // check if user is not self client
        if (!appUser.isSelfServiceUser()) {
            throw new NoAuthorizationException("Your account has been locked, contact customer service support.");
        }

        final String emailAddress = client.emailAddress();
        if (StringUtils.isNotBlank(emailAddress)) {
            final boolean isUsernameStillExistsInRegistrationLog = this.selfServiceRegistrationRepository.existsByUsername(emailAddress);
            if (isUsernameStillExistsInRegistrationLog) {
                //delete audit registration logs for uniqueness to have dropOff of users who are yet to complete their onboarding
                this.selfServiceRegistrationRepository.deleteByUsername(emailAddress);
            }
        }

        JsonObject requestObject = new JsonObject();
        requestObject.addProperty(SelfServiceApiConstants.usernameParamName, appUser.getUsername());
        requestObject.addProperty(SelfServiceApiConstants.passwordParamName, password);

        return this.authenticationApiResource.authenticate(requestObject.toString(), true);
    }

}
