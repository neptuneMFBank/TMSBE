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
package org.apache.fineract.infrastructure.documentmanagement.api.business;

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
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentConfigReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("documents/config")
@Component
@Scope("singleton")
@Tag(name = "Entity Document Config", description = """
        Create an enity document configuration.""")
@RequiredArgsConstructor
public class DocumentConfigApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<DocumentConfigData> toBusinessApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DocumentConfigReadPlatformService documentConfigReadPlatformService;

//    private boolean is(final String commandParam, final String commandValue) {
//        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
//    }
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create a Document Config", description = """
                Note:
            """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsResponse.class))
        )})
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        // client
        // loans
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createDocumentConfig()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toBusinessApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{entityId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update a Document Config", description = """
            Note:
                                                                   """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.PutClientsClientIdRequest.class))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.PutClientsClientIdResponse.class))
        )})
    public String update(@Parameter(description = "entityId") @PathParam("entityId") final Long entityId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateDocumentConfig(entityId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toBusinessApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Clients", description = """
            Note:
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
        )})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("type") @Parameter(description = "type") final Integer type,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("active") @Parameter(description = "active") final Boolean active,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.context.authenticatedUser().validateHasReadPermission(DocumentConfigApiConstants.resourceName);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forDocumentConfig(type, offset, limit, orderBy,
                sortOrder, fromDate, toDate, displayName, active);

        final Page<DocumentConfigData> documentConfigData;
        // client
        // loans
        //if (is(type, "client")) {
        documentConfigData = this.documentConfigReadPlatformService.retrieveAll(searchParameters);
//        } // else if (is(typeParam, "loans")) {
//          // }
//        else {
//            throw new UnrecognizedQueryParamException("typeRetrieveAll", type);
//        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentConfigApiConstants.DOCUMENT_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Document Config Template", description = """
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsTemplateResponse.class))
        )})
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DocumentConfigApiConstants.resourceName);

        final DocumentConfigData documentConfigData = this.documentConfigReadPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentConfigApiConstants.DOCUMENT_CONFIG_TEMPLATE_DATA_PARAMETERS);
    }

    @GET
    @Path("{documentId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Document Config", description = """
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
        )})
    public String retrieveOne(@PathParam("documentId") @Parameter(description = "documentId") final Long documentId,
            @Context final UriInfo uriInfo, @QueryParam("type") @Parameter(description = "type") final String type) {

        this.context.authenticatedUser().validateHasReadPermission(DocumentConfigApiConstants.resourceName);

        final DocumentConfigData documentConfigData = this.documentConfigReadPlatformService.retrieveOne(documentId
        //, type
        );

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toBusinessApiJsonSerializer.serialize(settings, documentConfigData,
                DocumentConfigApiConstants.DOCUMENT_CONFIG_RESPONSE_DATA_PARAMETERS);
    }

}
