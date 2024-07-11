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
package org.apache.fineract.infrastructure.security.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.security.constants.TwoFactorConstants;
import org.apache.fineract.infrastructure.security.data.AuthenticatedUserData;
import org.apache.fineract.infrastructure.security.exception.NoAuthorizationException;
import org.apache.fineract.infrastructure.security.service.SpringSecurityPlatformSecurityContext;
import org.apache.fineract.infrastructure.security.service.business.AuthenticationBusinessReadPlatformService;
import org.apache.fineract.infrastructure.security.service.business.AuthenticationBusinessWritePlatformService;
import org.apache.fineract.organisation.business.businesstime.domain.BusinessTime;
import org.apache.fineract.organisation.business.businesstime.domain.BusinessTimeRepositoryWrapper;
import org.apache.fineract.organisation.business.businesstime.exception.BusinessTimeNotFoundException;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformService;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.business.AppUserExtension;
import org.apache.fineract.useradministration.domain.business.AppUserExtensionRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@Path("/authentication")
@Tag(name = "Authentication HTTP Basic", description = "An API capability that allows client applications to verify authentication details using HTTP Basic Authentication.")
public class AuthenticationApiResource {

    @Value("${fineract.security.2fa.enabled}")
    private boolean twoFactorEnabled;

    public static class AuthenticateRequest {

        public String username;
        public String password;
    }

    private final DaoAuthenticationProvider customAuthenticationProvider;
    private final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService;
    private final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext;
    private final ClientReadPlatformService clientReadPlatformService;
    private final AuthenticationBusinessWritePlatformService authenticationBusinessWritePlatformService;
    private final AuthenticationBusinessReadPlatformService authenticationBusinessReadPlatformService;
    private final AppUserExtensionRepositoryWrapper appUserExtensionRepositoryWrapper;
    private final ClientBusinessReadPlatformService clientBusinessReadPlatformService;
    private final BusinessTimeRepositoryWrapper businessTimeRepository;
    private final ConfigurationReadPlatformService configurationReadPlatformService;

