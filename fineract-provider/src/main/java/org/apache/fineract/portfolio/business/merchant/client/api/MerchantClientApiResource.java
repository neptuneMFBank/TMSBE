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
package org.apache.fineract.portfolio.business.merchant.client.api;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.merchant.client.service.MerchantClientMapperReadService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.business.ClientAddressBusinessApiResources;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;
import org.apache.fineract.portfolio.client.api.business.ClientIdentifiersBusinessApiResource;
import org.apache.fineract.portfolio.client.api.business.ClientsBusinessApiResource;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessData;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformService;
import org.apache.fineract.portfolio.self.client.data.SelfClientDataValidator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/merchant/clients/{clientId}")
@Component
@Scope("singleton")
@Tag(name = "Merchant Client ", description = "")
public class MerchantClientApiResource {

    private final ClientsBusinessApiResource clientsBusinessApiResource;
    private final PlatformSecurityContext context;
    private final MerchantClientMapperReadService merchantClientMapperReadService;
    private final SelfClientDataValidator dataValidator;
    private final ClientAddressBusinessApiResources clientAddressBusinessApiResources;
    private final ClientIdentifiersBusinessApiResource clientIdentifiersBusinessApiResource;
    private final ClientBusinessReadPlatformService clientBusinessReadPlatformService;
    private final ToApiJsonSerializer<ClientBusinessData> toBusinessApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public MerchantClientApiResource(final ClientsBusinessApiResource clientsBusinessApiResource, final PlatformSecurityContext context,
            final MerchantClientMapperReadService merchantClientMapperReadService, final SelfClientDataValidator dataValidator,
            final ClientAddressBusinessApiResources clientAddressBusinessApiResources,
            final ClientBusinessReadPlatformService clientBusinessReadPlatformService,
            final ClientIdentifiersBusinessApiResource clientIdentifiersBusinessApiResource,
            final ToApiJsonSerializer<ClientBusinessData> toBusinessApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.clientsBusinessApiResource = clientsBusinessApiResource;
        this.context = context;
        this.merchantClientMapperReadService = merchantClientMapperReadService;
        this.dataValidator = dataValidator;
        this.clientAddressBusinessApiResources = clientAddressBusinessApiResources;
        this.clientIdentifiersBusinessApiResource = clientIdentifiersBusinessApiResource;
        this.clientBusinessReadPlatformService = clientBusinessReadPlatformService;
        this.toBusinessApiJsonSerializer = toBusinessApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Path("kyc-level")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client Checker")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveKycLevel(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateMerchantClientsMapping(clientId);
        return this.clientsBusinessApiResource.retrieveKycLevel(clientId, uriInfo);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Details Template", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveTemplate(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateMerchantClientsMapping(clientId);
        final Integer legalFormId = LegalForm.MERCHANT.getValue();
        final Boolean staffInSelectedOfficeOnly = null;
        final Long officeId = null;
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ClientBusinessData clientData;

        clientData = this.clientBusinessReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly, legalFormId);
        clientData.setSavingProductOptions(null);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveOne(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        this.dataValidator.validateRetrieveOne(uriInfo);
        validateMerchantClientsMapping(clientId);
        final Boolean staffInSelectedOfficeOnly = null;
        return this.clientsBusinessApiResource.retrieveOne(clientId, uriInfo, staffInSelectedOfficeOnly);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String update(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateMerchantClientsMapping(clientId);
        return this.clientsBusinessApiResource.update(clientId, apiRequestBodyAsJson);
    }

    @GET
    @Path("addresses/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAddressesTemplate(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateMerchantClientsMapping(clientId);
        return this.clientAddressBusinessApiResources.getAddressesTemplate(uriInfo);
    }

    @GET
    @Path("addresses/{id}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get Single address for a Client", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String getAddress(@PathParam("id") @Parameter(description = "id") final long id,
            @PathParam("clientId") @Parameter(description = "clientId") final long clientId, @Context final UriInfo uriInfo) {
        validateMerchantClientsMapping(clientId);
        return this.clientAddressBusinessApiResources.getAddress(id, clientId, uriInfo);
    }

    @GET
    @Path("addresses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Get All active address for a Client", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String getAddresses(@PathParam("clientId") @Parameter(description = "clientId") final long clientId,
            @QueryParam("type") @Parameter(description = "type") final long addressTypeId, @Context final UriInfo uriInfo) {
        validateMerchantClientsMapping(clientId);
        final String status = "true";
        return this.clientAddressBusinessApiResources.getAddresses(status, addressTypeId, clientId, uriInfo);
    }

    @POST
    @Path("addresses")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an address for a Client", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String addClientAddress(@QueryParam("type") @Parameter(description = "type") final long addressTypeId,
            @PathParam("clientId") @Parameter(description = "clientId") final long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateMerchantClientsMapping(clientId);
        return this.clientAddressBusinessApiResources.addClientAddress(addressTypeId, clientId, apiRequestBodyAsJson);
    }

    @POST
    @Path("identifiers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Identifier for a Client", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String createClientIdentifier(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateMerchantClientsMapping(clientId);
        return this.clientIdentifiersBusinessApiResource.createClientIdentifier(clientId, apiRequestBodyAsJson);
    }

    @GET
    @Path("identifiers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List all Identifiers for a Client", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAllClientIdentifiers(@Context final UriInfo uriInfo,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        validateMerchantClientsMapping(clientId);
        return this.clientIdentifiersBusinessApiResource.retrieveAllClientIdentifiers(uriInfo, clientId);
    }

    private void validateMerchantClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.merchantClientMapperReadService.isClientMappedToMerchant(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
