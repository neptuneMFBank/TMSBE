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
package org.apache.fineract.portfolio.charge.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.business.ChargeUpfrontData;
import org.apache.fineract.portfolio.charge.service.business.ChargeUpfrontReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/charges/upfront")
@Component
@Scope("singleton")
@Tag(name = "s Upfront", description = "Its typical for MFIs to add extra costs for their financial products. These are typically Fees or Penalties.\n"
        + "\n" + "A Charge on fineract platform is what we use to model both Fees and Penalties.\n" + "\n"
        + "At present we support defining charges for use with Client accounts and both loan and saving products.")
public class ChargesUpfrontApiResource {

    private final Set<String> chargesDataParameters = new HashSet<>(Arrays.asList("id", "name", "amount", "currency", "active",
            "chargeAppliesTo", "chargeCalculationType", "chargeCalculationTypeOptions", "chargeAppliesToOptions",
            "chargeTimeTypeOptions", "currencyOptions", "loanChargeCalculationTypeOptions", "loanChargeTimeTypeOptions",
            "savingsChargeCalculationTypeOptions", "savingsChargeTimeTypeOptions", "incomeAccount", "clientChargeCalculationTypeOptions",
            "clientChargeTimeTypeOptions"));

    private final String resourceNameForPermissions = "CHARGE_UPFRONT";

    private final PlatformSecurityContext context;
    private final ChargeUpfrontReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ChargeUpfrontData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ChargesUpfrontApiResource(final PlatformSecurityContext context, final ChargeUpfrontReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<ChargeUpfrontData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Charge Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "charges/template\n")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.GetChargesUpfrontTemplateResponse.class)))})
    public String retrieveNewChargeDetails(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ChargeUpfrontData charge = this.readPlatformService.retrieveNewChargeDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charge, this.chargesDataParameters);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve ChargesUpfront", description = "Returns the list of defined charges.\n" + "\n" + "Example Requests:\n" + "\n"
            + "charges")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.GetChargesUpfrontResponse.class))))})
    public String retrieveAllChargesUpfront(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<ChargeUpfrontData> charges = this.readPlatformService.retrieveAllCharges();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, charges, this.chargesDataParameters);
    }

    @GET
    @Path("{chargeId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Charge", description = "Returns the details of a defined Charge.\n" + "\n" + "Example Requests:\n"
            + "\n" + "charges/1")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.GetChargesUpfrontResponse.class)))})
    public String retrieveCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ChargeUpfrontData charge = this.readPlatformService.retrieveCharge(chargeId);
        if (settings.isTemplate()) {
            final ChargeUpfrontData templateData = this.readPlatformService.retrieveNewChargeDetails();
            charge = ChargeUpfrontData.withTemplate(charge, templateData);
        }

        return this.toApiJsonSerializer.serialize(settings, charge, this.chargesDataParameters);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create/Define a Charge", description = "Define a new charge that can later be associated with loans and savings through their respective product definitions or directly on each account instance.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.PostChargesUpfrontRequest.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.PostChargesUpfrontResponse.class)))})
    public String createCharge(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createCharge().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{chargeId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update a Charge", description = "Updates the details of a Charge.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.PutChargesUpfrontChargeIdRequest.class)))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.PutChargesUpfrontChargeIdResponse.class)))})
    public String updateCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateCharge(chargeId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{chargeId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Delete a Charge", description = "Deletes a Charge.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = ChargesUpfrontApiResourceSwagger.DeleteChargesUpfrontChargeIdResponse.class)))})
    public String deleteCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteCharge(chargeId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }
}
