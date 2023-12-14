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
package org.apache.fineract.portfolio.client.service.business;

import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.descriptionParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.documentKeyParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.documentTypeIdParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.locationParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.resourceIdParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.typeParam;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentBusinessWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.ClientIdentifiersApiResource;
import org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessDataValidator;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientIdentifierBusinessWritePlatformServiceImpl implements ClientIdentifierBusinessWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(ClientIdentifierBusinessWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final ClientIdentifierBusinessDataValidator clientIdentifierBusinessDataValidator;
    private final ClientIdentifiersApiResource clientIdentifiersApiResource;
    private final DocumentBusinessWritePlatformService documentWritePlatformService;
    private final FromJsonHelper fromApiJsonHelper;
    private final DocumentRepository documentRepository;

    @Autowired
    public ClientIdentifierBusinessWritePlatformServiceImpl(final PlatformSecurityContext context,
            final ClientRepositoryWrapper clientRepository, final ClientIdentifierRepository clientIdentifierRepository,
            final CodeValueRepositoryWrapper codeValueRepository,
            final ClientIdentifierBusinessDataValidator clientIdentifierBusinessDataValidator, final FromJsonHelper fromApiJsonHelper,
            final ClientIdentifiersApiResource clientIdentifiersApiResource,
            final DocumentBusinessWritePlatformService documentWritePlatformService, final DocumentRepository documentRepository) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientIdentifierRepository = clientIdentifierRepository;
        this.codeValueRepository = codeValueRepository;
        this.clientIdentifierBusinessDataValidator = clientIdentifierBusinessDataValidator;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.clientIdentifiersApiResource = clientIdentifiersApiResource;
        this.documentWritePlatformService = documentWritePlatformService;
        this.documentRepository = documentRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult addClientIdentifier(final Long clientId, final String apiRequestBodyAsJson) {

        this.context.authenticatedUser();
        this.clientIdentifierBusinessDataValidator.validateForCreate(apiRequestBodyAsJson);
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);

        final Long documentTypeId = this.fromApiJsonHelper.extractLongNamed(documentTypeIdParam, jsonElement);
        final CodeValue documentType = this.codeValueRepository.findOneWithNotFoundDetection(documentTypeId);
        final String name = documentType.label();

        final String documentKey = this.fromApiJsonHelper.extractStringNamed(documentKeyParam, jsonElement);
        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParam, jsonElement);
        final String type = this.fromApiJsonHelper.extractStringNamed(typeParam, jsonElement);
        String location = this.fromApiJsonHelper.extractStringNamed(locationParam, jsonElement);

        final JsonObject jsonClientIdentifier = new JsonObject();
        jsonClientIdentifier.addProperty(documentTypeIdParam, documentTypeId);
        if (StringUtils.isNotBlank(documentKey)) {
            jsonClientIdentifier.addProperty(documentKeyParam, documentKey);
        }
        if (StringUtils.isNotBlank(description)) {
            jsonClientIdentifier.addProperty(descriptionParam, description);
        }
        jsonClientIdentifier.addProperty(ClientApiConstants.statusParamName, "Active");

        final String clientIdentifierResult = this.clientIdentifiersApiResource.createClientIdentifier(clientId,
                jsonClientIdentifier.toString());
        final JsonElement jsonclientIdentifierElement = this.fromApiJsonHelper.parse(clientIdentifierResult);
        final Long resourceId = this.fromApiJsonHelper.extractLongNamed(resourceIdParam, jsonclientIdentifierElement);

        final JsonObject jsonClientIdentifierDocument = new JsonObject();
        jsonClientIdentifierDocument.addProperty(typeParam, type);
        jsonClientIdentifierDocument.addProperty(locationParam, location);
        jsonClientIdentifierDocument.addProperty(DocumentConfigApiConstants.nameParam, name);
        // if (StringUtils.isNotBlank(description)) {
        // jsonClientIdentifierDocument.addProperty(descriptionParam, documentTypeId);
        // }
        final CommandProcessingResult documentIdentifier = this.documentWritePlatformService.createBase64Document("client_identifiers",
                resourceId, jsonClientIdentifierDocument.toString());
        final Long subResourceId = documentIdentifier.resourceId();

        return new CommandProcessingResultBuilder() //
                .withEntityId(resourceId).withSubEntityId(subResourceId).build();

    }

    @Override
    public CommandProcessingResult updateClientIdentifier(Long clientId, Long identifierId, final Long clientDocumentId,
            String apiRequestBodyAsJson) {

        this.context.authenticatedUser();
        final String clientIdentifiers = "client_identifiers";
        this.clientIdentifierBusinessDataValidator.validateForCreate(apiRequestBodyAsJson);

        this.clientRepository.findOneWithNotFoundDetection(clientId);

        this.clientIdentifierRepository.findById(identifierId).orElseThrow(() -> new ClientIdentifierNotFoundException(identifierId));

        this.documentRepository.findById(clientDocumentId)
                .orElseThrow(() -> new DocumentNotFoundException(clientIdentifiers, identifierId, clientDocumentId));

        final JsonElement jsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);

        final Long documentTypeId = this.fromApiJsonHelper.extractLongNamed(documentTypeIdParam, jsonElement);
        final CodeValue documentType = this.codeValueRepository.findOneWithNotFoundDetection(documentTypeId);
        final String name = documentType.label();

        final String documentKey = this.fromApiJsonHelper.extractStringNamed(documentKeyParam, jsonElement);
        final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParam, jsonElement);
        final String type = this.fromApiJsonHelper.extractStringNamed(typeParam, jsonElement);
        String location = this.fromApiJsonHelper.extractStringNamed(locationParam, jsonElement);

        final JsonObject jsonClientIdentifier = new JsonObject();
        jsonClientIdentifier.addProperty(documentTypeIdParam, documentTypeId);
        if (StringUtils.isNotBlank(documentKey)) {
            jsonClientIdentifier.addProperty(documentKeyParam, documentKey);
        }
        if (StringUtils.isNotBlank(description)) {
            jsonClientIdentifier.addProperty(descriptionParam, description);
        }
        jsonClientIdentifier.addProperty(ClientApiConstants.statusParamName, "Active");

        this.clientIdentifiersApiResource.updateClientIdentifer(clientId, identifierId, jsonClientIdentifier.toString());
        final Long resourceId = identifierId;

        final JsonObject jsonClientIdentifierDocument = new JsonObject();
        jsonClientIdentifierDocument.addProperty(typeParam, type);
        jsonClientIdentifierDocument.addProperty(locationParam, location);
        jsonClientIdentifierDocument.addProperty(DocumentConfigApiConstants.nameParam, name);
        // if (StringUtils.isNotBlank(description)) {
        // jsonClientIdentifierDocument.addProperty(descriptionParam, documentTypeId);
        // }
        this.documentWritePlatformService.updateBase64Document(clientDocumentId, clientIdentifiers, identifierId,
                jsonClientIdentifierDocument.toString());
        final Long subResourceId = clientDocumentId;

        return new CommandProcessingResultBuilder() //
                .withEntityId(resourceId).withSubEntityId(subResourceId).build();
    }

}
