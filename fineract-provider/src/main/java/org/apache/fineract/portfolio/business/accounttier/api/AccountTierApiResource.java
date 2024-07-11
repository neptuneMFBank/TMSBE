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
package org.apache.fineract.portfolio.business.accounttier.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Collection;
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
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.accounttier.data.AccountTierData;
import org.apache.fineract.portfolio.business.accounttier.data.CumulativeTransactionsData;
import org.apache.fineract.portfolio.business.accounttier.service.AccountTierReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("accounttier")
@Component
@Scope
public class AccountTierApiResource {

    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<AccountTierData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<CumulativeTransactionsData> apiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AccountTierReadPlatformService accountTierReadPlatformService;

    @Autowired
    public AccountTierApiResource(final PlatformSecurityContext context,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<AccountTierData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final AccountTierReadPlatformService accountTierReadPlatformService,
            final DefaultToApiJsonSerializer<CumulativeTransactionsData> apiJsonSerializer) {
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.accountTierReadPlatformService = accountTierReadPlatformService;
        this.apiJsonSerializer = apiJsonSerializer;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an account tier")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountTier().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List account tiers", description = "Lists account tiers")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("clientTypeId") @Parameter(description = "clientTypeId") final Long clientTypeId,
            @QueryParam("name") @Parameter(description = "name") final String name) {

        this.context.authenticatedUser().validateHasReadPermission(AccountTierApiResouceConstants.RESOURCE_NAME);

        final Collection<AccountTierData> accountTiers = this.accountTierReadPlatformService.retrieveAll(clientTypeId, name);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, accountTiers, AccountTierApiResouceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountTierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an account tier", description = "Retrieves an account tier")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveOne(@PathParam("accountTierId") @Parameter(description = "accountTierId") final Long accountTierId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountTierApiResouceConstants.RESOURCE_NAME);

        AccountTierData accountTierData = this.accountTierReadPlatformService.retrieveOne(accountTierId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, accountTierData, AccountTierApiResouceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{accountTierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an account tier", description = "Updates an account tier")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String update(@PathParam("accountTierId") @Parameter(description = "accountTierId") final Long accountTierId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAccountTier(accountTierId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @DELETE
    @Path("{accountTierId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete an account tier", description = "Deletes an account tier")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String delete(@PathParam("accountTierId") @Parameter(description = "accountTierId") final Long accountTierId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteAccountTier(accountTierId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Path("cumulativetransaction/{savingsId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve cumulative transaction for a savings", description = "Retrieve cumulative transaction for a savings")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveClientCumulativeTransaction(@PathParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            @QueryParam("channelId") @Parameter(description = "channelId") final Long channelId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountTierApiResouceConstants.RESOURCE_NAME);

        CumulativeTransactionsData cumulativeTransactionsData = this.accountTierReadPlatformService
                .retrieveCumulativeTransactionsAmount(savingsId, channelId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.apiJsonSerializer.serialize(settings, cumulativeTransactionsData);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve accounnt tier  template")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(AccountTierApiResouceConstants.RESOURCE_NAME);

        AccountTierData accountTierData = this.accountTierReadPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, accountTierData, AccountTierApiResouceConstants.RESPONSE_DATA_PARAMETERS);
    }

}
