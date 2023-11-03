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
package org.apache.fineract.portfolio.client.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessData;
import org.apache.fineract.portfolio.client.service.business.ClientIdentifierBusinessReadPlatformService;
import org.apache.fineract.portfolio.client.service.business.ClientIdentifierBusinessWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/{clientId}/identifiers/business")
@Component
@Scope("singleton")
@Tag(name = "Client Identifier", description = """
        Client Identifiers refer to documents that are used to uniquely identify a customer
        Ex: Drivers License, Passport, Ration card etc """)
public class ClientIdentifiersBusinessApiResource {

    private static final Set<String> CLIENT_IDENTIFIER_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "clientId", "documentType", "documentKey", "description", "allowedDocumentTypes", "attachmentId"));

    private final String resourceNameForPermissions = "CLIENTIDENTIFIER";
    private final PlatformSecurityContext context;
    private final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientIdentifierBusinessData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final ClientIdentifierBusinessWritePlatformService clientIdentifierBusinessWritePlatformService;

    @Autowired
    public ClientIdentifiersBusinessApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<ClientIdentifierBusinessData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final ClientIdentifierBusinessReadPlatformService clientIdentifierBusinessReadPlatformService,
            final ClientIdentifierBusinessWritePlatformService clientIdentifierBusinessWritePlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.clientIdentifierBusinessReadPlatformService = clientIdentifierBusinessReadPlatformService;
        this.clientIdentifierBusinessWritePlatformService = clientIdentifierBusinessWritePlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Identifier for a Client", description = """
            Mandatory Fields
            documentKey, documentTypeId, location, type """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersResponse.class))
    ) })
    public String createClientIdentifier(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandProcessingResult documentIdentifier = this.clientIdentifierBusinessWritePlatformService.addClientIdentifier(clientId,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(documentIdentifier);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Identifiers for a Client", description = """
            Example Requests:
            clients/1/identifiers/business""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(array = @ArraySchema(schema = @Schema(implementation =
    // ClientIdentifiersApiResourceSwagger.GetClientsClientIdIdentifiersResponse.class)))
    ) })
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ClientIdentifierBusinessData> clientIdentifiers = this.clientIdentifierBusinessReadPlatformService
                .retrieveClientIdentifiers(clientId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientIdentifiers, CLIENT_IDENTIFIER_DATA_PARAMETERS);
    }

    @PUT
    @Path("{identifierId}/{clientDocumentId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an Identifier for a Client", description = """
            Mandatory Fields
            documentKey, documentTypeId, location, type """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientIdentifiersApiResourceSwagger.PostClientsClientIdIdentifiersResponse.class))
    ) })
    public String updateClientIdentifier(@PathParam("identifierId") @Parameter(description = "identifierId") final Long clientIdentifierId,
            @PathParam("clientDocumentId") @Parameter(description = "clientDocumentId") final Long clientDocumentId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandProcessingResult documentIdentifier = this.clientIdentifierBusinessWritePlatformService
                .updateClientIdentifier(clientId, clientIdentifierId, clientDocumentId, apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(documentIdentifier);
    }
}
