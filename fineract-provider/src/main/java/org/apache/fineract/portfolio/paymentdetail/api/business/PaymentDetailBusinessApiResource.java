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
package org.apache.fineract.portfolio.paymentdetail.api.business;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.paymentdetail.service.business.PaymentDetailReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/paymentdetail")
@Component

@Tag(name = "Payment Detail", description = "This defines the payment detail")
public class PaymentDetailBusinessApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<JsonObject> jsonSerializer;
    private final PaymentDetailReadPlatformService paymentDetailReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public PaymentDetailBusinessApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<JsonObject> jsonSerializer,
            PaymentDetailReadPlatformService paymentDetailReadPlatformService, final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.paymentDetailReadPlatformService = paymentDetailReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @GET
    @Path("{receiptNumber}/receipt-number-exits")
    @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Check a Payment Detail Receipt Number", description = "Confirm Exist")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")})
    public String isReceiptNumberExisting(@PathParam("receiptNumber") @Parameter(description = "receiptNumber") final String receiptNumber,
            @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(PaymentDetailBusinessApiResourceConstants.resourceNameForPermissions);
        final JsonObject jsonObject = this.paymentDetailReadPlatformService.isReceiptNumberExisting(receiptNumber);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, jsonObject, PaymentDetailBusinessApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

}
