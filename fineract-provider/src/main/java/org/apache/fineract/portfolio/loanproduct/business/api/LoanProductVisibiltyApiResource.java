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
package org.apache.fineract.portfolio.loanproduct.business.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductVisibilityConfigData;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductVisibilityReadPlatformService;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/loanproducts/visibility")
@Component
@RequiredArgsConstructor
@Tag(name = "Loan Product Visibility", description = "Loan Product visibility")
public class LoanProductVisibiltyApiResource {

    private final PlatformSecurityContext securityContext;
    private final ToApiJsonSerializer<LoanProductVisibilityConfigData> jsonSerializer;
    private final LoanProductVisibilityReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create Loan Product visibility", description = "Creates a new Loan Product Approval")
    @RequestBody(required = true)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String createLoanProductApproval(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createLoanProductVisibility()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Loan Product Visiblity Config", description = "Retrieve all Loan Product Visiblity Config")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String getAllLoanProductApproval(@Context final UriInfo uriInfo,
            @QueryParam("name") @Parameter(description = "name") final String name,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductVisibilityApiResourceConstants.RESOURCENAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoanProductApproval(offset, limit, orderBy, sortOrder,
                null, fromDate, toDate, name);

        final Page<LoanProductVisibilityConfigData> loanProductVisibilityConfigData = this.readPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductVisibilityConfigData,
                LoanProductApprovalApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{loanProductVisibilityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a loan Product Approval", description = "Retrieves a loan Product Approval")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String retrieveOneLoanProductApproval(
            @PathParam("loanProductVisibilityId") @Parameter(description = "loanProductApprovalId") final Long loanProductVisibilityId,
            @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductVisibilityApiResourceConstants.RESOURCENAME);
        final LoanProductVisibilityConfigData loanProductVisibilityConfigData = this.readPlatformService.retrieveOne(loanProductVisibilityId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductVisibilityConfigData,
                LoanProductApprovalApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{loanProductVisibilityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update Loan Product visibility Config", description = "Update Loan Product visibility Config")
    @RequestBody(required = true
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String updateLoanProductApproval(
            @PathParam("loanProductVisibilityId") @Parameter(description = "loanProductVisibilityId") final Long loanProductVisibilityId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductVisibilityApiResourceConstants.RESOURCENAME);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanProductVisibility(loanProductVisibilityId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{loanProductVisibilityId}")
    @Operation(summary = "Delete Loan Product visibility Config", description = "Delete Loan Product visibility Config")
    @ApiResponse(responseCode = "200", description = "OK")
    public String delete(@PathParam("loanProductVisibilityId") final Long loanProductVisibilityId) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductVisibilityApiResourceConstants.RESOURCENAME);

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteLoanProductVisibility(loanProductVisibilityId).build();
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Loan Product Visibility Template", description = "Retrieve Loan Product Visibility Template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductVisibilityApiResourceConstants.RESOURCENAME);

        final LoanProductVisibilityConfigData loanProductVisibilityConfigData = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductVisibilityConfigData,
                LoanProductVisibilityApiResourceConstants.RESPONSE_TEMPLATE_PARAMETERS);
    }
}
