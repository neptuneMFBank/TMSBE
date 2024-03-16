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
package org.apache.fineract.portfolio.business.merchant.registration.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.portfolio.business.merchant.registration.service.MerchantRegistrationWriteService;
import org.apache.fineract.portfolio.self.registration.service.SelfServiceRegistrationWritePlatformService;
import org.apache.fineract.simplifytech.data.ApiResponseMessage;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/merchant/registration")
@Component
@Scope("singleton")

@Tag(name = "Merchant Registration", description = "")
public class MerchantRegistrationApiResource {

    private final MerchantRegistrationWriteService merchantRegistrationWriteService;
    private final DefaultToApiJsonSerializer<AppUser> toApiJsonSerializer;
    private final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService;

    @Autowired
    public MerchantRegistrationApiResource(final MerchantRegistrationWriteService merchantRegistrationWriteService,
            final DefaultToApiJsonSerializer<AppUser> toApiJsonSerializer,
            final SelfServiceRegistrationWritePlatformService selfServiceRegistrationWritePlatformService) {
        this.merchantRegistrationWriteService = merchantRegistrationWriteService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.selfServiceRegistrationWritePlatformService = selfServiceRegistrationWritePlatformService;
    }

    @POST
    @Path("getstarted")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String createMerchant(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        ApiResponseMessage apiResponseMessage = this.merchantRegistrationWriteService.createMerchant(apiRequestBodyAsJson);
        return toApiJsonSerializer.serializeResult(apiResponseMessage);

    }

    @POST
    @Path("getstarted/resend")
    @Produces({MediaType.APPLICATION_JSON})
    public String resendCustomeronRequest(final String apiRequestBodyAsJson) {
        final ApiResponseMessage apiResponseMessage = this.merchantRegistrationWriteService
                .resendCustomeronRequest(apiRequestBodyAsJson);
        return toApiJsonSerializer.serializeResult(apiResponseMessage);
    }

    @POST
    @Path("validate")
    @Produces({MediaType.APPLICATION_JSON})
    public String validateCustomer(final String apiRequestBodyAsJson) {
        final ApiResponseMessage apiResponseMessage = this.selfServiceRegistrationWritePlatformService
                .validateCustomer(apiRequestBodyAsJson, true);
        return toApiJsonSerializer.serializeResult(apiResponseMessage);
    }

    @POST
    @Path("reset-password")
    @Produces({MediaType.APPLICATION_JSON})
    public String resetCustomerPassword(final String apiRequestBodyAsJson) {
        final ApiResponseMessage apiResponseMessage = this.merchantRegistrationWriteService.resetMerchantPassword(apiRequestBodyAsJson);
        return toApiJsonSerializer.serializeResult(apiResponseMessage);
    }
}
