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
package org.apache.fineract.infrastructure.documentmanagement.contentrepository.business;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.Base64EncodedImage;
import org.apache.fineract.infrastructure.documentmanagement.command.DocumentCommand;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepository;
import org.apache.fineract.infrastructure.documentmanagement.contentrepository.ContentRepositoryUtils;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.data.FileData;
import org.apache.fineract.infrastructure.documentmanagement.data.ImageData;
import org.apache.fineract.infrastructure.documentmanagement.domain.StorageType;
import org.apache.fineract.infrastructure.documentmanagement.exception.ContentManagementException;

@Slf4j
public class AzureContentRepository implements ContentRepository {

    private StorageSharedKeyCredential credential;
    private String endpoint;
    private BlobServiceClient storageClient;
    private BlobContainerClient blobContainerClient;
    private String containerName;

    public AzureContentRepository(final String accountKey, final String accountName, final String endpointSuffix, String containerName) {
        this.containerName = containerName;
        credential = new StorageSharedKeyCredential(accountName, accountKey);
        endpoint = String.format(Locale.ROOT, "https://%s.blob.%s", accountName, endpointSuffix);
        storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();
        blobContainerClient = storageClient.getBlobContainerClient(containerName);
        try {
            blobContainerClient.create();
        } catch (BlobStorageException error) {
            if (error.getErrorCode().equals(BlobErrorCode.CONTAINER_ALREADY_EXISTS)) {
                log.warn("Azure Can't create container. It already exists: {}", error);
            }
        }
    }

    @Override
    public String saveFile(InputStream uploadedInputStream, DocumentCommand documentCommand) {
        final String fileName = documentCommand.getFileName();
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(documentCommand.getSize(), fileName);

        final String uploadDocFolder = generateFileParentDirectory(documentCommand.getParentEntityType(),
                documentCommand.getParentEntityId());
        final String uploadDocFullPath = uploadDocFolder + File.separator + fileName;

        return putObject(fileName, uploadDocFullPath, uploadedInputStream);
    }

    protected String putObject(final String fileName, final String uploadDocFullPath, InputStream uploadedInputStream) {
        BlobClient blobClient = blobContainerClient.getBlobClient(uploadDocFullPath);
        try {
            try (uploadedInputStream) {
                byte[] data = uploadedInputStream.readAllBytes();
                blobClient.upload(new ByteArrayInputStream(data), data.length);
            }
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            log.error("Azure putObjectError: ", e);
            throw new ContentManagementException(fileName, e.getMessage());
        }
    }

    @Override
    public void deleteFile(String documentPath) {
        deleteObject(documentPath);
    }

    protected void deleteObject(String documentPath) {
        log.info("Azure deleteObjectPath: {}", documentPath);
        try {
            BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(getFileDirectoryFromLocation(documentPath))
                    .getBlockBlobClient();
            blockBlobClient.delete();
        } catch (Exception e) {
            log.error("Azure deleteObjectError: ", e);
            throw new ContentManagementException(documentPath, e.getMessage());
        }
    }

    @Override
    public FileData fetchFile(DocumentData documentData) {
        try {
            String filePath = getFileDirectoryFromLocation(documentData.fileLocation());
            log.info("Azure fetchFile: {}", filePath);
            BlockBlobClient blobClient = blobContainerClient.getBlobClient(filePath).getBlockBlobClient();
            int dataSize = (int) blobClient.getProperties().getBlobSize();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
            blobClient.download(outputStream);

            return new FileData(new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return new ByteArrayInputStream(outputStream.toByteArray());
                }
            }, documentData.fileName(), documentData.contentType());
        } catch (Exception e) {
            log.warn("Azure fetchFile Error: {}", e);
            return null;
        }
    }

    @Override
    public String saveImage(InputStream uploadedInputStream, Long resourceId, String imageName, Long fileSize) {
        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName;
        ContentRepositoryUtils.validateFileSizeWithinPermissibleRange(fileSize, imageName);

        return putObject(imageName, fileLocation, uploadedInputStream);
    }

    @Override
    public String saveImage(Base64EncodedImage base64EncodedImage, Long resourceId, String imageName) {

        final String uploadImageLocation = generateClientImageParentDirectory(resourceId);
        final String fileLocation = uploadImageLocation + File.separator + imageName + base64EncodedImage.getFileExtension();
        final InputStream toUploadInputStream = new ByteArrayInputStream(
                Base64.getMimeDecoder().decode(base64EncodedImage.getBase64EncodedString()));
        return putObject(imageName, fileLocation, toUploadInputStream);
    }

    @Override
    public void deleteImage(String documentPath) {
        deleteObject(documentPath);
    }

    @Override
    public FileData fetchImage(ImageData imageData) {
        try {
            BlockBlobClient blobClient = blobContainerClient.getBlobClient(getFileDirectoryFromLocation(imageData.location()))
                    .getBlockBlobClient();
            int dataSize = (int) blobClient.getProperties().getBlobSize();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
            blobClient.download(outputStream);

            return new FileData(new ByteSource() {

                @Override
                public InputStream openStream() throws IOException {
                    return new ByteArrayInputStream(outputStream.toByteArray());
                }
            }, imageData.getEntityDisplayName(), imageData.contentType().getValue());

        } catch (Exception e) {
            log.warn("Azure fetchImageError: {}",e);
            return null;
        }
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.AZURE;
    }

    private String generateFileParentDirectory(final String entityType, final Long entityId) {
        return "documents" + File.separator + entityType + File.separator + entityId + File.separator
                + ContentRepositoryUtils.generateRandomString();
    }

    private String generateClientImageParentDirectory(final Long resourceId) {
        return "images" + File.separator + "clients" + File.separator + resourceId + File.separator
                + ContentRepositoryUtils.generateRandomString();
    }

    private String getFileDirectoryFromLocation(String location) {
        return Iterables.get(Splitter.onPattern(containerName + "/").split(location), 1);
    }

}
