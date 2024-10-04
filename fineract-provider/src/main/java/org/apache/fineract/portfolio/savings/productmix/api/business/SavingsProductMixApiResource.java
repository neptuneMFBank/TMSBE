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
package org.apache.fineract.portfolio.savings.productmix.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.productmix.data.business.SavingsProductMixData;
import org.apache.fineract.portfolio.savings.productmix.service.business.SavingsProductMixReadPlatformService;
import org.apache.fineract.portfolio.savings.service.business.SavingsProductBusinessReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("/savingsproducts/productmix")
@Component
@Scope("singleton")
@Tag(name = "Savings Product Mix", description = "")
public class SavingsProductMixApiResource {

    private final String resourceNameForPermissions = "SAVINGSPRODUCTMIX";

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<SavingsProductMixData> toApiJsonSerializer;

    private final SavingsProductMixReadPlatformService savingsProductMixReadPlatformService;
    private final SavingsProductBusinessReadPlatformService savingsProductBusinessReadPlatformService;

    @Autowired
    public SavingsProductMixApiResource(final PlatformSecurityContext context,
                                        final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
                                        final ApiRequestParameterHelper apiRequestParameterHelper, final DefaultToApiJsonSerializer<SavingsProductMixData> toApiJsonSerializer,
                                        final SavingsProductMixReadPlatformService savingsProductMixReadPlatformService,
                                        final SavingsProductBusinessReadPlatformService savingsProductBusinessReadPlatformService) {
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.savingsProductMixReadPlatformService = savingsProductMixReadPlatformService;
        this.savingsProductBusinessReadPlatformService = savingsProductBusinessReadPlatformService;
    }

    @GET
    @Path("/{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        SavingsProductMixData productMixData = this.savingsProductMixReadPlatformService.retrieveSavingsProductMixDetails(productId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        final Collection<SavingsProductData> productOptions = this.savingsProductBusinessReadPlatformService.retrieveAvailableSavingsProductsForMix();
        productMixData = SavingsProductMixData.withTemplateOptions(productMixData, productOptions);
        return this.toApiJsonSerializer.serialize(settings, productMixData, SavingsProductMixApiConstants.PRODUCTMIX_DATA_PARAMETERS);
    }

    @POST
    @Path("/{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createProductMix(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsProductMix(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("/{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateProductMix(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingsProductMix(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("/{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteProductMix(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsProductMix(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Savings Products Mixes", description = "Lists Savings Products Mixes\n\n" + "Example Requests:\n" + "\n"
            + "savingsproducts\n" + "\n" + "savingsproducts?fields=name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final Collection<SavingsProductMixData> productMixes = this.savingsProductMixReadPlatformService.retrieveAllSavingsProductMixes();
        return this.toApiJsonSerializer.serialize(settings, productMixes, SavingsProductMixApiConstants.PRODUCTMIX_LIST_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Savings Product Mix Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "Account Mapping:\n" + "\n"
            + "savingsproducts/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final Collection<SavingsProductData> productOptions = this.savingsProductBusinessReadPlatformService.retrieveAvailableSavingsProductsForMix();
        final SavingsProductMixData productMixData = SavingsProductMixData.template(productOptions);
        return this.toApiJsonSerializer.serialize(settings, productMixData, SavingsProductMixApiConstants.PRODUCTMIX_DATA_PARAMETERS);
    }
}
