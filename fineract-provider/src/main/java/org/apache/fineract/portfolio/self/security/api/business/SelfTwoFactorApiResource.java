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
package org.apache.fineract.portfolio.self.security.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.security.api.TwoFactorApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Profile("twofactor")
@Path("/self/twofactor/{clientId}")
@Tag(name = "Self Two Factor", description = "")

public class SelfTwoFactorApiResource {

    private final PlatformSecurityContext context;

    private final TwoFactorApiResource twoFactorApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfTwoFactorApiResource(PlatformSecurityContext context, final TwoFactorApiResource twoFactorApiResource,
            final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.context = context;
        this.twoFactorApiResource = twoFactorApiResource;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public String getOTPDeliveryMethods(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.twoFactorApiResource.getOTPDeliveryMethods(uriInfo);
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String requestToken(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("deliveryMethod") final String deliveryMethod,
            @QueryParam("extendedToken") @DefaultValue("false") boolean extendedAccessToken, @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.twoFactorApiResource.requestToken(deliveryMethod, extendedAccessToken, uriInfo);
    }

    @Path("validate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public String validate(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("token") final String token) {
        validateAppuserClientsMapping(clientId);
        return this.twoFactorApiResource.validate(token);
    }

    @Path("invalidate")
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Invalidate Access Token", description = "Two factor access tokens should be invalidated on logout.")
    public String updateConfiguration(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            final String apiRequestBodyAsJson) {
        validateAppuserClientsMapping(clientId);
        return this.twoFactorApiResource.updateConfiguration(apiRequestBodyAsJson);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
