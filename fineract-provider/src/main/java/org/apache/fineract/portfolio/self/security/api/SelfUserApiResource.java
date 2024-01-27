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
package org.apache.fineract.portfolio.self.security.api;

import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.useradministration.api.UsersApiResource;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("/self/user")
@Component

@Tag(name = "Self User", description = "")
public class SelfUserApiResource {

    private final UsersApiResource usersApiResource;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;
    private final Set<String> supportedParameters = new HashSet<>(Arrays.asList("password", "repeatPassword"));
    private final AppuserClientMapperReadService appUserClientMapperReadService;

    @Autowired
    public SelfUserApiResource(final UsersApiResource usersApiResource, final PlatformSecurityContext context,
            final AppuserClientMapperReadService appUserClientMapperReadService, final FromJsonHelper fromApiJsonHelper) {
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.usersApiResource = usersApiResource;
        this.context = context;
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    @PUT
    @Operation(summary = "Update User Password", description = "This API can be used by Self Service user to update their own user information. Currently, \"password\" and \"repeatPassword\" are the only parameters accepted.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = SelfUserApiResourceSwagger.PutSelfUserRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfUserApiResourceSwagger.PutSelfUserResponse.class))) })
    public String update(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        if (StringUtils.isBlank(apiRequestBodyAsJson)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, apiRequestBodyAsJson, this.supportedParameters);

        final AppUser appUser = this.context.authenticatedUser();
        return this.usersApiResource.update(appUser.getId(), apiRequestBodyAsJson);
    }

    @GET
    @Path("{userId}")
    @Operation(summary = "Retrieve a User", description = "This API can be used by Self Service user to retrieve their own user information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfUserApiResourceSwagger.GetUsersUserIdResponse.class))) })
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("userId") @Parameter(description = "userId") final Long userId, @Context final UriInfo uriInfo) {
        validateAppuserUserMapping(userId);

        return this.usersApiResource.retrieveOne(userId, uriInfo);

    }

    private void validateAppuserUserMapping(final Long userId) {
        final AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = Objects.equals(user.getId(), userId);
        if (!mappedClientId) {
            throw new UserNotFoundException(userId);
        }
    }
}
