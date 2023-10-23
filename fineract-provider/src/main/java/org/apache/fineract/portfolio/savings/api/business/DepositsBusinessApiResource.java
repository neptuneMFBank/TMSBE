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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.savings.api.FixedDepositAccountsApiResource;
import org.apache.fineract.portfolio.savings.api.RecurringDepositAccountsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.portfolio.savings.business.DepositsBusinessApiConstants;
import static org.apache.fineract.portfolio.savings.business.DepositsBusinessApiConstants.DEPOSIT_RESPONSE_DATA_PARAMETERS;
import org.apache.fineract.portfolio.savings.data.business.DepositAccountBusinessData;
import org.apache.fineract.portfolio.savings.service.business.DepositsBusinessReadPlatformService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/deposits")
@Component
@Scope("singleton")
@Tag(name = "Deposits", description = """
        View of all saving and investment deposit types""")
@RequiredArgsConstructor
public class DepositsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<DepositAccountBusinessData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DepositsBusinessReadPlatformService depositsBusinessReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final SavingsAccountsApiResource savingsAccountsApiResource;
    private final FixedDepositAccountsApiResource fixedDepositAccountsApiResource;
    private final RecurringDepositAccountsApiResource recurringDepositAccountsApiResource;
    private final FromJsonHelper fromApiJsonHelper;

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Deposits", description = """
            The list capability of deposits can support pagination and sorting.
""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
        )})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("accountWithBalance") @Parameter(description = "accountWithBalance") final Boolean accountWithBalance,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("depositTypeId") @Parameter(description = "depositTypeId") final Integer depositTypeId,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue(" ms.id ") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue(" desc ") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forDeposit(offset, limit, orderBy, sortOrder,
                productId, fromDate, toDate, depositTypeId, accountNo, officeId, statusId, externalId, clientId, displayName, accountWithBalance);

        final Page<DepositAccountBusinessData> clientData = this.depositsBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNo}/enquiry")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve account enquiry", description = """
            Example Requests:""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
        )})
    public String retrieveName(@PathParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        DepositAccountBusinessData depositAccountBusinessData = this.depositsBusinessReadPlatformService.retrieveName(accountNo);

        return this.toApiJsonSerializer.serialize(settings, depositAccountBusinessData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNo}/balance")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve account enquiry", description = """
            Example Requests:""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
        )})
    public String retrieveBalance(@PathParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        DepositAccountBusinessData depositAccountBusinessData = this.depositsBusinessReadPlatformService.retrieveBalance(accountNo);

        return this.toApiJsonSerializer.serialize(settings, depositAccountBusinessData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Submit new savings application", description = """
                                                                         Submits new savings application
                                                                         
                                                                         """)
    @RequestBody(required = true
    //, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //                , content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsResponse.class))
        )})
    public String submitApplication(@Context final UriInfo uriInfo, @QueryParam("command") final String commandParam, @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        log.info("submitApplication-commandParam {}: {}", commandParam, apiRequestBodyAsJson);
        CommandWrapper commandRequest;
        CommandProcessingResult result = null;
        String templateJson;
        if (is(commandParam, "savings")) {
            templateJson = DepositsBusinessApiTemplate.savingsTemplateConfig(this.savingsAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("commandParam {}: ", templateJson);
            commandRequest = new CommandWrapperBuilder().createSavingsAccount().withJson(templateJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "fixed")) {
            templateJson = DepositsBusinessApiTemplate.fixedTemplateConfig(this.fixedDepositAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("commandParam {}: ", templateJson);
            commandRequest = new CommandWrapperBuilder().createFixedDepositAccount().withJson(templateJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "recurring")) {
            templateJson = DepositsBusinessApiTemplate.recurringTemplateConfig(this.recurringDepositAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("commandParam {}: ", templateJson);
            commandRequest = new CommandWrapperBuilder().createRecurringDepositAccount().withJson(templateJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[]{"savings", "fixed", "approve", "recurring"});
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update savings application", description = """
                                                                         Update savings application
                                                                         
                                                                         """)
    @RequestBody(required = true
    //, content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //                , content = @Content(schema = @Schema(implementation = SavingsAccountsApiResourceSwagger.PostSavingsAccountsResponse.class))
        )})
    public String updateApplication(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId, @Context final UriInfo uriInfo, @QueryParam("command") final String commandParam, @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        log.info("updateApplication-commandParam {}: {}", commandParam, apiRequestBodyAsJson);
        CommandWrapper commandRequest;
        CommandProcessingResult result = null;
        String templateJson;
        if (is(commandParam, "savings") || is(commandParam, "updateWithHoldTax")) {
            templateJson = DepositsBusinessApiTemplate.savingsTemplateConfig(this.savingsAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("updateApplication commandParam {}: ", templateJson);

            if (is(commandParam, "updateWithHoldTax")) {
                commandRequest = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson).updateWithHoldTax(accountId)
                        .build();
            } else {
                commandRequest = new CommandWrapperBuilder().updateSavingsAccount(accountId).withJson(apiRequestBodyAsJson)
                        .build();
            }
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "fixed")) {
            templateJson = DepositsBusinessApiTemplate.fixedTemplateConfig(this.fixedDepositAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("updateApplication commandParam {}: ", templateJson);
            commandRequest = new CommandWrapperBuilder().updateFixedDepositAccount(accountId)
                    .withJson(templateJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "recurring")) {
            templateJson = DepositsBusinessApiTemplate.recurringTemplateConfig(this.recurringDepositAccountsApiResource, apiRequestBodyAsJson, this.fromApiJsonHelper, true, uriInfo, null);
            log.info("updateApplication commandParam {}: ", templateJson);
            commandRequest = new CommandWrapperBuilder().updateRecurringDepositAccount(accountId)
                    .withJson(templateJson).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[]{"savings", "updateWithHoldTax", "fixed", "approve", "recurring"});
        }
        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