    @Autowired
    public AuthenticationApiResource(
            @Qualifier("customAuthenticationProvider") final DaoAuthenticationProvider customAuthenticationProvider,
            final ToApiJsonSerializer<AuthenticatedUserData> apiJsonSerializerService,
            final SpringSecurityPlatformSecurityContext springSecurityPlatformSecurityContext,
            ClientReadPlatformService aClientReadPlatformService,
            final AuthenticationBusinessWritePlatformService authenticationBusinessWritePlatformService,
            final AuthenticationBusinessReadPlatformService authenticationBusinessReadPlatformService,
            final AppUserExtensionRepositoryWrapper appUserExtensionRepositoryWrapper,
            final ClientBusinessReadPlatformService clientBusinessReadPlatformService,
            final BusinessTimeRepositoryWrapper businessTimeRepository,
            final ConfigurationReadPlatformService configurationReadPlatformService) {
        this.customAuthenticationProvider = customAuthenticationProvider;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.springSecurityPlatformSecurityContext = springSecurityPlatformSecurityContext;
        clientReadPlatformService = aClientReadPlatformService;
        this.authenticationBusinessWritePlatformService = authenticationBusinessWritePlatformService;
        this.authenticationBusinessReadPlatformService = authenticationBusinessReadPlatformService;
        this.appUserExtensionRepositoryWrapper = appUserExtensionRepositoryWrapper;
        this.clientBusinessReadPlatformService = clientBusinessReadPlatformService;
        this.businessTimeRepository = businessTimeRepository;
        this.configurationReadPlatformService = configurationReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AuthenticationApiResourceSwagger.PostAuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Unauthenticated. Please login") })
    public String authenticate(@Parameter(hidden = true) final String apiRequestBodyAsJson,
            @QueryParam("returnClientList") @DefaultValue("false") boolean returnClientList) {
        // TODO FINERACT-819: sort out Jersey so JSON conversion does not have
        // to be done explicitly via GSON here, but implicit by arg
        AuthenticateRequest request = new Gson().fromJson(apiRequestBodyAsJson, AuthenticateRequest.class);
        if (request == null) {
            throw new IllegalArgumentException(
                    "Invalid JSON in BODY (no longer URL param; see FINERACT-726) of POST to /authentication: " + apiRequestBodyAsJson);
        }
        if (request.username == null || request.password == null) {
            throw new IllegalArgumentException("Username or Password is null in JSON (see FINERACT-726) of POST to /authentication: "
                    + apiRequestBodyAsJson + "; username=" + request.username + ", password=" + request.password);
        }
        final String username = request.username;
        // check login attempts
        this.authenticationBusinessWritePlatformService.lockUserAfterMultipleAttempts(username, false);
        final Authentication authentication = new UsernamePasswordAuthenticationToken(username, request.password);
        Authentication authenticationCheck = null;
        // final Authentication authenticationCheck = this.customAuthenticationProvider.authenticate(authentication);
        try {
            authenticationCheck = this.customAuthenticationProvider.authenticate(authentication);
        } catch (AuthenticationException e) {
            throw new NoAuthorizationException(e.getMessage());
        }
        final Collection<String> permissions = new ArrayList<>();
        AuthenticatedUserData authenticatedUserData = new AuthenticatedUserData(request.username, permissions);

        Long userId = null;
        if (authenticationCheck.isAuthenticated()) {
            final Collection<GrantedAuthority> authorities = new ArrayList<>(authenticationCheck.getAuthorities());
            for (final GrantedAuthority grantedAuthority : authorities) {
                permissions.add(grantedAuthority.getAuthority());
            }

            final byte[] base64EncodedAuthenticationKey = Base64.getEncoder()
                    .encode((request.username + ":" + request.password).getBytes(StandardCharsets.UTF_8));

            final AppUser principal = (AppUser) authenticationCheck.getPrincipal();
            final Collection<RoleData> roles = new ArrayList<>();
            final Set<Role> userRoles = principal.getRoles();
            for (final Role role : userRoles) {
                roles.add(role.toData());
                validateBusinessTime(role.getId());
            }

            final Long officeId = principal.getOffice().getId();
            final String officeName = principal.getOffice().getName();

            final Long staffId = principal.getStaffId();
            final String staffDisplayName = principal.getStaffDisplayName();

            final EnumOptionData organisationalRole = principal.organisationalRoleData();

            boolean isTwoFactorRequired = this.twoFactorEnabled
                    && !principal.hasSpecificPermissionTo(TwoFactorConstants.BYPASS_TWO_FACTOR_PERMISSION);
            userId = principal.getId();
            AppUserExtension appUserExtension = this.appUserExtensionRepositoryWrapper.findByAppuserId(principal);
            Boolean isMerchant = ObjectUtils.isNotEmpty(appUserExtension) ? appUserExtension.isMerchant() : false;

            if (this.springSecurityPlatformSecurityContext.doesPasswordHasToBeRenewed(principal)) {
                authenticatedUserData = new AuthenticatedUserData(request.username, userId,
                        new String(base64EncodedAuthenticationKey, StandardCharsets.UTF_8), isTwoFactorRequired);
            } else {

                authenticatedUserData = new AuthenticatedUserData(request.username, officeId, officeName, staffId, staffDisplayName,
                        organisationalRole, roles, permissions, principal.getId(),
                        new String(base64EncodedAuthenticationKey, StandardCharsets.UTF_8), isTwoFactorRequired,
                        returnClientList
                                ? (isMerchant ? clientBusinessReadPlatformService.retrieveMerchantClients(userId)
                                        : clientReadPlatformService.retrieveUserClients(userId))
                                : null);
            }
            authenticatedUserData.setFirstTimeLoginRemaining(principal.isFirstTimeLoginRemaining());

        }

        if (userId != null) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("username", request.username);
            // log the current user
            this.authenticationBusinessWritePlatformService.loggedUserLogIn(jsonObject.toString(), userId);
            final LocalDateTime lastLoginDate = this.authenticationBusinessReadPlatformService.lastLoginDate(userId);
            authenticatedUserData.setLastLoggedIn(lastLoginDate);
        }
        // clear login attempts if available
        this.authenticationBusinessWritePlatformService.lockUserAfterMultipleAttempts(username, true);
        return this.apiJsonSerializerService.serialize(authenticatedUserData);
    }

    private void validateBusinessTime(Long roleId) {
        final GlobalConfigurationPropertyData businessLoginTime = this.configurationReadPlatformService
                .retrieveGlobalConfigurationX("business-login-time");
        if (BooleanUtils.isTrue(businessLoginTime.isEnabled())) {
            // only check Business Time Configured
            LocalDateTime today = LocalDateTime.now(DateUtils.getDateTimeZoneOfTenant());
            DayOfWeek weekDay = today.getDayOfWeek();
            LocalTime time = today.toLocalTime();

            BusinessTime businessTime = this.businessTimeRepository.findByRoleIdAndWeekDayId(roleId, weekDay.getValue());
            if (businessTime != null) {
                LocalTime businessStartTime = businessTime.getStartTime();
                LocalTime businessEndTime = businessTime.getEndTime();

                if (businessStartTime == null || businessEndTime == null) {
                    throw new BusinessTimeNotFoundException("No login time set for user.");
                }

                if (!time.isAfter(businessStartTime)) {
                    throw new BusinessTimeNotFoundException(businessStartTime, " before business start");
                }

                if (!time.isBefore(businessEndTime)) {
                    throw new BusinessTimeNotFoundException(businessEndTime, "after business end");
                }
            } else {
                throw new BusinessTimeNotFoundException("User is not allowed to access the application today.");
            }
        }
    }
}
