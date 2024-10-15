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
package org.apache.fineract.portfolio.business.kyc.api;

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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigApiConstants;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigData;
import org.apache.fineract.portfolio.business.kyc.service.KycConfigReadService;
import org.springframework.stereotype.Component;

@Path("/kycconfig")
@Component
@Tag(name = "Kyc Configuration")
@RequiredArgsConstructor
public class KycConfigApiResource {

    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<KycConfigData> toApiJsonSerializer;
    private final PlatformSecurityContext context;
    private final KycConfigReadService kycConfigReadService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Kyc  Configuration")
    @RequestBody(required = true, content = @Content())
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createKycConfig().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{kycConfigId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a KYC Configuration", description = "Updates a KYC Configuration")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content()) })
    public String update(@PathParam("kycConfigId") @Parameter(description = "kycConfigId") final Long kycConfigId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateKycConfig(kycConfigId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List KYC Configurations", description = "Lists KYC Configurations")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema()))) })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);

        final Page<KycConfigData> kycConfigs = this.kycConfigReadService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, kycConfigs, KycConfigApiConstants.KYC_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("params")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List KYC Configurations", description = "Lists KYC Configurations")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema()))) })
    public String retrieveAllWithParams(@Context final UriInfo uriInfo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);

        final SearchParameters searchParameters = SearchParameters.forPagination(offset, limit, orderBy, sortOrder);

        final Page<KycConfigData> kycConfigs = this.kycConfigReadService.retrieveAll(searchParameters);

        for (KycConfigData kycConfigData : kycConfigs.getPageItems()) {
            final KycConfigData kycConfigDataWithParams = this.kycConfigReadService.retrieveOne(kycConfigData.getId());
            kycConfigData.setKycParams(kycConfigDataWithParams.getKycParams());
        }
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, kycConfigs, KycConfigApiConstants.KYC_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{kycConfigId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a KYC Configuration", description = "Retrieve a KYC Configuration")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema())) })
    public String retrieveOne(@PathParam("kycConfigId") @Parameter(description = "kycConfigId") final Long kycConfigId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);
        final KycConfigData kycConfigData = this.kycConfigReadService.retrieveOne(kycConfigId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, kycConfigData, KycConfigApiConstants.KYC_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Kyc Config Template", description = "Retrieve Kyc Config Template")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema())) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);

        final KycConfigData kycConfigData = this.kycConfigReadService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, kycConfigData, KycConfigApiConstants.KYC_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @DELETE
    @Path("{kycConfigId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a KYC Configuration", description = "Deletes a KYC Configuration")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK", content = @Content()) })
    public String delete(@PathParam("kycConfigId") @Parameter(description = "kycConfigId") final Long kycConfigId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteKycConfig(kycConfigId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}
