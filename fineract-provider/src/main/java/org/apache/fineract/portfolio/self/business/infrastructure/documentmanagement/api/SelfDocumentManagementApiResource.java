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
package org.apache.fineract.portfolio.self.business.infrastructure.documentmanagement.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.documentmanagement.api.DocumentManagementApiResource;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentBusinessManagementApiResource;
import org.apache.fineract.infrastructure.documentmanagement.exception.business.DocumentConfigNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.business.infrastructure.documentmanagement.service.AppuserClientIdentifierMapperReadService;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserClientMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Scope("singleton")
@Path("/self/{entityType}/{entityId}/documents")
@Tag(name = "Documents", description = "")
public class SelfDocumentManagementApiResource {

    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final AppuserClientIdentifierMapperReadService appuserClientIdentifierMapperReadService;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;
    private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;
    private final DocumentBusinessManagementApiResource documentBusinessManagementApiResource;
    private final DocumentManagementApiResource documentManagementApiResource;

    private final PlatformSecurityContext context;

    @Autowired
    public SelfDocumentManagementApiResource(final PlatformSecurityContext context,
            final AppuserClientMapperReadService appUserClientMapperReadService,
            final AppuserLoansMapperReadService appuserLoansMapperReadService,
            final AppuserSavingsMapperReadService appuserSavingsMapperReadService,
            final AppuserClientIdentifierMapperReadService appuserClientIdentifierMapperReadService,
            final DocumentBusinessManagementApiResource documentBusinessManagementApiResource,
            final DocumentManagementApiResource documentManagementApiResource) {
        this.context = context;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.appuserLoansMapperReadService = appuserLoansMapperReadService;
        this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;
        this.appuserClientIdentifierMapperReadService = appuserClientIdentifierMapperReadService;
        this.documentBusinessManagementApiResource = documentBusinessManagementApiResource;
        this.documentManagementApiResource = documentManagementApiResource;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDocuments(@Context final UriInfo uriInfo,
            @PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId) {
        validateAppuser(entityType, entityId, null);
        return this.documentManagementApiResource.retrieveAllDocuments(uriInfo, entityType, entityId);
    }

    @POST
    @Path("base64")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createBase64Document(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId, final String apiRequestBodyAsJson) {
        validateAppuser(entityType, entityId, null);
        return this.documentBusinessManagementApiResource.createBase64Document(entityType, entityId, apiRequestBodyAsJson);
    }

    @POST
    @Path("bulk-base64")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createBulkBase64Document(@PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId, final String apiRequestBodyAsJson) {
        validateAppuser(entityType, entityId, null);
        return this.documentBusinessManagementApiResource.createBulkBase64Document(entityType, entityId, apiRequestBodyAsJson);
    }

    @GET
    @Path("{documentId}/attachment")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAttachment(@PathParam("documentId") @Parameter(description = "documentId") final Long documentId,
            @PathParam("entityType") @Parameter(description = "entityType") final String entityType,
            @PathParam("entityId") @Parameter(description = "entityId") final Long entityId) {
        validateAppuser(entityType, entityId, documentId);
        return this.documentBusinessManagementApiResource.retrieveAttachment(documentId, entityType, entityId);
    }

    private void validateAppuser(final String entityType, final Long entityId, final Long documentId) {
        if (StringUtils.equalsIgnoreCase(entityType, DocumentManagementEntity.LOANS.name())) {
            validateAppuserLoanMapping(entityId);
        } else if (StringUtils.equalsIgnoreCase(entityType, DocumentManagementEntity.CLIENTS.name())) {
            validateAppuserClientsMapping(entityId);
        } else if (StringUtils.equalsIgnoreCase(entityType, DocumentManagementEntity.CLIENT_IDENTIFIERS.name())) {
            validateAppuserClientIdentfiersMapping(documentId);
        } else if (StringUtils.equalsIgnoreCase(entityType, DocumentManagementEntity.SAVINGS.name())) {
            validateAppuserSavingsAccountMapping(entityId);
        } else {
            throw new DocumentConfigNotFoundException(entityId);
        }
    }

    private void validateAppuserLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.appuserLoansMapperReadService.isLoanMappedToUser(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }

    private void validateAppuserClientIdentfiersMapping(final Long clientIdentifierId) {
        AppUser user = this.context.authenticatedUser();
        Long clientId = null;
        Set<AppUserClientMapping> appUserClientMappings = user.getAppUserClientMappings();
        if (!CollectionUtils.isEmpty(appUserClientMappings)) {
            final AppUserClientMapping appUserClientMapping = user.getAppUserClientMappings().stream().findAny().orElse(null);
            if (appUserClientMapping != null) {
                final Client client = appUserClientMapping.getClient();
                if (client != null) {
                    clientId = client.getId();
                }
            }
        }

        final boolean mappedClientIdentifierID = this.appuserClientIdentifierMapperReadService
                .isClientIdentifierMappedToUser(clientIdentifierId, clientId);
        if (!mappedClientIdentifierID) {
            throw new ClientIdentifierNotFoundException(clientIdentifierId);
        }
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

    private void validateAppuserSavingsAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMappedSavings = this.appuserSavingsMapperReadService.isSavingsMappedToUser(accountId, user.getId());
        if (!isMappedSavings) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }
}
