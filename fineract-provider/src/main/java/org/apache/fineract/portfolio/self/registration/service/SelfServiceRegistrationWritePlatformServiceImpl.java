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
package org.apache.fineract.portfolio.self.registration.service;

import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.OFFICE_ID;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.SAVINGS_PRODUCT_ID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.campaigns.sms.data.SmsProviderData;
import org.apache.fineract.infrastructure.campaigns.sms.domain.SmsCampaign;
import org.apache.fineract.infrastructure.campaigns.sms.service.SmsCampaignDropdownReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.GmailBackedPlatformEmailService;
import org.apache.fineract.infrastructure.sms.domain.SmsMessage;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageRepository;
import org.apache.fineract.infrastructure.sms.domain.SmsMessageStatusType;
import org.apache.fineract.infrastructure.sms.scheduler.SmsMessageScheduledJobService;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistration;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistrationRepository;
import org.apache.fineract.portfolio.self.registration.exception.SelfServiceRegistrationNotFoundException;
import org.apache.fineract.simplifytech.data.ApiResponseMessage;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserClientMapping;
import org.apache.fineract.useradministration.domain.AppUserClientMappingRepository;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicy;
import org.apache.fineract.useradministration.domain.PasswordValidationPolicyRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class SelfServiceRegistrationWritePlatformServiceImpl implements SelfServiceRegistrationWritePlatformService {

    private final SelfServiceRegistrationRepository selfServiceRegistrationRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService;
    private final ClientRepositoryWrapper clientRepository;
    private final PasswordValidationPolicyRepository passwordValidationPolicy;
    private final UserDomainService userDomainService;
    private final GmailBackedPlatformEmailService gmailBackedPlatformEmailService;
    private final SmsMessageRepository smsMessageRepository;
    private final SmsMessageScheduledJobService smsMessageScheduledJobService;
    private final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService;
    private final AppUserReadPlatformService appUserReadPlatformService;
    private final RoleRepository roleRepository;
    private static final SecureRandom secureRandom = new SecureRandom();
    final SelfServiceRegistrationCommandFromApiJsonDeserializer selfServiceRegistrationCommandFromApiJsonDeserializer;
    final AppUserClientMappingRepository appUserClientMappingRepository;
    private final Long savingsProductId;
    private final Long officeId;

    @Autowired
    public SelfServiceRegistrationWritePlatformServiceImpl(final SelfServiceRegistrationRepository selfServiceRegistrationRepository,
            final FromJsonHelper fromApiJsonHelper,
            final SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService,
            final ClientRepositoryWrapper clientRepository, final PasswordValidationPolicyRepository passwordValidationPolicy,
            final UserDomainService userDomainService, final GmailBackedPlatformEmailService gmailBackedPlatformEmailService,
            final SmsMessageRepository smsMessageRepository, SmsMessageScheduledJobService smsMessageScheduledJobService,
            final SmsCampaignDropdownReadPlatformService smsCampaignDropdownReadPlatformService,
            final AppUserReadPlatformService appUserReadPlatformService, final RoleRepository roleRepository,
            final AppUserClientMappingRepository appUserClientMappingRepository, final ApplicationContext context,
            final SelfServiceRegistrationCommandFromApiJsonDeserializer selfServiceRegistrationCommandFromApiJsonDeserializer) {
        this.selfServiceRegistrationRepository = selfServiceRegistrationRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.selfServiceRegistrationReadPlatformService = selfServiceRegistrationReadPlatformService;
        this.clientRepository = clientRepository;
        this.passwordValidationPolicy = passwordValidationPolicy;
        this.userDomainService = userDomainService;
        this.gmailBackedPlatformEmailService = gmailBackedPlatformEmailService;
        this.smsMessageRepository = smsMessageRepository;
        this.smsMessageScheduledJobService = smsMessageScheduledJobService;
        this.smsCampaignDropdownReadPlatformService = smsCampaignDropdownReadPlatformService;
        this.appUserReadPlatformService = appUserReadPlatformService;
        this.roleRepository = roleRepository;
        this.selfServiceRegistrationCommandFromApiJsonDeserializer = selfServiceRegistrationCommandFromApiJsonDeserializer;
        Environment environment = context.getEnvironment();
        this.savingsProductId = Long.valueOf(environment.getProperty(SAVINGS_PRODUCT_ID));
        this.officeId = Long.valueOf(environment.getProperty(OFFICE_ID));
        this.appUserClientMappingRepository = appUserClientMappingRepository;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ApiResponseMessage createCustomeronRequest(String apiRequestBodyAsJson) {
        this.selfServiceRegistrationCommandFromApiJsonDeserializer.validateForCreate(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);

        String firstName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.firstNameParamName, element);

        String lastName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.lastNameParamName, element);

        String email = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.emailParamName, element);

        String mobileNumber = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.mobileNumberParamName, element);

        validateForDuplicateUsername(email);

        throwExceptionIfValidationErrorExists(email, mobileNumber);

        String authenticationToken = randomAuthorizationTokenGeneration();
        String password = authenticationToken;
        Client client = Client.createInstance(savingsProductId, LegalForm.PERSON.getValue(), mobileNumber, email, firstName, lastName);
        this.clientRepository.saveAndFlush(client);
        SelfServiceRegistration selfServiceRegistration = SelfServiceRegistration.instance(client, client.getAccountNumber(), firstName,
                lastName, mobileNumber, email, SelfServiceApiConstants.bothModeParamName, email, password);
        this.selfServiceRegistrationRepository.saveAndFlush(selfServiceRegistration);
        final JsonObject createUserObject = new JsonObject();
        createUserObject.addProperty(SelfServiceApiConstants.requestIdParamName, selfServiceRegistration.getId());
        createUserObject.addProperty(SelfServiceApiConstants.authenticationTokenParamName,
                selfServiceRegistration.getAuthenticationToken());
        createCustomer(createUserObject.toString());
        sendAuthorizationToken(selfServiceRegistration.getClient(), selfServiceRegistration.getPassword(),
                selfServiceRegistration.getEmail(), selfServiceRegistration.getMobileNumber(), selfServiceRegistration.getFirstName());
        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage(HttpStatus.CREATED.value(),
                SelfServiceApiConstants.createRequestSuccessMessage, selfServiceRegistration.getId(), null);
        return apiResponseMessage;
    }

    @Override
    public ApiResponseMessage resendCustomeronRequest(String apiRequestBodyAsJson) {
        this.selfServiceRegistrationCommandFromApiJsonDeserializer.validateForResend(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);

        Long requestId = this.fromApiJsonHelper.extractLongNamed(SelfServiceApiConstants.requestIdParamName, element);
        SelfServiceRegistration selfServiceRegistration = this.selfServiceRegistrationRepository.findById(requestId)
                .orElseThrow(() -> new SelfServiceRegistrationNotFoundException(requestId));

        boolean isClientExist = this.selfServiceRegistrationReadPlatformService.isClientExist(selfServiceRegistration.getAccountNumber(),
                selfServiceRegistration.getFirstName(), selfServiceRegistration.getLastName(), selfServiceRegistration.getMobileNumber(),
                false);
        if (!isClientExist) {
            throw new ClientNotFoundException();
        }

        sendAuthorizationToken(selfServiceRegistration.getClient(), selfServiceRegistration.getPassword(),
                selfServiceRegistration.getEmail(), selfServiceRegistration.getMobileNumber(), selfServiceRegistration.getFirstName());

        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage(HttpStatus.OK.value(),
                SelfServiceApiConstants.resendRequestSuccessMessage, selfServiceRegistration.getId(), null);
        return apiResponseMessage;
    }

    @Override
    public ApiResponseMessage validateCustomer(String apiRequestBodyAsJson) {
        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage();
        apiResponseMessage.setStatus(HttpStatus.NOT_FOUND.value());
        apiResponseMessage.setMessage("Record not found");

        this.selfServiceRegistrationCommandFromApiJsonDeserializer.validateMode(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);
        String authenticationMode = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationModeParamName, element);
        String value = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.valueParamName, element);
        boolean isEmailAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.emailModeParamName);
        boolean isMobileAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.mobileModeParamName);
        ClientData clientData = null;
        Client client = null;
        if (isEmailAuthenticationMode) {
            // check email
            client = this.clientRepository.findByEmailAddress(value);
            apiResponseMessage.setStatus(HttpStatus.OK.value());
            apiResponseMessage.setMessage("Record found");
        } else if (isMobileAuthenticationMode) {
            // check mobile
            client = this.clientRepository.findByMobileNo(value);
            apiResponseMessage.setStatus(HttpStatus.OK.value());
            apiResponseMessage.setMessage("Record found");
        } else {
            throw new PlatformDataIntegrityException("error.msg.customer.validate.mode", "Validation mode not supported");
        }
        if (ObjectUtils.isNotEmpty(client)) {
            clientData = ClientData.lookup(client.getId(), client.getDisplayName(), client.getMiddlename(), client.mobileNo(),
                    client.emailAddress(), client.getLastname());
            apiResponseMessage.setData(clientData);
        }

        return apiResponseMessage;
    }

    @Override
    public ApiResponseMessage resetCustomerPassword(String apiRequestBodyAsJson) {

        this.selfServiceRegistrationCommandFromApiJsonDeserializer.validateMode(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);
        String authenticationMode = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationModeParamName, element);
        String value = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.valueParamName, element);
        boolean isEmailAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.emailModeParamName);
        boolean isMobileAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.mobileModeParamName);
        Client client;
        if (isEmailAuthenticationMode) {
            // check email
            client = this.clientRepository.findByEmailAddress(value);
        } else if (isMobileAuthenticationMode) {
            // check mobile
            client = this.clientRepository.findByMobileNo(value);
        } else {
            throw new PlatformDataIntegrityException("error.msg.customer.reset.mode", "Password reset mode not supported");
        }
        if (ObjectUtils.isNotEmpty(client)) {
            List<AppUserClientMapping> appUserClientMappings = this.appUserClientMappingRepository.findByClientId(client.getId());
            if (!CollectionUtils.isEmpty(appUserClientMappings)) {
                AppUser appUser = appUserClientMappings.get(0).getAppUser();
                String authenticationToken = randomAuthorizationTokenGeneration();
                String password = authenticationToken;
                appUser.setPassword(password);
                this.userDomainService.createCustomer(appUser, true);
                sendAuthorizationToken(client, password, client.emailAddress(), client.mobileNo(), client.getFirstname());
            }
        }

        return new ApiResponseMessage(HttpStatus.OK.value(), "A reset details was sent to your " + authenticationMode, null, null);
    }

    private void throwExceptionIfValidationErrorExists(String email, String mobileNumber) {

        boolean isClientExist = this.selfServiceRegistrationReadPlatformService.isClientExist(email, mobileNumber);
        if (isClientExist) {
            throw new ClientNotFoundException("201", "Your record is available, kindly login or reset password.");
        }
    }

    @Override
    public SelfServiceRegistration createRegistrationRequest(String apiRequestBodyAsJson) {
        Gson gson = new Gson();
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson,
                SelfServiceApiConstants.REGISTRATION_REQUEST_DATA_PARAMETERS);
        JsonElement element = gson.fromJson(apiRequestBodyAsJson.toString(), JsonElement.class);

        String accountNumber = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.accountNumberParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.accountNumberParamName).value(accountNumber).notNull().notBlank()
                .notExceedingLengthOf(100);

        String firstName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.firstNameParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.firstNameParamName).value(firstName).notBlank()
                .notExceedingLengthOf(100);

        String lastName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.lastNameParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.lastNameParamName).value(lastName).notBlank().notExceedingLengthOf(100);

        String username = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.usernameParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.usernameParamName).value(username).notBlank().notExceedingLengthOf(100);

        // validate password policy
        String password = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.passwordParamName, element);
        final PasswordValidationPolicy validationPolicy = this.passwordValidationPolicy.findActivePasswordValidationPolicy();
        final String regex = validationPolicy.getRegex();
        final String description = validationPolicy.getDescription();
        baseDataValidator.reset().parameter(SelfServiceApiConstants.passwordParamName).value(password)
                .matchesRegularExpression(regex, description).notExceedingLengthOf(100);

        String authenticationMode = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationModeParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.authenticationModeParamName).value(authenticationMode).notBlank()
                .isOneOfTheseStringValues(SelfServiceApiConstants.emailModeParamName, SelfServiceApiConstants.mobileModeParamName);

        String email = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.emailParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.emailParamName).value(email).notNull().notBlank()
                .notExceedingLengthOf(100);

        boolean isEmailAuthenticationMode = authenticationMode.equalsIgnoreCase(SelfServiceApiConstants.emailModeParamName);
        String mobileNumber = null;
        if (!isEmailAuthenticationMode) {
            mobileNumber = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.mobileNumberParamName, element);
            baseDataValidator.reset().parameter(SelfServiceApiConstants.mobileNumberParamName).value(mobileNumber).notNull()
                    .validatePhoneNumber();
        }
        validateForDuplicateUsername(username);

        throwExceptionIfValidationError(dataValidationErrors, accountNumber, firstName, lastName, mobileNumber, isEmailAuthenticationMode);

        String authenticationToken = randomAuthorizationTokenGeneration();
        Client client = this.clientRepository.getClientByAccountNumber(accountNumber);
        SelfServiceRegistration selfServiceRegistration = SelfServiceRegistration.instance(client, accountNumber, firstName, lastName,
                mobileNumber, email, authenticationToken, username, password);
        this.selfServiceRegistrationRepository.saveAndFlush(selfServiceRegistration);
        sendAuthorizationToken(selfServiceRegistration, isEmailAuthenticationMode);
        return selfServiceRegistration;

    }

    public void validateForDuplicateUsername(String username) {
        boolean isDuplicateUserName = this.appUserReadPlatformService.isUsernameExist(username);
        if (isDuplicateUserName) {
            final StringBuilder defaultMessageBuilder = new StringBuilder("User with username ").append(username)
                    .append(" already exists.");
            throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessageBuilder.toString(),
                    SelfServiceApiConstants.usernameParamName, username);
        }
    }

    public void sendAuthorizationToken(final Client client, final String password, final String email, final String mobile,
            final String firstname) {
        if (StringUtils.isNotBlank(email)) {
            final String subject = "Hello " + firstname + ",";
            final String body = "Kindly use your registered email or phone number to login \n Password: " + password + "\n"
                    + "You will be required to change your password on first login.\n" + "Thanks for choosing.";

            final EmailDetail emailDetail = new EmailDetail(subject, body, email, firstname);
            this.gmailBackedPlatformEmailService.sendDefinedEmail(emailDetail);

        }
        if (StringUtils.isNotBlank(mobile)) {
            Collection<SmsProviderData> smsProviders = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
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
            Staff staff = null;
            SmsCampaign smsCampaign = null;
            boolean isNotification = false;
            SmsMessage smsMessage = SmsMessage.instance(externalId, group, client, staff, SmsMessageStatusType.PENDING, message, mobile,
                    smsCampaign, isNotification);
            this.smsMessageRepository.save(smsMessage);
            this.smsMessageScheduledJobService.sendTriggeredMessage(new ArrayList<>(Arrays.asList(smsMessage)), providerId);

        }
    }

    public void sendAuthorizationToken(SelfServiceRegistration selfServiceRegistration, Boolean isEmailAuthenticationMode) {
        if (isEmailAuthenticationMode) {
            sendAuthorizationMail(selfServiceRegistration);
        } else {
            sendAuthorizationMessage(selfServiceRegistration);
        }
    }

    private void sendAuthorizationMessage(SelfServiceRegistration selfServiceRegistration) {
        Collection<SmsProviderData> smsProviders = this.smsCampaignDropdownReadPlatformService.retrieveSmsProviders();
        if (smsProviders.isEmpty()) {
            throw new PlatformDataIntegrityException("error.msg.mobile.service.provider.not.available",
                    "Mobile service provider not available.");
        }
        Long providerId = new ArrayList<>(smsProviders).get(0).getId();
        final String message = "Hi  " + selfServiceRegistration.getFirstName() + "," + "\n"
                + "To create user, please use following details \n" + "Request Id : " + selfServiceRegistration.getId()
                + "\n Authentication Token : " + selfServiceRegistration.getAuthenticationToken();
        String externalId = null;
        Group group = null;
        Staff staff = null;
        SmsCampaign smsCampaign = null;
        boolean isNotification = false;
        SmsMessage smsMessage = SmsMessage.instance(externalId, group, selfServiceRegistration.getClient(), staff,
                SmsMessageStatusType.PENDING, message, selfServiceRegistration.getMobileNumber(), smsCampaign, isNotification);
        this.smsMessageRepository.save(smsMessage);
        this.smsMessageScheduledJobService.sendTriggeredMessage(new ArrayList<>(Arrays.asList(smsMessage)), providerId);
    }

    private void sendAuthorizationMail(SelfServiceRegistration selfServiceRegistration) {
        final String subject = "Authorization token ";
        final String body = "Hi  " + selfServiceRegistration.getFirstName() + "," + "\n" + "To create user, please use following details\n"
                + "Request Id : " + selfServiceRegistration.getId() + "\n Authentication Token : "
                + selfServiceRegistration.getAuthenticationToken();

        final EmailDetail emailDetail = new EmailDetail(subject, body, selfServiceRegistration.getEmail(),
                selfServiceRegistration.getFirstName());
        this.gmailBackedPlatformEmailService.sendDefinedEmail(emailDetail);
    }

    private void throwExceptionIfValidationError(final List<ApiParameterError> dataValidationErrors, String accountNumber, String firstName,
            String lastName, String mobileNumber, boolean isEmailAuthenticationMode) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
        boolean isClientExist = this.selfServiceRegistrationReadPlatformService.isClientExist(accountNumber, firstName, lastName,
                mobileNumber, isEmailAuthenticationMode);
        if (!isClientExist) {
            throw new ClientNotFoundException();
        }
    }

    public static String randomAuthorizationTokenGeneration() {
        Integer randomPIN = (int) (secureRandom.nextDouble() * 9000) + 1000;
        return randomPIN.toString();
    }

    @Override
    public AppUser createUser(String apiRequestBodyAsJson) {
        JsonCommand command = null;
        String username = null;
        try {
            Gson gson = new Gson();
            final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson,
                    SelfServiceApiConstants.CREATE_USER_REQUEST_DATA_PARAMETERS);
            JsonElement element = gson.fromJson(apiRequestBodyAsJson.toString(), JsonElement.class);

            Long id = this.fromApiJsonHelper.extractLongNamed(SelfServiceApiConstants.requestIdParamName, element);
            baseDataValidator.reset().parameter(SelfServiceApiConstants.requestIdParamName).value(id).notNull().integerGreaterThanZero();
            command = JsonCommand.fromJsonElement(id, element);
            String authenticationToken = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationTokenParamName,
                    element);
            baseDataValidator.reset().parameter(SelfServiceApiConstants.authenticationTokenParamName).value(authenticationToken).notBlank()
                    .notNull().notExceedingLengthOf(100);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            SelfServiceRegistration selfServiceRegistration = this.selfServiceRegistrationRepository
                    .getRequestByIdAndAuthenticationToken(id, authenticationToken);
            if (selfServiceRegistration == null) {
                throw new SelfServiceRegistrationNotFoundException(id, authenticationToken);
            }
            username = selfServiceRegistration.getUsername();
            Client client = selfServiceRegistration.getClient();
            final boolean passwordNeverExpire = true;
            final boolean isSelfServiceUser = true;
            final Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("DUMMY_ROLE_NOT_USED_OR_PERSISTED_TO_AVOID_EXCEPTION"));
            final Set<Role> allRoles = new HashSet<>();
            Role role = this.roleRepository.getRoleByName(SelfServiceApiConstants.SELF_SERVICE_USER_ROLE);
            if (role != null) {
                allRoles.add(role);
            } else {
                throw new RoleNotFoundException(SelfServiceApiConstants.SELF_SERVICE_USER_ROLE);
            }
            List<Client> clients = new ArrayList<>(Arrays.asList(client));
            User user = new User(selfServiceRegistration.getUsername(), selfServiceRegistration.getPassword(), authorities);
            AppUser appUser = new AppUser(client.getOffice(), user, allRoles, selfServiceRegistration.getEmail(), client.getFirstname(),
                    client.getLastname(), null, passwordNeverExpire, isSelfServiceUser, clients, null);
            this.userDomainService.create(appUser, true);
            return appUser;

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve, username);
            return null;
        } catch (final PersistenceException | AuthenticationServiceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve, username);
            return null;
        }

    }

    public AppUser createCustomer(String apiRequestBodyAsJson) {
        JsonCommand command = null;
        String username = null;
        try {
            Gson gson = new Gson();
            final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("user");
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson,
                    SelfServiceApiConstants.CREATE_USER_REQUEST_DATA_PARAMETERS);
            JsonElement element = gson.fromJson(apiRequestBodyAsJson.toString(), JsonElement.class);

            Long id = this.fromApiJsonHelper.extractLongNamed(SelfServiceApiConstants.requestIdParamName, element);
            baseDataValidator.reset().parameter(SelfServiceApiConstants.requestIdParamName).value(id).notNull().integerGreaterThanZero();
            command = JsonCommand.fromJsonElement(id, element);
            String authenticationToken = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationTokenParamName,
                    element);
            baseDataValidator.reset().parameter(SelfServiceApiConstants.authenticationTokenParamName).value(authenticationToken).notBlank()
                    .notNull().notExceedingLengthOf(100);

            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }

            SelfServiceRegistration selfServiceRegistration = this.selfServiceRegistrationRepository
                    .getRequestByIdAndAuthenticationToken(id, authenticationToken);
            if (selfServiceRegistration == null) {
                throw new SelfServiceRegistrationNotFoundException(id, authenticationToken);
            }
            username = selfServiceRegistration.getUsername();
            Client client = selfServiceRegistration.getClient();
            final boolean passwordNeverExpire = true;
            final boolean isSelfServiceUser = true;
            final Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("DUMMY_ROLE_NOT_USED_OR_PERSISTED_TO_AVOID_EXCEPTION"));
            final Set<Role> allRoles = new HashSet<>();
            Role role = this.roleRepository.getRoleByName(SelfServiceApiConstants.SELF_SERVICE_USER_ROLE);
            if (role != null) {
                allRoles.add(role);
            } else {
                throw new RoleNotFoundException(SelfServiceApiConstants.SELF_SERVICE_USER_ROLE);
            }
            List<Client> clients = new ArrayList<>(Arrays.asList(client));
            User user = new User(selfServiceRegistration.getUsername(), selfServiceRegistration.getPassword(), authorities);
            AppUser appUser = new AppUser(client.getOffice(), user, allRoles, selfServiceRegistration.getEmail(), client.getFirstname(),
                    client.getLastname(), null, passwordNeverExpire, isSelfServiceUser, clients, null);
            this.userDomainService.createCustomer(appUser, true);
            return appUser;

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve, username);
            return null;
        } catch (final PersistenceException | AuthenticationServiceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve, username);
            return null;
        }

    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve, String username) {
        if (realCause.getMessage().contains("'username_org'")) {
            final StringBuilder defaultMessageBuilder = new StringBuilder("User with username ").append(username)
                    .append(" already exists.");
            throw new PlatformDataIntegrityException("error.msg.user.duplicate.username", defaultMessageBuilder.toString(), "username",
                    username);
        }
        throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue", "Unknown data integrity issue with resource.");
    }

}
