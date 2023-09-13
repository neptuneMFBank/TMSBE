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
package org.apache.fineract.portfolio.products.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import lombok.RequiredArgsConstructor;
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
import org.apache.fineract.portfolio.products.data.business.DocumentProductConfigData;
import org.apache.fineract.portfolio.products.service.business.DocumentProductConfigReadPlatformService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("documents/config/product")
@Component
@Scope("singleton")
@Tag(name = "Entity Document Product Config", description = """
        Create an enity product document configuration.""")
@RequiredArgsConstructor
public class DocumentProductConfigApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<DocumentProductConfigData> toBusinessApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DocumentProductConfigReadPlatformService documentProductConfigReadPlatformService;

    // private boolean is(final String commandParam, final String commandValue) {
    // return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    // }
    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Document Product Config Template", description = """
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsTemplateResponse.class))
    ) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DocumentProductConfigApiConstants.resourceName);

        final DocumentProductConfigData documentProductConfigData = this.documentProductConfigReadPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, documentProductConfigData,
                DocumentProductConfigApiConstants.DOCUMENT_PRODUCT_CONFIG_TEMPLATE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an array Product Document Config", description = """
                Note:
            """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsResponse.class))
    ) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        // client
        // loans
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createProductDocumentConfig()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toBusinessApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{entityId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Delete a Document Config", description = """
            Note:
                                                                   """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.PutClientsClientIdRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.PutClientsClientIdResponse.class))
    ) })
    public String delete(@Parameter(description = "entityId") @PathParam("entityId") final Long entityId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteProductDocumentConfig(entityId) //
                .withNoJsonBody().build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toBusinessApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Clients", description = """
            Note:
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
    ) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("documentConfigId") @Parameter(description = "documentConfigId") final Long documentConfigId,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("active") @Parameter(description = "active") final Boolean active,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("showLoanProducts") @Parameter(description = "showLoanProducts") final Boolean showLoanProducts,
            @QueryParam("showSavingsProducts") @Parameter(description = "showSavingsProducts") final Boolean showSavingsProducts,
            // @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            // @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.context.authenticatedUser().validateHasReadPermission(DocumentProductConfigApiConstants.resourceName);

        // LocalDate fromDate = null;
        // if (startPeriod != null) {
        // fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        // }
        // LocalDate toDate = null;
        // if (endPeriod != null) {
        // toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        // }
        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forDocumentProductConfig(documentConfigId, offset, limit,
                orderBy, sortOrder, showLoanProducts, showSavingsProducts);

        final Page<DocumentProductConfigData> documentConfigData;
        documentConfigData = this.documentProductConfigReadPlatformService.retrieveAll(searchParameters);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentProductConfigApiConstants.DOCUMENT_PRODUCT_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{loanProductId}/loan-product")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan ProductDocument Config", description = """
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
    ) })
    public String retrieveLoanProductDocument(
            @PathParam("loanProductId") @Parameter(description = "loanProductId") final Long loanProductId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DocumentProductConfigApiConstants.resourceName);

        final DocumentProductConfigData documentConfigData = this.documentProductConfigReadPlatformService
                .retrieveLoanProductDocument(loanProductId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentProductConfigApiConstants.DOCUMENT_PRODUCT_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{savingsProductId}/savings-product")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan ProductDocument Config", description = """
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
    ) })
    public String retrieveSavingProductDocument(
            @PathParam("savingsProductId") @Parameter(description = "savingsProductId") final Long savingsProductId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DocumentProductConfigApiConstants.resourceName);

        final DocumentProductConfigData documentConfigData = this.documentProductConfigReadPlatformService
                .retrieveSavingProductDocument(savingsProductId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentProductConfigApiConstants.DOCUMENT_PRODUCT_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

}
