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
package org.apache.fineract.portfolio.business.merchant.security.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.portfolio.business.merchant.security.service.MerchantAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@ConditionalOnProperty("fineract.security.basicauth.enabled")
@Path("/merchant/authentication")
@Tag(name = "Self Authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed")
public class MerchantAuthenticationApiResource {

    private final MerchantAuthService merchantAuthService;

    @Autowired
    public MerchantAuthenticationApiResource(final MerchantAuthService merchantAuthService) {
        this.merchantAuthService = merchantAuthService;
    }

    @POST
    @Path("/login")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Verify authentication", description = "Authenticates the credentials provided and returns the set roles and permissions allowed.\n\n"
            + "Please visit this link for more info - https://fineract.apache.org/legacy-docs/apiLive.htm#selfbasicauth")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String authenticateLogin(final String apiRequestBodyAsJson) {
        return this.merchantAuthService.authenticate(apiRequestBodyAsJson);
    }
}
