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
package org.apache.fineract.portfolio.account.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.service.business.AccountTransfersBusinessReadPlatformService;
import org.apache.fineract.portfolio.account.service.business.AccountTransfersBusinessWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/accounttransfers/business")
@Component
@Scope("singleton")
@Tag(name = "Account Transfers", description = "Ability to be able to transfer monetary funds from one account to another.\n\nNote: At present only savings account to savings account transfers are supported.")
public class AccountTransfersBusinessApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<AccountTransferData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AccountTransfersBusinessReadPlatformService accountTransfersReadPlatformService;
    private final AccountTransfersBusinessWritePlatformService accountTransfersBusinessWritePlatformService;

    @Autowired
    public AccountTransfersBusinessApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<AccountTransferData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final AccountTransfersBusinessReadPlatformService accountTransfersReadPlatformService,
            final AccountTransfersBusinessWritePlatformService accountTransfersBusinessWritePlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.accountTransfersReadPlatformService = accountTransfersReadPlatformService;
        this.accountTransfersBusinessWritePlatformService = accountTransfersBusinessWritePlatformService;
    }

    @POST
    @Path("savings/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Account Transfer Template", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // AccountTransfersApiResourceSwagger.GetAccountTransfersTemplateResponse.class))
    ) })
    public String templateSavings(@Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {

        /*
         * @QueryParam("fromOfficeId") @Parameter(description = "fromOfficeId") final Long fromOfficeId,
         *
         * @QueryParam("fromClientId") @Parameter(description = "fromClientId") final Long fromClientId,
         *
         * @QueryParam("fromAccountId") @Parameter(description = "fromAccountId") final Long fromAccountId,
         *
         * @QueryParam("fromAccountType") @Parameter(description = "fromAccountType") final Integer fromAccountType,
         *
         * @QueryParam("toOfficeId") @Parameter(description = "toOfficeId") final Long toOfficeId,
         *
         * @QueryParam("toClientId") @Parameter(description = "toClientId") final Long toClientId,
         *
         * @QueryParam("toAccountId") @Parameter(description = "toAccountId") final Long toAccountId,
         *
         * @QueryParam("toAccountType") @Parameter(description = "toAccountType") final Integer toAccountType,
         */
        this.context.authenticatedUser().validateHasReadPermission(AccountTransfersApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);

        final AccountTransferData transferData = this.accountTransfersReadPlatformService.retrieveTemplate(apiRequestBodyAsJson);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, transferData, AccountTransfersApiConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("savings")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create new Transfer", description = "Ability to create new transfer of monetary funds from one savings account to another.")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // AccountTransfersApiResourceSwagger.PostAccountTransfersRequest.class))
    )
    @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // AccountTransfersApiResourceSwagger.PostAccountTransfersResponse.class))
    )
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandProcessingResult result = this.accountTransfersBusinessWritePlatformService.create(apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }
}
