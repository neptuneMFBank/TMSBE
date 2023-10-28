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
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductPaymentTypeConfigData;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductPaymentTypeConfigReadPlatformService;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/loanproducts/payment")
@Component
@RequiredArgsConstructor
@Tag(name = "Loan Product Payment", description = "Loan Product Payment")
public class LoanProductPaymentTypeConfigApiResource {

    private final PlatformSecurityContext securityContext;
    private final ToApiJsonSerializer<LoanProductPaymentTypeConfigData> jsonSerializer;
    private final LoanProductPaymentTypeConfigReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Loan Product Payment Template", description = """
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsTemplateResponse.class))
        )})
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(LoanProductPaymentTypeConfigConstants.RESOURCENAME);

        final LoanProductPaymentTypeConfigData loanProductPaymentTypeConfigData = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductPaymentTypeConfigData,
                LoanProductPaymentTypeConfigConstants.RESPONSE_TEMPLATE_PARAMETERS);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create Loan Product Payment", description = """
            Creates a new Loan Product Payment""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostLoanProductPaymentTypeConfigRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostLoanProductPaymentTypeConfigResponse.class))
        )})
    public String createLoanProductPaymentTypeConfig(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createLoanProductPaymentTypeConfig()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{loanProductPaymentId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update Loan Product Payment", description = "Update Loan Product Payment")
    @RequestBody(required = true
    // ,content = @Content(schema = @Schema(implementation =
    // EmployerApiResourceSwagger.PutLoanProductPaymentTypeConfigEmployerIdRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // EmployerApiResourceSwagger.PutLoanProductPaymentTypeConfigEmployerIdResponse.class))
        )})
    public String updateLoanProductPaymentTypeConfig(
            @PathParam("loanProductPaymentId") @Parameter(description = "loanProductPaymentId") final Long loanProductPaymentId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanProductPaymentTypeConfig(loanProductPaymentId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all LoanProductPaymentTypeConfig", description = "Retrieve list of LoanProductPaymentTypeConfig")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(array = @ArraySchema(schema = @Schema(implementation =
        // EmployerApiResourceSwagger.GetLoanProductPaymentTypeConfigResponse.class)))
        )})
    public String getAllLoanProductPaymentTypeConfig(@Context final UriInfo uriInfo,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("name") @Parameter(description = "name") final String name,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("lpa.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductPaymentTypeConfigConstants.RESOURCENAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forLoanProductApproval(offset, limit, orderBy, sortOrder,
                productId, fromDate, toDate, name);

        final Page<LoanProductPaymentTypeConfigData> loanProductPaymentDatas = this.readPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductPaymentDatas,
                LoanProductPaymentTypeConfigConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{id}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a loan Product Payment", description = "Retrieves a loan Product Payment")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String retrieveOne(
            @PathParam("id") @Parameter(description = "id") final Long id,
            @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(LoanProductPaymentTypeConfigConstants.RESOURCENAME);
        final LoanProductPaymentTypeConfigData loanProductPaymentData = this.readPlatformService.retrieveOne(id);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, loanProductPaymentData,
                LoanProductPaymentTypeConfigConstants.RESPONSE_DATA_PARAMETERS);
    }

}
