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
package org.apache.fineract.portfolio.business.merchant.registration.service;

import static org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformServiceImpl.randomAuthorizationTokenGeneration;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.OFFICE_ID;
import static org.apache.fineract.simplifytech.data.ApplicationPropertiesConstant.SAVINGS_PRODUCT_RECONCILE_ID_API;

import com.google.common.base.Splitter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.sql.PreparedStatement;
import java.util.List;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistration;
import org.apache.fineract.portfolio.self.registration.domain.SelfServiceRegistrationRepository;
import org.apache.fineract.portfolio.self.registration.exception.SelfServiceRegistrationNotFoundException;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationReadPlatformService;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformServiceImpl;
import org.apache.fineract.simplifytech.data.ApiResponseMessage;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.UserDomainService;
import org.apache.fineract.useradministration.domain.business.AppUserMerchantMapping;
import org.apache.fineract.useradministration.domain.business.AppUserMerchantMappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class MerchantRegistrationWriteServiceImp implements MerchantRegistrationWriteService {

    private final MerchantServiceRegistrationCommandFromApiJsonDeserializer merchantServiceRegistrationCommandFromApiJsonDeserializer;
    private final SelfServiceRegistrationWritePlatformServiceImpl selfServiceRegistrationWritePlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Long savingsProductId;
    private final Long officeId;
    private final ClientRepositoryWrapper clientRepository;
    private final AppUserMerchantMappingRepository appUserMerchantMappingRepository;
    private final SelfServiceRegistrationCommandFromApiJsonDeserializer selfServiceRegistrationCommandFromApiJsonDeserializer;
    private final UserDomainService userDomainService;
    private final SelfServiceRegistrationRepository selfServiceRegistrationRepository;
    private final MerchantRegistrationReadPlatformService merchantRegistrationReadPlatformService;

    @Autowired
    public MerchantRegistrationWriteServiceImp(
            final SelfServiceRegistrationWritePlatformServiceImpl selfServiceRegistrationWritePlatformService,
            final MerchantServiceRegistrationCommandFromApiJsonDeserializer merchantServiceRegistrationCommandFromApiJsonDeserializer,
            final FromJsonHelper fromApiJsonHelper, final RoutingDataSource dataSource,
            final SelfServiceRegistrationReadPlatformService selfServiceRegistrationReadPlatformService, final ApplicationContext context,
            final ClientRepositoryWrapper clientRepository, final AppUserMerchantMappingRepository appUserMerchantMappingRepository,
            final SelfServiceRegistrationCommandFromApiJsonDeserializer selfServiceRegistrationCommandFromApiJsonDeserializer,
            final UserDomainService userDomainService, final SelfServiceRegistrationRepository selfServiceRegistrationRepository,
            final MerchantRegistrationReadPlatformService merchantRegistrationReadPlatformService) {
        this.selfServiceRegistrationWritePlatformService = selfServiceRegistrationWritePlatformService;
        this.merchantServiceRegistrationCommandFromApiJsonDeserializer = merchantServiceRegistrationCommandFromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.selfServiceRegistrationReadPlatformService = selfServiceRegistrationReadPlatformService;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        Environment environment = context.getEnvironment();

        this.savingsProductId = Long.valueOf(environment.getProperty(SAVINGS_PRODUCT_RECONCILE_ID_API));
        this.officeId = Long.valueOf(environment.getProperty(OFFICE_ID));
        this.clientRepository = clientRepository;
        this.appUserMerchantMappingRepository = appUserMerchantMappingRepository;
        this.selfServiceRegistrationCommandFromApiJsonDeserializer = selfServiceRegistrationCommandFromApiJsonDeserializer;
        this.userDomainService = userDomainService;
        this.selfServiceRegistrationRepository = selfServiceRegistrationRepository;
        this.merchantRegistrationReadPlatformService = merchantRegistrationReadPlatformService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ApiResponseMessage createMerchant(String apiRequestBodyAsJson) {

        this.merchantServiceRegistrationCommandFromApiJsonDeserializer.validateForCreate(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);

        String fullName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.fullNameParamName, element);

        String email = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.emailParamName, element);

        String mobileNumber = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.mobileNumberParamName, element);

        // String[] str = fullName.split("\\s+");
        List<String> str = Splitter.onPattern("\\s+").splitToList(fullName);
        String firstName = str.get(0);

        System.out.println("firstName " + firstName);

        String lastName = str.size() > 1 && StringUtils.isNotBlank(str.get(1)) ? str.get(1) : "Merchant";
        System.out.println("lastName" + lastName);
        System.out.println("str" + str);

        this.selfServiceRegistrationWritePlatformService.validateForDuplicateUsername(email);

        throwExceptionIfValidationErrorExists(email, mobileNumber);

        String authenticationToken = randomAuthorizationTokenGeneration();
        String password = authenticationToken;

        // create client
        Long clientId;
        try {
            final String accountNumber = new RandomPasswordGenerator(19).generate();

            KeyHolder keyHolder = new GeneratedKeyHolder();

            String clientSql = "INSERT INTO m_client  (default_savings_product, legal_form_enum, office_id, mobile_no, email_address,  fullName, created_by, created_on_utc, account_no, status_enum, display_name) VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(clientSql, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setLong(1, savingsProductId);
                ps.setInt(2, LegalForm.MERCHANT.getValue());
                ps.setLong(3, officeId);
                ps.setString(4, mobileNumber);
                ps.setString(5, email);
                ps.setString(6, fullName);
                ps.setInt(7, 1);
                ps.setString(8, accountNumber);
                ps.setInt(9, ClientStatus.PENDING.getValue());
                ps.setString(10, fullName);

                return ps;
            }, keyHolder);

            clientId = keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();

        } catch (DataAccessException e) {
            log.warn("Customer already exists: {}", e);
            throw new PlatformDataIntegrityException("error.msg.customer.validate.mode",
                    "Customer information already exist, use login or forgt password.");
        }

        Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);

        // generate accountNumber and update client
        String accountNumber = this.selfServiceRegistrationWritePlatformService.generateAccountNumberUpdateClient(client, clientId);

        KeyHolder keyHolderSelf = new GeneratedKeyHolder();
        String clientSqlSelf = "INSERT INTO request_audit_table  (client_id, account_number, firstname, lastname, mobile_number, email, authentication_token, username, password, created_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(clientSqlSelf, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setLong(1, clientId);
            ps.setString(2, accountNumber);
            ps.setString(3, firstName);
            ps.setString(4, lastName);
            ps.setString(5, mobileNumber);
            ps.setString(6, email);
            ps.setString(7, SelfServiceApiConstants.bothModeParamName);
            ps.setString(8, email);
            ps.setString(9, password);
            return ps;
        }, keyHolderSelf);
        Long selfClientId = (Long) keyHolderSelf.getKey();

        SelfServiceRegistration selfServiceRegistration = this.selfServiceRegistrationWritePlatformService.RegisterSelfUser(selfClientId,
                SelfServiceApiConstants.MERCHANT_USER_ROLE, true);

        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage(HttpStatus.CREATED.value(),
                SelfServiceApiConstants.createRequestSuccessMessage, selfServiceRegistration.getId(), null);
        return apiResponseMessage;
    }

    @Override
    public ApiResponseMessage resetMerchantPassword(String apiRequestBodyAsJson) {

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
            List<AppUserMerchantMapping> AppUserMerchantMappings = this.appUserMerchantMappingRepository.findByClientId(client.getId());
            if (!CollectionUtils.isEmpty(AppUserMerchantMappings)) {
                AppUser appUser = AppUserMerchantMappings.get(0).getAppUser();
                String authenticationToken = randomAuthorizationTokenGeneration();
                String password = authenticationToken;
                appUser.setPassword(password);
                this.userDomainService.createCustomer(appUser, true);
                this.selfServiceRegistrationWritePlatformService.sendAuthorizationToken(client, password, client.emailAddress(),
                        client.mobileNo(), client.getDisplayName(), "Reset Password");
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
    public ApiResponseMessage resendCustomeronRequest(String apiRequestBodyAsJson) {
        this.selfServiceRegistrationCommandFromApiJsonDeserializer.validateForResend(apiRequestBodyAsJson);
        Gson gson = new Gson();
        JsonElement element = gson.fromJson(apiRequestBodyAsJson, JsonElement.class);

        Long requestId = this.fromApiJsonHelper.extractLongNamed(SelfServiceApiConstants.requestIdParamName, element);
        SelfServiceRegistration selfServiceRegistration = this.selfServiceRegistrationRepository.findById(requestId)
                .orElseThrow(() -> new SelfServiceRegistrationNotFoundException(requestId));

        boolean isClientExist = this.merchantRegistrationReadPlatformService.isClientExist(selfServiceRegistration.getAccountNumber(),
                selfServiceRegistration.getEmail(), selfServiceRegistration.getMobileNumber(), false);
        if (!isClientExist) {
            throw new ClientNotFoundException();
        }

        this.selfServiceRegistrationWritePlatformService.sendAuthorizationToken(selfServiceRegistration.getClient(),
                selfServiceRegistration.getPassword(), selfServiceRegistration.getEmail(), selfServiceRegistration.getMobileNumber(),
                selfServiceRegistration.getFirstName(), "Onboarding-Resent");

        final ApiResponseMessage apiResponseMessage = new ApiResponseMessage(HttpStatus.OK.value(),
                SelfServiceApiConstants.resendRequestSuccessMessage, selfServiceRegistration.getId(), null);
        return apiResponseMessage;
    }
}
