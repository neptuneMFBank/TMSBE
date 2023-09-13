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
package org.apache.fineract.infrastructure.documentmanagement.api.business;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.documentmanagement.exception.InvalidEntityTypeForImageManagementException;
import org.apache.fineract.infrastructure.documentmanagement.service.business.ImageBusinessWritePlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
@Path("business/{entity}/{entityId}/images")
public class ImagesBusinessApiResource {

    private final ImageBusinessWritePlatformService imageWritePlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;

    @Autowired
    public ImagesBusinessApiResource(final ImageBusinessWritePlatformService imageWritePlatformService,
            final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer) {
        this.imageWritePlatformService = imageWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    /**
     * Upload image as a Data URL (essentially a base64 encoded stream)
     *
     * @param entityName
     * @param entityId
     * @param jsonRequestBody
     * @return
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @RequestBody(required = true)
    public String addNewClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @Parameter(hidden = true) final String jsonRequestBody) {
        validateEntityTypeforImage(entityName);

        final CommandProcessingResult result = this.imageWritePlatformService.saveOrUpdateImage(entityName, entityId, jsonRequestBody);

        return this.toApiJsonSerializer.serialize(result);
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     *
     * Upload image as a Data URL (essentially a base64 encoded stream)
     *
     * @param entityName
     * @param entityId
     * @param jsonRequestBody
     * @return
     */
    @PUT
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @RequestBody(required = true)
    public String updateClientImage(@PathParam("entity") final String entityName, @PathParam("entityId") final Long entityId,
            @Parameter(hidden = true) final String jsonRequestBody) {
        return addNewClientImage(entityName, entityId, jsonRequestBody);
    }

    /**
     * * Entities for document Management *
     */
    public enum EntityTypeForImages {

        STAFF, CLIENTS;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private void validateEntityTypeforImage(final String entityName) {
        if (!checkValidEntityType(entityName)) {
            throw new InvalidEntityTypeForImageManagementException(entityName);
        }
    }

    private static boolean checkValidEntityType(final String entityType) {
        for (final EntityTypeForImages entities : EntityTypeForImages.values()) {
            if (entities.name().equalsIgnoreCase(entityType)) {
                return true;
            }
        }
        return false;
    }
}
