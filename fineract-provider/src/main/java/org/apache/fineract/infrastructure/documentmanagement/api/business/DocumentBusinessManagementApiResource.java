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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentBusinessWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Path("business/{entityType}/{entityId}/documents")
@Tag(name = "Documents", description = """
        Multiple Documents (a combination of a name, description and a file) may be attached to different Entities like Clients, Groups, Staff, Loans, Savings and Client Identifiers in the system

        Note: The currently allowed Entities are

        Clients: URL Pattern as clients
        Staff: URL Pattern as staff
        Loans: URL Pattern as loans
        Savings: URL Pattern as savings
        Client Identifiers: URL Pattern as client_identifiers
        Groups: URL Pattern as groups""")
public class DocumentBusinessManagementApiResource {

    private final String systemEntityType = "DOCUMENT";

    private final PlatformSecurityContext context;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DocumentBusinessWritePlatformService documentWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ToApiJsonSerializer<DocumentData> toApiJsonSerializer;

    @Autowired
    public DocumentBusinessManagementApiResource(final PlatformSecurityContext context,
            final DocumentReadPlatformService documentReadPlatformService,
            final DocumentBusinessWritePlatformService documentWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper, final ToApiJsonSerializer<DocumentData> toApiJsonSerializer) {
        this.context = context;
        this.documentReadPlatformService = documentReadPlatformService;
        this.documentWritePlatformService = documentWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @POST
    @Path("base64")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Base64 Document", description = """
            Note: A document is created using Base64 upload

            Body

            name :
            Name or summary of the document

            description :
            Description of the document

            location :
            The Base64 file to be uploaded

            Mandatory Fields :
            location, name, fileName, type and description""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // DocumentManagementApiResourceSwagger.PostEntityTypeEntityIdDocumentsRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = ""
    // , content = @Content(schema = @Schema(implementation =
    // DocumentManagementApiResourceSwagger.PostEntityTypeEntityIdDocumentsResponse.class))
    ) })
    public String createBase64Document(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId, final String apiRequestBodyAsJson) {

        final CommandProcessingResult documentIdentifier = this.documentWritePlatformService.createBase64Document(entityType, entityId,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(documentIdentifier);

    }

    @POST
    @Path("bulk-base64")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Bulk Base64 Document", description = "")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // DocumentManagementApiResourceSwagger.PostEntityTypeEntityIdDocumentsRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = ""
    // , content = @Content(schema = @Schema(implementation =
    // DocumentManagementApiResourceSwagger.PostEntityTypeEntityIdDocumentsResponse.class))
    ) })
    public String createBulkBase64Document(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId, final String apiRequestBodyAsJson) {

        final CommandProcessingResult documentIdentifier = this.documentWritePlatformService.createBulkBase64Document(entityType, entityId,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(documentIdentifier);

    }

    @GET
    @Path("{documentId}/attachment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Avatar", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetAllClientIdResponse.class))
    ) })
    public String retrieveAttachment(@PathParam("documentId") @Parameter(description = "documentId") final Long documentId,
            @PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId) {
        this.context.authenticatedUser().validateHasReadPermission(this.systemEntityType);
        final CommandProcessingResult result = this.documentWritePlatformService.retrieveAttachment(entityType, entityId, documentId);
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{documentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Document", description = """
            Note: A document is updated using a Json
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "Update Document"
    // , content = @Content(schema = @Schema(implementation =
    // DocumentManagementApiResourceSwagger.PutEntityTypeEntityIdDocumentsResponse.class))
    ) })
    public String updateDocument(@PathParam("documentId") @Parameter(description = "documentId") final Long documentId,
            @PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId, final String apiRequestBodyAsJson) {

        final CommandProcessingResult documentIdentifier = this.documentWritePlatformService.updateBase64Document(documentId, entityType,
                entityId, apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(documentIdentifier);
    }
}
