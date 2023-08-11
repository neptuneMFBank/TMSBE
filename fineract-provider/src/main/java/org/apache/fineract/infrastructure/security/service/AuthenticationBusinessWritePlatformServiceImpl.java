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
package org.apache.fineract.infrastructure.security.service;

import com.google.gson.JsonElement;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignDropdownReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.GmailBackedPlatformEmailService;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.simplifytech.data.ApiResponseMessage;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationBusinessWritePlatformServiceImpl implements AuthenticationBusinessWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final UserDomainService userDomainService;
    private final GmailBackedPlatformEmailService gmailBackedPlatformEmailService;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsMessageScheduledJobService smsMessageScheduledJobService;
    private final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService;
    private static final SecureRandom secureRandom = new SecureRandom();
    final AuthenticationBusinessCommandFromApiJsonDeserializer authenticationBusinessCommandFromApiJsonDeserializer;
    private final AppUserRepositoryWrapper appUserRepositoryWrapper;

    @Autowired
    public AuthenticationBusinessWritePlatformServiceImpl(final FromJsonHelper fromApiJsonHelper, final UserDomainService userDomainService,
            final GmailBackedPlatformEmailService gmailBackedPlatformEmailService, final SmsMessageRepository smsMessageRepository,
            SmsMessageScheduledJobService smsMessageScheduledJobService,
            final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService,
            final AuthenticationBusinessCommandFromApiJsonDeserializer authenticationBusinessCommandFromApiJsonDeserializer,
            final AppUserRepositoryWrapper appUserRepositoryWrapper) {
        this.appUserRepositoryWrapper = appUserRepositoryWrapper;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.userDomainService = userDomainService;
        this.gmailBackedPlatformEmailService = gmailBackedPlatformEmailService;
        this.smsMessageRepository = smsMessageRepository;
        this.smsMessageScheduledJobService = smsMessageScheduledJobService;
        this.smsCampaignDropdownReadPlatformService = smsCampaignDropdownReadPlatformService;
        this.authenticationBusinessCommandFromApiJsonDeserializer = authenticationBusinessCommandFromApiJsonDeserializer;
    }

    @Override
    public ApiResponseMessage resetPassword(String apiRequestBodyAsJson) {

        this.authenticationBusinessCommandFromApiJsonDeserializer.validateMode(apiRequestBodyAsJson);
        JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
        String authenticationMode = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationModeParamName, element);
        String value = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.valueParamName, element);
        boolean isEmailAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.emailModeParamName);
        // boolean isMobileAuthenticationMode =
        // authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.mobileModeParamName);
        AppUser appUser;
        if (isEmailAuthenticationMode) {
            // check email
            appUser = this.appUserRepositoryWrapper.findAppUserByName(value);
        } // else if (isMobileAuthenticationMode) {
          // check mobile
          // }
        else {
            throw new PlatformDataIntegrityException("error.msg.reset.mode", "Password reset mode not supported");
        }

        String authenticationToken = randomAuthorizationTokenGeneration();
        String password = authenticationToken;
        appUser.setPassword(password);
        this.userDomainService.createCustomer(appUser, true);
        sendAuthorizationToken(appUser, password, value, null, appUser.getDisplayName(), "Reset Password");
        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage(HttpStatus.OK.value(),
                "A reset details was sent to your " + authenticationMode, null, null);
        return apiResponseMessage;
    }

    public void sendAuthorizationToken(final AppUser appUser, final String password, final String email, final String mobile,
            final String firstname, final String appendSubject) {
        if (StringUtils.isNotBlank(email)) {
            try {
                final String subject = "Hello " + firstname + "[" + appendSubject + "],";
                final String body = "Kindly use your registered email to login \n Password: " + password + "\n"
                        + "You will be required to change your password on first login.\n" + "Thanks for choosing.";

                final EmailDetail emailDetail = new EmailDetail(subject, body, email, firstname);
                this.gmailBackedPlatformEmailService.sendDefinedEmail(emailDetail);

            } catch (Exception e) {
                log.warn("email gateway not available: {}", e);
                throw new PlatformDataIntegrityException("error.msg", "Email service unavailable.");
            }
        }
        if (StringUtils.isNotBlank(mobile)) {
            try {
                log.info("sms check 1");
                Collection<SmsProviderData> smsProviders = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
                log.info("sms check 2");
                if (smsProviders.isEmpty()) {
                    log.warn("Mobile sevice provider is down or not available.");
                    // throw new PlatformDataIntegrityException("error.msg.mobile.service.provider.not.available",
                    // "Mobile service provider not available.");
                }
                Long providerId = new ArrayList<>(smsProviders).get(0).getId();
                final String message = "Hi  " + firstname + "," + "\n" + "Kindly use your registered email or phone number to login \n"
                        + "\n Password: " + password + "\nYou will be required to change your password on first login.";
                String externalId = null;
                Group group = null;
                Staff staff = appUser.getStaff();
                SmsCampaign smsCampaign = null;
                boolean isNotification = false;
                SmsMessage smsMessage = SmsMessage.instance(externalId, group, null, staff, SmsMessageStatusType.PENDING, message, mobile,
                        smsCampaign, isNotification);
                this.smsMessageRepository.save(smsMessage);
                this.smsMessageScheduledJobService.sendTriggeredMessage(new ArrayList<>(Arrays.asList(smsMessage)), providerId);
            } catch (Exception e) {
                log.warn("sms gateway not available: {}", e);
                throw new PlatformDataIntegrityException("error.msg", "Sms service unavailable.");
            }
        }
    }

    public static String randomAuthorizationTokenGeneration() {
        Integer randomPIN = (int) (secureRandom.nextDouble() * 9000) + 1000;
        return randomPIN.toString();
    }

}
