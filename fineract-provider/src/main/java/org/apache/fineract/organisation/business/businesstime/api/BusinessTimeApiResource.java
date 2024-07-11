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
package org.apache.fineract.organisation.business.businesstime.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.business.businesstime.data.BusinessTimeData;
import org.apache.fineract.organisation.business.businesstime.service.BusinessTimeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/businesstime")
@Component
@Tag(name = "Business Time ", description = "This defines the business time")
public class BusinessTimeApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<BusinessTimeData> jsonSerializer;
    private final BusinessTimeReadPlatformService businessTimeReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public BusinessTimeApiResource(final PlatformSecurityContext securityContext,
            final DefaultToApiJsonSerializer<BusinessTimeData> jsonSerializer,
            final BusinessTimeReadPlatformService businessTimeReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.businessTimeReadPlatformService = businessTimeReadPlatformService;
        this.commandWritePlatformService = commandWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Business Time Template", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.securityContext.authenticatedUser().validateHasReadPermission(BusinessTimeApiResourceConstants.RESOURCE_NAME);

        BusinessTimeData businessTimeData = this.businessTimeReadPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, businessTimeData, BusinessTimeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("role/{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Business Time", description = "List Business Time ")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAll(@Context final UriInfo uriInfo, @PathParam("roleId") @Parameter(description = "roleId") final Long roleId) {
        this.securityContext.authenticatedUser().validateHasReadPermission(BusinessTimeApiResourceConstants.RESOURCE_NAME);

        final Collection<BusinessTimeData> businessTimeData = this.businessTimeReadPlatformService.retrieveAll(roleId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, businessTimeData, BusinessTimeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{businessTimeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a business time")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveOne(@PathParam("businessTimeId") @Parameter(description = "businessTimeId") final Long businessTimeId,
            @Context final UriInfo uriInfo) {

        this.securityContext.authenticatedUser().validateHasReadPermission(BusinessTimeApiResourceConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        BusinessTimeData businessTimeData = this.businessTimeReadPlatformService.retrieveOne(businessTimeId);

        return this.jsonSerializer.serialize(settings, businessTimeData, BusinessTimeApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a business time", description = "Creates a business time")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createBusinessTime().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{businessTimeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a business time", description = "Updates a business time")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String update(@PathParam("businessTimeId") @Parameter(description = "businessTimeId") final Long businessTimeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateBusinessTime(businessTimeId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);

    }

    @DELETE
    @Path("role/{roleId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a a business time by role", description = "Deletes  a business time by role")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String deleteByRole(@PathParam("roleId") @Parameter(description = "roleId") final Long roleId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteBusinessTimeByRole(roleId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{businessTimeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a a business time", description = "Deletes  a business time")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String delete(@PathParam("businessTimeId") @Parameter(description = "businessTimeId") final Long businessTimeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteBusinessTime(businessTimeId).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);

    }
}
