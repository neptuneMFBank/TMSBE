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
package org.apache.fineract.portfolio.self.business.collateral.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.collateralmanagement.api.ClientCollateralManagementApiResource;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("self/clients/{clientId}/collaterals")
@Component
@Scope("singleton")
@Tag(name = "Client Collateral Management", description = "Client Collateral Management is for managing collateral operations")
public class SelfClientCollateralManagementApiResource {

    private final PlatformSecurityContext context;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final ClientCollateralManagementApiResource clientCollateralManagementApiResource;

    public SelfClientCollateralManagementApiResource(
            final AppuserClientMapperReadService appUserClientMapperReadService,
            final ClientCollateralManagementApiResource clientCollateralManagementApiResource,
            final PlatformSecurityContext context) {
        this.context = context;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.clientCollateralManagementApiResource = clientCollateralManagementApiResource;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get Clients Collateral Products", description = "Get Collateral Product of a Client")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String getClientCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo, @QueryParam("prodId") @Parameter(description = "prodId") final Long prodId) {
        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.getClientCollateral(clientId, uriInfo, prodId);

    }

    @GET
    @Path("{clientCollateralId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get Client Collateral Data", description = "Get Client Collateral Data")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String getClientCollateralData(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("clientCollateralId") @Parameter(description = "clientCollateralId") final Long collateralId) {

        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.getClientCollateralData(clientId, collateralId);
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Get Client Collateral Template", description = "Get Client Collateral Template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String getClientCollateralTemplate(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.getClientCollateralTemplate(uriInfo, clientId);
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Add New Collateral For a Client", description = "Add New Collateral For a Client")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String addCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) String apiJsonRequestBody) {
        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.addCollateral(clientId, apiJsonRequestBody);

    }

    @PUT
    @Path("{collateralId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update New Collateral of a Client", description = "Update New Collateral of a Client")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String updateCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) String apiJsonRequestBody) {

        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.updateCollateral(clientId, collateralId, apiJsonRequestBody);
    }

    @DELETE
    @Path("{collateralId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(summary = "Delete Client Collateral", description = "Delete Client Collateral")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String deleteCollateral(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        validateAppuserClientsMapping(clientId);
        return this.clientCollateralManagementApiResource.deleteCollateral(clientId, collateralId);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
