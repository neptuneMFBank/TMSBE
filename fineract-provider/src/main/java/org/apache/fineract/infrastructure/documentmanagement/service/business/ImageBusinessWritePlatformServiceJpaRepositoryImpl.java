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

import com.google.gson.JsonElement;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepository;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryFactory;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.domain.Image;
import org.apache.fineract.infrastructure.documentmanagement.domain.ImageRepository;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;
import org.apache.fineract.infrastructure.documentmanagement.exception.InvalidEntityTypeForImageManagementException;
import org.apache.fineract.infrastructure.documentmanagement.serialization.business.DocumentBusinessDataValidator;
import org.apache.fineract.infrastructure.documentmanagement.service.ImageReadPlatformService;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ImageBusinessWritePlatformServiceJpaRepositoryImpl implements ImageBusinessWritePlatformService {

    private final ContentRepositoryFactory contentRepositoryFactory;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final ImageRepository imageRepository;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final DocumentBusinessDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final ImageReadPlatformService imageReadPlatformService;

    @Autowired
    public ImageBusinessWritePlatformServiceJpaRepositoryImpl(final ContentRepositoryFactory documentStoreFactory,
            final ClientRepositoryWrapper clientRepositoryWrapper, final ImageRepository imageRepository,
            final StaffRepositoryWrapper staffRepositoryWrapper, final DocumentBusinessDataValidator fromApiJsonDeserializer,
            final FromJsonHelper fromApiJsonHelper, final ImageReadPlatformService imageReadPlatformService) {
        this.contentRepositoryFactory = documentStoreFactory;
        this.clientRepositoryWrapper = clientRepositoryWrapper;
        this.imageRepository = imageRepository;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.imageReadPlatformService = imageReadPlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult saveOrUpdateImage(String entityName, final Long entityId, final String jsonRequestBody) {
        validateEntityTypeforImage(entityName);
        this.fromApiJsonDeserializer.validateForImage(entityName, entityId, jsonRequestBody);
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(jsonRequestBody);
        final String avatarBase64 = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.avatarBase64Param, jsonElement);
        // log.info("saveOrUpdateImage_avatarBase64: {}", jsonRequestBody);
        final Base64EncodedImage base64EncodedImage = ContentRepositoryUtils.extractImageFromDataURL(avatarBase64);
        try {
            Object owner = deletePreviousImage(entityName, entityId);

            final ContentRepository contenRepository = this.contentRepositoryFactory.getRepository();
            final String imageLocation = contenRepository.saveImage(base64EncodedImage, entityId, "image");
            return updateImage(owner, imageLocation, contenRepository.getStorageType());

        } catch (Exception e) {
            log.error("saveOrUpdateImage: {}", e);
            throw new ContentManagementException(entityName, "Image upload not successful");
        }
    }

    /**
     * @param entityName
     * @param entityId
     * @return
     */
    private Object deletePreviousImage(String entityName, final Long entityId) {
        Object owner = null;
        Image image = null;
        if (EntityTypeForImages.CLIENTS.toString().equals(entityName)) {
            Client client = this.clientRepositoryWrapper.findOneWithNotFoundDetection(entityId);
            image = client.getImage();
            owner = client;
        } else if (EntityTypeForImages.STAFF.toString().equals(entityName)) {
            Staff staff = this.staffRepositoryWrapper.findOneWithNotFoundDetection(entityId);
            image = staff.getImage();
            owner = staff;
        }
        if (image != null) {
            final ContentRepository contentRepository = this.contentRepositoryFactory
                    .getRepository(StorageType.fromInt(image.getStorageType()));
            contentRepository.deleteImage(image.getLocation());
        }
        return owner;
    }

    private CommandProcessingResult updateImage(final Object owner, final String imageLocation, final StorageType storageType) {
        Image image = null;
        Long clientId = null;
        if (owner instanceof Client client) {
            image = client.getImage();
            clientId = client.getId();
            image = createImage(image, imageLocation, storageType);
            client.setImage(image);
            this.clientRepositoryWrapper.save(client);
        } else if (owner instanceof Staff staff) {
            image = staff.getImage();
            clientId = staff.getId();
            image = createImage(image, imageLocation, storageType);
            staff.setImage(image);
            this.staffRepositoryWrapper.save(staff);
        }

        this.imageRepository.save(image);
        return new CommandProcessingResult(clientId);
    }

    private Image createImage(Image image, final String imageLocation, final StorageType storageType) {
        if (image == null) {
            image = new Image(imageLocation, storageType);
        } else {
            image.setLocation(imageLocation);
            image.setStorageType(storageType.getValue());
        }
        return image;
    }

    @Override
    public CommandProcessingResult retrieveImage(String entityName, Long entityId) {
        validateEntityTypeforImage(entityName);
        final FileData imageData = this.imageReadPlatformService.retrieveImage(entityName, entityId);
        try {
            // Else return response with Base64 encoded
            // TODO: Need a better way of determining image type
            String imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.JPEG.getValue();
            if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.GIF.getValue())) {
                imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.GIF.getValue();
            } else if (StringUtils.endsWith(imageData.name(), ContentRepositoryUtils.ImageFileExtension.PNG.getValue())) {
                imageDataURISuffix = ContentRepositoryUtils.ImageDataURIsuffix.PNG.getValue();
            }

            byte[] resizedImageBytes = imageData.getByteSource().read();
            if (resizedImageBytes != null) {
                final String imageAsBase64Text = imageDataURISuffix + Base64.getMimeEncoder().encodeToString(resizedImageBytes);
                return new CommandProcessingResultBuilder().withEntityId(entityId).withResourceIdAsString(imageAsBase64Text).build();
            } else {
                throw new ContentManagementException(imageData.name(), "Image not available.");
            }
        } catch (Exception e) {
            throw new ContentManagementException(imageData.name(), e.getMessage(), e);
        }
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
