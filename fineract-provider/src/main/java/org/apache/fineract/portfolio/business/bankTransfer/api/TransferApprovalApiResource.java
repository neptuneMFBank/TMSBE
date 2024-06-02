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
package org.apache.fineract.portfolio.business.bankTransfer.api;

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
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.bankTransfer.data.TransferApprovalData;
import org.apache.fineract.portfolio.business.bankTransfer.service.TransferApprovalReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/transfer/approval")
@Component

@Tag(name = "Transfer Approval", description = "This defines the Approvals for transfer")
public class TransferApprovalApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<TransferApprovalData> jsonSerializer;
    private final TransferApprovalReadPlatformService transferApprovalReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public TransferApprovalApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<TransferApprovalData> jsonSerializer,
            final TransferApprovalReadPlatformService transferApprovalReadPlatformService,
            ApiRequestParameterHelper apiRequestParameterHelper, PortfolioCommandSourceWritePlatformService commandWritePlatformService) {
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandWritePlatformService = commandWritePlatformService;
        this.transferApprovalReadPlatformService = transferApprovalReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Transfer Template", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.securityContext.authenticatedUser().validateHasReadPermission(TransferApprovalApiResourceConstants.RESOURCE_NAME);

        TransferApprovalData transferApprovalData = this.transferApprovalReadPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, transferApprovalData, TransferApprovalApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Transfer Approval ", description = "List Transfer Approval")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("transferType") @Parameter(description = "transferType") final Long transferType,
            @QueryParam("status") @Parameter(description = "status") final Integer status,
            @QueryParam("tobankId") @Parameter(description = "tobankId") final Long tobankId,
            @QueryParam("toAccountNumber") @Parameter(description = "toAccountNumber") final String toAccountNumber,
            @QueryParam("fromAccountNumber") @Parameter(description = "fromAccountNumber") final String fromAccountNumber,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat,
            @DefaultValue("en") @QueryParam("locale") final String locale
    ) {
        this.securityContext.authenticatedUser().validateHasReadPermission(TransferApprovalApiResourceConstants.RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate("startPeriod", dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate("endPeriod", dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forOverdraft(offset, limit, orderBy, sortOrder,
                null, fromDate, toDate, status);

        searchParameters.setTobankId(tobankId);
        searchParameters.setToAccountNumber(toAccountNumber);
        searchParameters.setTransferType(transferType);
        searchParameters.setFromAccountNumber(fromAccountNumber);
        final Page<TransferApprovalData> transferApprovalData = this.transferApprovalReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, transferApprovalData, TransferApprovalApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{transferApprovalId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a TransferApproval")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String retrieveOne(@PathParam("transferApprovalId")
            @Parameter(description = "transferApprovalId")
            final Long transferApprovalId,
            @Context final UriInfo uriInfo) {

        this.securityContext.authenticatedUser().validateHasReadPermission(TransferApprovalApiResourceConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        TransferApprovalData transferApprovalData = this.transferApprovalReadPlatformService.retrieveOne(transferApprovalId);

        return this.jsonSerializer.serialize(settings, transferApprovalData, TransferApprovalApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create a Transfer Approval", description = "Creates a Transfer Approval\n\n")
    @RequestBody(required = true)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String create(@Parameter(hidden = true)
            final String apiRequestBodyAsJson
    ) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createTransferApproval().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @POST
    @Path("{transferApprovalId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Approve a transfer  | Reject a transfer  ")
    @RequestBody(required = true)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String handleCommands(@PathParam("transferApprovalId") @Parameter(description = "transferApprovalId") final Long transferApprovalId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest = null;
        if (is(commandParam, "reject")) {
            commandRequest = builder.RejectTransfer(transferApprovalId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "approve")) {
            commandRequest = builder.AprroveTransfer(transferApprovalId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam,
                    new Object[]{"reject", "approve"});
        }

        return this.jsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}
