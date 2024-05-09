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
package org.apache.fineract.portfolio.paymenttype.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymenttype.api.PaymentTypeApiResourceConstants;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.service.business.PaymentTypeGridReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/paymenttypes/grid")
@Component

@Tag(name = "Payment Type Grid", description = "This defines the an extension to payment type")
public class PaymentTypeGridApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<PaymentTypeGridData> jsonSerializer;
    private final PaymentTypeGridReadPlatformService readPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public PaymentTypeGridApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<PaymentTypeGridData> jsonSerializer,
            PaymentTypeGridReadPlatformService readPlatformService, final PaymentTypeReadPlatformService paymentTypeReadPlatformService,
            ApiRequestParameterHelper apiRequestParameterHelper, PortfolioCommandSourceWritePlatformService commandWritePlatformService) {
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandWritePlatformService = commandWritePlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    @GET
    @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types and Child ", description = "Retrieve list of payment types and Child")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.GetPaymentTypesResponse.class)))
        )})
    public String getAllPaymentTypes(@Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.resourceNameForPermissions);
        final Collection<PaymentTypeData> paymentTypes = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();
        if (!CollectionUtils.isEmpty(paymentTypes)) {
            for (PaymentTypeData paymentType : paymentTypes) {
                final Collection<PaymentTypeGridData> paymentTypeGridData = this.readPlatformService.retrievePaymentTypeGrids(paymentType.getId());
                if (!CollectionUtils.isEmpty(paymentTypeGridData)) {
                    paymentType.setPaymentTypeGridData(paymentTypeGridData);
                }
            }
        }
        //final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(paymentTypes);
    }

    @GET
    @Path("{paymentTypeId}")
    @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types Grid", description = "Retrieve list of payment types grid")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.GetPaymentTypesResponse.class)))
        )
    })
    public String getAllPaymentTypeGrids(@PathParam("paymentTypeId") @Parameter(description = "paymentTypeId") final Long paymentTypeId, @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.resourceNameForPermissions);
        final Collection<PaymentTypeGridData> paymentTypeGridData = this.readPlatformService.retrievePaymentTypeGrids(paymentTypeId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypeGridData, PaymentTypeGridApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }
    @GET
    @Path("charge/{chargeId}")
    @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Payment Types Grid Via Charge", description = "Retrieve list of payment types grid via Charge")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        //, content = @Content(array = @ArraySchema(schema = @Schema(implementation = PaymentTypeApiResourceSwagger.GetPaymentTypesResponse.class)))
        )
    })
    public String getAllPaymentTypeGridsViaCharge(@PathParam("chargeId") @Parameter(description = "chargeId") final Long chargeId, @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentTypeApiResourceConstants.resourceNameForPermissions);
        final Collection<PaymentTypeGridData> paymentTypeGridData = this.readPlatformService.retrievePaymentTypeGridsViaCharge(chargeId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, paymentTypeGridData, PaymentTypeGridApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

}
