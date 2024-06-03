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
package org.apache.fineract.portfolio.self.account.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.api.business.AccountTransfersBusinessApiResource;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/accounttransfers/business/{clientId}")
@Component
@Scope("singleton")
@Tag(name = "Account Transfers", description = "Ability to be able to transfer monetary funds from one account to another.\n\nNote: At present only savings account to savings account transfers are supported.")
public class SelfAccountTransfersBusinessApiResource {

    private final PlatformSecurityContext context;
    private final AccountTransfersBusinessApiResource accountTransfersBusinessApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfAccountTransfersBusinessApiResource(final PlatformSecurityContext context,
            final AccountTransfersBusinessApiResource accountTransfersBusinessApiResource, final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.context = context;
        this.accountTransfersBusinessApiResource = accountTransfersBusinessApiResource;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
    }

    @POST
    @Path("savings/template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Account Transfer Template", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String templateSavings(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId, @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);

        return this.accountTransfersBusinessApiResource.templateSavings(apiRequestBodyAsJson, uriInfo);
    }

    @POST
    @Path("savings")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create new Transfer", description = "Ability to create new transfer of monetary funds from one savings account to another.")
    @RequestBody(required = true
    )
    @ApiResponse(responseCode = "200", description = "OK"
    )
    public String create(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId, @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateAppuserClientsMapping(clientId);

        return this.accountTransfersBusinessApiResource.create(apiRequestBodyAsJson);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
