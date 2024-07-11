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
package org.apache.fineract.portfolio.business.merchant.infrastructure.documentmanagement.api;

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
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.merchant.client.service.MerchantClientMapperReadService;
import org.apache.fineract.portfolio.business.merchant.inventory.exception.InventoryNotFound;
import org.apache.fineract.portfolio.business.merchant.inventory.service.MerchantInventoryMapperReadService;
import org.apache.fineract.portfolio.business.merchant.loanaccount.service.MerchantLoansMapperReadService;
import org.apache.fineract.portfolio.business.merchant.savings.api.service.MerchantSavingsMapperReadService;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.business.infrastructure.documentmanagement.service.AppuserClientIdentifierMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserClientMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Scope("singleton")
@Path("/merchant/{entityType}/{entityId}/documents")
@Tag(name = "Documents", description = "")
@Component
public class MerchantDocumentManagementApiResource {

    private final MerchantClientMapperReadService merchantClientMapperReadService;
    private final AppuserClientIdentifierMapperReadService appuserClientIdentifierMapperReadService;
    private final MerchantLoansMapperReadService merchantLoansMapperReadService;
    private final MerchantSavingsMapperReadService merchantSavingsMapperReadService;
    private final MerchantInventoryMapperReadService merchantInventoryMapperReadService;
    private final DocumentBusinessManagementApiResource documentBusinessManagementApiResource;
    private final DocumentManagementApiResource documentManagementApiResource;

    private final PlatformSecurityContext context;

    @Autowired
    public MerchantDocumentManagementApiResource(final PlatformSecurityContext context,
            final MerchantClientMapperReadService merchantClientMapperReadService,
            final MerchantLoansMapperReadService merchantLoansMapperReadService,
            final MerchantSavingsMapperReadService merchantSavingsMapperReadService,
            final AppuserClientIdentifierMapperReadService appuserClientIdentifierMapperReadService,
            final DocumentBusinessManagementApiResource documentBusinessManagementApiResource,
            final DocumentManagementApiResource documentManagementApiResource,
            final MerchantInventoryMapperReadService merchantInventoryMapperReadService) {
        this.context = context;
        this.merchantClientMapperReadService = merchantClientMapperReadService;
        this.merchantLoansMapperReadService = merchantLoansMapperReadService;
        this.merchantSavingsMapperReadService = merchantSavingsMapperReadService;
        this.appuserClientIdentifierMapperReadService = appuserClientIdentifierMapperReadService;
        this.documentBusinessManagementApiResource = documentBusinessManagementApiResource;
        this.documentManagementApiResource = documentManagementApiResource;
        this.merchantInventoryMapperReadService = merchantInventoryMapperReadService;
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
        if (StringUtils.equalsIgnoreCase(entityType, DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.LOANS.name())) {
            validateMerchantLoanMapping(entityId);
        } else if (StringUtils.equalsIgnoreCase(entityType,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.CLIENTS.name())) {
            validateMerchantClientsMapping(entityId);
        } else if (StringUtils.equalsIgnoreCase(entityType,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.CLIENT_IDENTIFIERS.name())) {
            validateMerchantClientIdentfiersMapping(documentId);
        } else if (StringUtils.equalsIgnoreCase(entityType,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.INVENTORY.name())) {
            validateMerchantInventoryMapping(entityId);
        } else if (StringUtils.equalsIgnoreCase(entityType,
                DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity.SAVINGS.name())) {
            validateMerchantSavingsAccountMapping(entityId);
        } else {
            throw new DocumentConfigNotFoundException(entityId);
        }
    }

    private void validateMerchantLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.merchantLoansMapperReadService.isLoanMappedToMerchant(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }

    private void validateMerchantClientIdentfiersMapping(final Long clientIdentifierId) {
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

    private void validateMerchantClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.merchantClientMapperReadService.isClientMappedToMerchant(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }

    private void validateMerchantSavingsAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMappedSavings = this.merchantSavingsMapperReadService.isSavingsMappedMerchant(accountId, user.getId());
        if (!isMappedSavings) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }

    private void validateMerchantInventoryMapping(final Long inventoryId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.merchantInventoryMapperReadService.isInventoryMappedToMerchant(inventoryId, user.getId());
        if (!mappedClientId) {
            throw new InventoryNotFound(inventoryId);
        }
    }

}
