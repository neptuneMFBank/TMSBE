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
package org.apache.fineract.portfolio.self.business.infrastructure.codes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.codes.api.CodeValuesApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/codes/{clientId}/{codeId}/codevalues")
@Component
@Scope("singleton")

@Tag(name = "Self Code Business", description = "")
public class SelfCodeValuesApiResource {

    private final PlatformSecurityContext context;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final CodeValuesApiResource codeValuesApiResource;

    @Autowired
    public SelfCodeValuesApiResource(final PlatformSecurityContext context,
            final AppuserClientMapperReadService appUserClientMapperReadService, final CodeValuesApiResource codeValuesApiResource) {
        this.context = context;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.codeValuesApiResource = codeValuesApiResource;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Code Values", description = """
            Returns the list of Code Values for a given Code

            Example Requests:

            codes/1/codevalues""", parameters = @Parameter(name = "codeId", description = "co"))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "A List of code values for a given code") })
    public String retrieveAllCodeValues(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("codeId") @Parameter(description = "codeId") final Long codeId) {
        validateAppuserClientsMapping(clientId);
        return this.codeValuesApiResource.retrieveAllCodeValues(uriInfo, codeId);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

}
