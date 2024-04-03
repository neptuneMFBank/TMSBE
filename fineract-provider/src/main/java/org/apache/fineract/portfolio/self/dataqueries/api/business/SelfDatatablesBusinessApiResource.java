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
package org.apache.fineract.portfolio.self.dataqueries.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.dataqueries.api.DatatablesApiResource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/datatables/{clientId}/{datatable}/{apptableId}")
@Component
@Scope("singleton")

@Tag(name = "Self Data Tables", description = "")
public class SelfDatatablesBusinessApiResource {

    private final DatatablesApiResource datatablesApiResource;
    private final PlatformSecurityContext context;
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfDatatablesBusinessApiResource(final PlatformSecurityContext context, final DatatablesApiResource datatablesApiResource,
            final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.context = context;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.datatablesApiResource = datatablesApiResource;
    }

    @GET
    @Path("{datatableId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Single Data table", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String getDatatableManyEntry(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @PathParam("datatableId") @Parameter(description = "datatableId") final Long datatableId, @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.datatablesApiResource.getDatatableManyEntry(datatable, apptableId, datatableId, null, true, uriInfo);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Single Data table", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String getDatatable(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId, @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.datatablesApiResource.getDatatable(datatable, apptableId, null, uriInfo);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String createDatatableEntry(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        validateAppuserClientsMapping(clientId);
        return this.datatablesApiResource.createDatatableEntry(datatable, apptableId, apiRequestBodyAsJson);
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String updateDatatableEntryOnetoOne(
            @PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @PathParam("datatable") @Parameter(description = "datatable") final String datatable,
            @PathParam("apptableId") @Parameter(description = "apptableId") final Long apptableId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        validateAppuserClientsMapping(clientId);
        return this.datatablesApiResource.updateDatatableEntryOnetoOne(datatable, apptableId, apiRequestBodyAsJson);
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

}
