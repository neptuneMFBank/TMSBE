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
package org.apache.fineract.portfolio.savings.api.business;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformServiceImpl;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.service.business.SavingsAccountBusinessReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/savingsaccounts/{savingsId}/transactions/business")
@Component
@Scope("singleton")
@Tag(name = "Savings Account Transaction", description = "")
public class SavingsAccountTransactionsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final SavingsAccountBusinessReadPlatformService savingsAccountBusinessReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final ClientReadPlatformService clientReadPlatformService;
    private final AddressReadPlatformServiceImpl readPlatformService;
    private final DefaultToApiJsonSerializer toApiDocJsonSerializer;
    private final SavingsAccountsApiResource savingsAccountsApiResource;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public SavingsAccountTransactionsBusinessApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsAccountTransactionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final SavingsAccountBusinessReadPlatformService savingsAccountBusinessReadPlatformService, final FromJsonHelper fromJsonHelper,
            final ClientReadPlatformService clientReadPlatformService, final AddressReadPlatformServiceImpl readPlatformService,
            final SavingsAccountsApiResource savingsAccountsApiResource, final DefaultToApiJsonSerializer toApiDocJsonSerializer, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.savingsAccountBusinessReadPlatformService = savingsAccountBusinessReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
        this.clientReadPlatformService = clientReadPlatformService;
        this.readPlatformService = readPlatformService;
        this.toApiDocJsonSerializer = toApiDocJsonSerializer;
        this.savingsAccountsApiResource = savingsAccountsApiResource;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAllBySavingsId(@PathParam("savingsId") final Long savingsId, @Context final UriInfo uriInfo,
            @QueryParam("startPeriod") @Parameter(description = "fromDate") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "toDate") final DateParam endPeriod,
            @QueryParam("transactionTypeId") @Parameter(description = "transactionTypeId") final Long transactionTypeId,
            @QueryParam("depositAccountTypeId") @Parameter(description = "depositAccountTypeId") Integer depositAccountTypeId,
            @QueryParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("tr.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("DESC") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(SavingsBusinessApiSetConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(SavingsBusinessApiSetConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forTransactions(transactionTypeId, transactionId, offset,
                limit, orderBy, sortOrder, fromDate, toDate, null);
        if (depositAccountTypeId == null) {
            depositAccountTypeId = DepositAccountType.SAVINGS_DEPOSIT.getValue();
        }
        final DepositAccountType depositAccountType = DepositAccountType.fromInt(depositAccountTypeId);
        final Page<SavingsAccountTransactionData> transactionData = this.savingsAccountBusinessReadPlatformService
                .retrieveAllTransactionsBySavingsId(savingsId, depositAccountType, searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, transactionData,
                SavingsBusinessApiSetConstants.SAVINGS_TRANSACTION_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/doc")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a savings Doc", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = LoansApiResourceSwagger.GetLoansLoanIdResponse.class))
        )})
    public String retrieveSavingsDoc(@PathParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            @Context final UriInfo uriInfo, @QueryParam("startPeriod") @Parameter(description = "fromDate") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "toDate") final DateParam endPeriod,
            @QueryParam("transactionTypeId") @Parameter(description = "transactionTypeId") final Long transactionTypeId,
            @QueryParam("depositAccountTypeId") @Parameter(description = "depositAccountTypeId") Integer depositAccountTypeId,
            @QueryParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_ACCOUNT_RESOURCE_NAME);

        final JsonObject jsonObject = new JsonObject();

        final String savings = this.savingsAccountsApiResource.retrieveOne(savingsId, true, "all", uriInfo);
        if (StringUtils.isNotBlank(savings)) {
            final JsonElement retrieveSavingsElement = this.fromJsonHelper.parse(savings);
            jsonObject.add("savings", retrieveSavingsElement);

            final String retrieveSavingsTransactions = this.retrieveAllBySavingsId(savingsId, uriInfo, startPeriod, endPeriod,
                    transactionTypeId, depositAccountTypeId, transactionId, offset, limit, orderBy, sortOrder, locale, dateFormat);
            if (StringUtils.isNotBlank(retrieveSavingsTransactions)) {
                final JsonElement retrieveSavingsTransactionsElement = this.fromJsonHelper.parse(retrieveSavingsTransactions);
                jsonObject.add("savingsTransactions", retrieveSavingsTransactionsElement);
            }

            final Long clientId = this.fromJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, retrieveSavingsElement);
            final ClientData clientData = this.clientReadPlatformService.retrieveOne(clientId);
            final String clientDataInfo = this.toApiJsonSerializer.serialize(clientData);
            final JsonElement clientInfo = this.fromJsonHelper.parse(clientDataInfo);
            jsonObject.add("clientInfo", clientInfo);

            final Integer homeAddress = 15;
            final Collection<AddressData> addressDatas = this.readPlatformService.retrieveAddressbyTypeAndStatus(clientId, homeAddress,
                    "true");
            final AddressData clientAddressData = addressDatas.stream().findFirst().orElse(null);
            final String clientAddressDataInfo = this.toApiJsonSerializer.serialize(clientAddressData);
            final JsonElement clientAddressInfo = this.fromJsonHelper.parse(clientAddressDataInfo);
            jsonObject.add("clientAddressData", clientAddressInfo);

            final Integer officeAddress = 16;
            final Collection<AddressData> officeAddressDatas = this.readPlatformService.retrieveAddressbyTypeAndStatus(clientId,
                    officeAddress, "true");
            final AddressData clientOfficeAddressData = officeAddressDatas.stream().findFirst().orElse(null);
            final String clientOfficeAddressDataInfo = this.toApiJsonSerializer.serialize(clientOfficeAddressData);
            final JsonElement clientOfficeAddressInfo = this.fromJsonHelper.parse(clientOfficeAddressDataInfo);
            jsonObject.add("clientOfficeAddressInfo", clientOfficeAddressInfo);
        }
        return this.toApiDocJsonSerializer.serialize(jsonObject);
    }

    @POST
    @Path("{transactionId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Undo Bulk transaction API", description = "Undo Bulk transaction API\n\n Accepted command = undo, reverse, modify, releaseAmount")
    @RequestBody(required = true
    //, content = @Content(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(array = @ArraySchema(schema = @Schema(implementation = SavingsAccountTransactionsApiResourceSwagger.PostSavingsAccountBulkReversalTransactionsRequest.class)))
        )})
    public String adjustTransaction(@PathParam("savingsId") final Long savingsId, @PathParam("transactionId") final Long transactionId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {

        String jsonApiRequest = apiRequestBodyAsJson;
        if (StringUtils.isBlank(jsonApiRequest)) {
            jsonApiRequest = "{}";
        }

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonApiRequest);

        CommandProcessingResult result = null;
        if (is(commandParam, SavingsBusinessApiSetConstants.COMMAND_UNDO_BULK_TRANSACTION)) {
            final CommandWrapper commandRequest = builder.undoBulkSavingsAccountTransaction(savingsId, transactionId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            //
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[]{SavingsBusinessApiSetConstants.COMMAND_UNDO_BULK_TRANSACTION});
        }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }
}
