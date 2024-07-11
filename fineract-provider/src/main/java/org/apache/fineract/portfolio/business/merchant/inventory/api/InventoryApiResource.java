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
package org.apache.fineract.portfolio.business.merchant.inventory.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.merchant.inventory.data.InventoryData;
import org.apache.fineract.portfolio.business.merchant.inventory.data.InventoryValidator;
import org.apache.fineract.portfolio.business.merchant.inventory.service.InventoryReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/merchant/inventory")
@Component
@Scope("singleton")
public class InventoryApiResource {

    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<InventoryData> toApiJsonSerializer;
    private final InventoryReadPlatformService inventoryReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public InventoryApiResource(final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<InventoryData> toApiJsonSerializer,
            final InventoryReadPlatformService inventoryReadPlatformService, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.platformSecurityContext = platformSecurityContext;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.inventoryReadPlatformService = inventoryReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an inventory", description = "Mandatory Fields\n"
            + "name, Skicode, price, description, discountPercentage")
    @RequestBody(required = true, content = @Content())
    @ApiResponse(responseCode = "200", description = "OK", content = @Content())
    public String createInventory(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createInventory().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{inventoryId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Inventory")
    @ApiResponse(responseCode = "200", description = "OK")
    public String retrieveInventory(@PathParam("inventoryId") final Long inventoryId, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(InventoryValidator.RESOURCE_NAME);
        InventoryData inventoryData = this.inventoryReadPlatformService.retrieveOne(inventoryId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, inventoryData);

    }

    @GET
    @Path("link/{link}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Inventory by link")
    @ApiResponse(responseCode = "200", description = "OK")
    public String retrieveInventoryByLink(@PathParam("link") final String link, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(InventoryValidator.RESOURCE_NAME);
        InventoryData inventoryData = this.inventoryReadPlatformService.retrieveOneByLink(link);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, inventoryData);

    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List inventories")
    @ApiResponse(responseCode = "200", description = "OK")
    public String retrieveAllInventories(@QueryParam("sqlSearch") final String sqlSearch, @QueryParam("offset") final Integer offset,
            @QueryParam("limit") final Integer limit, @QueryParam("orderBy") final String orderBy,
            @QueryParam("sortOrder") final String sortOrder, @QueryParam("clientId") final Long clientId,
            @QueryParam("name") final String name, @Context final UriInfo uriInfo) {
        this.platformSecurityContext.authenticatedUser().validateHasReadPermission(InventoryValidator.RESOURCE_NAME);
        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forInventory(offset, name, clientId, limit, orderBy,
                sortOrder);
        Page<InventoryData> inventoryDataCollection = this.inventoryReadPlatformService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, inventoryDataCollection);
    }

    @PUT
    @Path("{inventoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an inventory")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CommandWrapper.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommandProcessingResult.class)))
    public String updateInventory(@PathParam("inventoryId") final Long inventoryId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInventory(inventoryId).withJson(apiRequestBodyAsJson)
                .build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{inventoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete an inventory")
    @ApiResponse(responseCode = "200", description = "OK")
    public String delete(@PathParam("inventoryId") final Long inventoryId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInventory(inventoryId).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

}
