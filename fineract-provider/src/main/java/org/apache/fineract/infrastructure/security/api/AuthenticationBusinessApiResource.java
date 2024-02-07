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
package org.apache.fineract.infrastructure.security.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.business.AuthenticationBusinessWritePlatformService;
import org.apache.fineract.simplifytech.data.ApiResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@Path("/authentication")
@Tag(name = "Authentication HTTP Basic", description = "An API capability that allows client applications to verify authentication details using HTTP Basic Authentication.")
public class AuthenticationBusinessApiResource {

    private final AuthenticationBusinessWritePlatformService authenticationBusinessWritePlatformService;

    private final ToApiJsonSerializer<ApiResponseMessage> apiJsonSerializerService;

    @Autowired
    public AuthenticationBusinessApiResource(final AuthenticationBusinessWritePlatformService authenticationBusinessWritePlatformService,
            final ToApiJsonSerializer<ApiResponseMessage> apiJsonSerializerService) {
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.authenticationBusinessWritePlatformService = authenticationBusinessWritePlatformService;
    }

    @POST
    @Path("reset-password")
    @Produces({MediaType.APPLICATION_JSON})
    public String resetCustomerPassword(final String apiRequestBodyAsJson) {
        final ApiResponseMessage apiResponseMessage = this.authenticationBusinessWritePlatformService.resetPassword(apiRequestBodyAsJson);
        return this.apiJsonSerializerService.serialize(apiResponseMessage);
    }

}
