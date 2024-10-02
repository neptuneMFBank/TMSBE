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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("/savingsproducts/{productId}/productmix/business")
@Component
@Scope("singleton")
@Tag(name = "Savings Product Mix", description = "")
public class SavingsProductMixApiResource {

    private final String resourceNameForPermissions = "SAVINGSPRODUCTMIX";

    private final Set<String> productMixDataParameters = new HashSet<>(
            Arrays.asList("productId", "productName", "restrictedProducts", "allowedProducts", "productOptions"));

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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        SavingsProductMixData productMixData = this.savingsProductMixReadPlatformService.retrieveSavingsProductMixDetails(productId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (settings.isTemplate()) {
            final Collection<SavingsProductData> productOptions = this.savingsProductBusinessReadPlatformService.retrieveAvailableSavingsProductsForMix();
            productMixData = SavingsProductMixData.withTemplateOptions(productMixData, productOptions);
        }
        return this.toApiJsonSerializer.serialize(settings, productMixData, this.productMixDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createProductMix(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingsProductMix(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateProductMix(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingsProductMix(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteProductMix(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingsProductMix(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
