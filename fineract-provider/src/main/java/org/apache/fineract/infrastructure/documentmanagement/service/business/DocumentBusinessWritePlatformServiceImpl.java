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
package org.apache.fineract.infrastructure.documentmanagement.service.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.ImageFileExtension;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils.ImageMIMEtype;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.domain.DocumentRepository;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.exception.DocumentNotFoundException;
import org.apache.fineract.infrastructure.documentmanagement.serialization.business.DocumentBusinessDataValidator;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentBusinessWritePlatformServiceImpl implements DocumentBusinessWritePlatformService {

    private final PlatformSecurityContext context;
    private final DocumentBusinessDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final DocumentWritePlatformService documentWritePlatformService;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentBusinessWritePlatformServiceImpl(final PlatformSecurityContext context,
            final DocumentBusinessDataValidator fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper,
            final DocumentWritePlatformService documentWritePlatformService,
            final DocumentReadPlatformService documentReadPlatformService, final DocumentRepository documentRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.documentWritePlatformService = documentWritePlatformService;
        this.documentReadPlatformService = documentReadPlatformService;
        this.documentRepository = documentRepository;
    }

    @Override
    public CommandProcessingResult createBase64Document(final String entityType, final Long entityId, final String apiRequestBodyAsJson) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(entityType, entityId, apiRequestBodyAsJson);
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
        try {
            final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, jsonElement);
            final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam, jsonElement);
            final String location = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.locationParam, jsonElement);
            final InputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(location));
            String fileName = GeneralConstants.generateUniqueId();
            String attachmentType;
            final String type = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, jsonElement);
            if (StringUtils.equalsIgnoreCase(type, "gif")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.GIF.getValue());
                attachmentType = ImageMIMEtype.GIF.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "jpeg")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.JPEG.getValue());
                attachmentType = ImageMIMEtype.JPEG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "jpg")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.JPG.getValue());
                attachmentType = ImageMIMEtype.JPEG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "png")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.PNG.getValue());
                attachmentType = ImageMIMEtype.PNG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "pdf")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.PDF.getValue());
                attachmentType = ImageMIMEtype.PDF.getValue();
            } else {
                throw new ContentManagementException(name, "fileType not supported");
            }

            DocumentCommand documentCommand = new DocumentCommand(null, null, entityType, entityId, name, fileName, null, attachmentType,
                    description, null);
            Long newDocumentId = this.documentWritePlatformService.createDocument(documentCommand, inputStream);
            return new CommandProcessingResult(newDocumentId);

        } catch (ContentManagementException e) {
            log.warn("createBase64Document Error: {}", e);
            throw new PlatformDataIntegrityException("error.document.base64.upload", "Invalid documents uploaded");
        }
    }

    @Override
    public CommandProcessingResult createBulkBase64Document(final String entityType, final Long entityId,
            final String apiRequestBodyAsJson) {
        this.context.authenticatedUser();
        try {
            final Map<String, Object> changes = new HashMap<>();
            this.fromApiJsonDeserializer.validateForCreateArray(entityType, entityId, apiRequestBodyAsJson);
            final JsonArray jsonArray = this.fromApiJsonHelper.parse(apiRequestBodyAsJson).getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, jsonElement);
                final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam,
                        jsonElement);
                final String location = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.locationParam, jsonElement);
                final String type = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, jsonElement);
                final JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(DocumentConfigApiConstants.nameParam, name);
                jsonObject.addProperty(DocumentConfigApiConstants.descriptionParam, description);
                jsonObject.addProperty(DocumentConfigApiConstants.locationParam, location);
                jsonObject.addProperty(DocumentConfigApiConstants.typeParam, type);
                final CommandProcessingResult resultChanges = this.createBase64Document(entityType, entityId, jsonObject.toString());
                if (resultChanges.resourceId() != null) {
                    changes.put(DocumentConfigApiConstants.nameParam, resultChanges.resourceId());
                }
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(entityId) //
                    .with(changes).build();
        } catch (Exception e) {
            log.warn("createBulkBase64Document Error: {}", e);
            throw new PlatformDataIntegrityException("error.document.bulk.base64.upload", "Invalid documents uploaded");
        }
    }

    @Override
    public CommandProcessingResult retrieveAttachment(final String entityName, final Long entityId, final Long documentId) {
        this.context.authenticatedUser();
        final FileData fileData = this.documentReadPlatformService.retrieveFileData(entityName, entityId, documentId);

        try {
            String documentDataURISuffix = getFileExtension(fileData);

            byte[] resizedImageBytes = fileData.getByteSource().read();
            if (resizedImageBytes != null) {
                final String imageAsBase64Text = documentDataURISuffix + Base64.getMimeEncoder().encodeToString(resizedImageBytes);
                return new CommandProcessingResultBuilder().withEntityId(entityId).withResourceIdAsString(imageAsBase64Text).build();
            } else {
                throw new ContentManagementException(fileData.name(), "Document not available.");
            }
        } catch (Exception e) {
            throw new ContentManagementException(fileData.name(), e.getMessage(), e);
        }
    }

    private static String getFileExtension(FileData fileData) {
        String fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.JPEG.getValue();
        if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.GIF.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.GIF.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.PNG.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.PNG.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.PDF.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.PDF.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.PDF.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.DOC.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.DOC.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.DOC.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.DOCX.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.DOCX.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.DOCX.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.XLS.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.XLS.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.XLS.getValue();
        } else if (StringUtils.equalsIgnoreCase(fileData.contentType(), ContentRepositoryUtils.ImageMIMEtype.XLSX.getValue())
                || StringUtils.endsWith(fileData.name(), ContentRepositoryUtils.ImageFileExtension.XLSX.getValue())) {
            fileDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.XLSX.getValue();
        }
        return fileDataURISuffix;
    }

    @Override
    public CommandProcessingResult updateBase64Document(final Long documentId, final String entityType, final Long entityId, final String apiRequestBodyAsJson) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(entityType, entityId, apiRequestBodyAsJson);
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);

        this.documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(entityType,
                entityId, documentId));

        try {
            final String location = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.locationParam, jsonElement);
            final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, jsonElement);
            final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam, jsonElement);
            final InputStream inputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(location));
            String fileName = GeneralConstants.generateUniqueId();
            String attachmentType;
            final String type = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, jsonElement);
            if (StringUtils.equalsIgnoreCase(type, "gif")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.GIF.getValue());
                attachmentType = ImageMIMEtype.GIF.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "jpeg")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.JPEG.getValue());
                attachmentType = ImageMIMEtype.JPEG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "jpg")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.JPG.getValue());
                attachmentType = ImageMIMEtype.JPEG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "png")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.PNG.getValue());
                attachmentType = ImageMIMEtype.PNG.getValue();
            } else if (StringUtils.equalsIgnoreCase(type, "pdf")) {
                fileName = StringUtils.appendIfMissing(fileName, ImageFileExtension.PDF.getValue());
                attachmentType = ImageMIMEtype.PDF.getValue();
            } else {
                throw new ContentManagementException(name, "fileType not supported");
            }

            DocumentCommand documentCommand = new DocumentCommand(null, documentId, entityType, entityId, name, fileName, null, attachmentType,
                    description, null);
            return this.documentWritePlatformService.updateDocument(documentCommand, inputStream);

        } catch (ContentManagementException e) {
            log.warn("updateBase64Document Error: {}", e);
            throw new PlatformDataIntegrityException("error.document.base64.update", "Invalid update on documents uploaded");
        }
    }
}
