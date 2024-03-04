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
package org.apache.fineract.portfolio.self.client.api.business;

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
import org.apache.fineract.infrastructure.documentmanagement.api.business.ImagesBusinessApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientFamilyMembersApiResources;
import org.apache.fineract.portfolio.client.api.business.ClientAddressBusinessApiResources;
import org.apache.fineract.portfolio.client.api.business.ClientIdentifiersBusinessApiResource;
import org.apache.fineract.portfolio.client.api.business.ClientsBusinessApiResource;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.data.SelfClientDataValidator;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/clients/business/{clientId}")
@Component
@Scope("singleton")

@Tag(name = "Self Client Business", description = "")
public class SelfClientsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final ImagesBusinessApiResource imagesApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final SelfClientDataValidator dataValidator;
    private final ClientsBusinessApiResource clientsBusinessApiResource;
    private final ClientAddressBusinessApiResources clientAddressBusinessApiResources;
    private final ClientFamilyMembersApiResources clientFamilyMembersApiResources;
    private final ClientIdentifiersBusinessApiResource clientIdentifiersBusinessApiResource;

    @Autowired
    public SelfClientsBusinessApiResource(final PlatformSecurityContext context,
            final ClientAddressBusinessApiResources clientAddressBusinessApiResources,
            final ClientIdentifiersBusinessApiResource clientIdentifiersBusinessApiResource,
            final ImagesBusinessApiResource imagesApiResource, final ClientsBusinessApiResource clientsBusinessApiResource,
            final ClientFamilyMembersApiResources clientFamilyMembersApiResources,
            final AppuserClientMapperReadService appUserClientMapperReadService, final SelfClientDataValidator dataValidator) {
        this.context = context;
        this.imagesApiResource = imagesApiResource;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.dataValidator = dataValidator;
        this.clientsBusinessApiResource = clientsBusinessApiResource;
        this.clientAddressBusinessApiResources = clientAddressBusinessApiResources;
        this.clientFamilyMembersApiResources = clientFamilyMembersApiResources;
        this.clientIdentifiersBusinessApiResource = clientIdentifiersBusinessApiResource;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Details Template", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveTemplate(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        final Integer legalFormId = LegalForm.PERSON.getValue();
        final Boolean staffInSelectedOfficeOnly = null;
        final Long officeId = null;
        return this.clientsBusinessApiResource.retrieveTemplate(uriInfo, officeId, staffInSelectedOfficeOnly, legalFormId);
    }

    @GET
    @Path("kyc-level")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = """
            Example Requests:""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveKycLevel(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.clientsBusinessApiResource.retrieveKycLevel(clientId, uriInfo);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveOne(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        this.dataValidator.validateRetrieveOne(uriInfo);
        validateAppuserClientsMapping(clientId);
        final Boolean staffInSelectedOfficeOnly = null;
        return this.clientsBusinessApiResource.retrieveOne(clientId, uriInfo, staffInSelectedOfficeOnly);
    }

    @GET
    @Path("balance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client accounts overview", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAssociatedAccounts(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.clientsBusinessApiResource.retrieveAssociatedAccounts(clientId, uriInfo);
    }

    @GET
    @Path("avatar")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Avatar", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAvatar(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        validateAppuserClientsMapping(clientId);
        final String entityName = "clients";
        return this.imagesApiResource.retrieveAvatar(entityName, clientId);
    }

    @POST
    @Path("avatar")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addNewClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
        validateAppuserClientsMapping(clientId);
        final String entityName = "clients";
        return this.imagesApiResource.addNewClientImage(entityName, clientId, jsonRequestBody);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String update(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateAppuserClientsMapping(clientId);
        return this.clientsBusinessApiResource.update(clientId, apiRequestBodyAsJson);
    }

    @GET
    @Path("addresses/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getAddressesTemplate(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
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
        validateAppuserClientsMapping(clientId);
        return this.clientAddressBusinessApiResources.getAddress(id, clientId, uriInfo);
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
        validateAppuserClientsMapping(clientId);
        return this.clientAddressBusinessApiResources.addClientAddress(addressTypeId, clientId, apiRequestBodyAsJson);
    }

    @GET
    @Path("familymembers/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getTemplate(@Context final UriInfo uriInfo, @PathParam("clientId") final long clientId) {
        validateAppuserClientsMapping(clientId);
        return this.clientFamilyMembersApiResources.getTemplate(uriInfo, clientId);
    }

    @POST
    @Path("familymembers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String addClientFamilyMembers(@PathParam("clientId") final long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateAppuserClientsMapping(clientId);
        return this.clientFamilyMembersApiResources.addClientFamilyMembers(clientId, apiRequestBodyAsJson);

    }

    @GET
    @Path("familymembers/{familyMemberId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getFamilyMember(@Context final UriInfo uriInfo, @PathParam("familyMemberId") final Long familyMemberId,
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId) {
        validateAppuserClientsMapping(clientId);
        return this.clientFamilyMembersApiResources.getFamilyMember(uriInfo, familyMemberId, clientId);
    }

    @GET
    @Path("familymembers")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String getFamilyMembers(@Context final UriInfo uriInfo, @PathParam("clientId") final long clientId) {
        validateAppuserClientsMapping(clientId);
        return this.clientFamilyMembersApiResources.getFamilyMembers(uriInfo, clientId);
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
        validateAppuserClientsMapping(clientId);
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
        validateAppuserClientsMapping(clientId);
        return this.clientIdentifiersBusinessApiResource.retrieveAllClientIdentifiers(uriInfo, clientId);
    }
}
