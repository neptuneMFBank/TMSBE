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
package org.apache.fineract.portfolio.client.data.business;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;

/**
 * Immutable data object represent client identity data.
 */
public class ClientIdentifierBusinessData {

    private final Long id;
    private final Long attachmentId;
    private final Long clientId;
    private final CodeValueData documentType;
    private final String documentKey;
    private final String description;
    private final String status;

    public static ClientIdentifierBusinessData singleItem(final Long id, final Long clientId, final CodeValueData documentType,
            final String documentKey, final String status, final String description, final Long attachmentId) {
        return new ClientIdentifierBusinessData(id, clientId, documentType, documentKey, description, status, attachmentId);
    }

    public static ClientIdentifierBusinessData template(final ClientIdentifierBusinessData data) {
        return new ClientIdentifierBusinessData(data.id, data.clientId, data.documentType, data.documentKey, data.description, data.status,
                data.attachmentId);
    }

    public ClientIdentifierBusinessData(final Long id, final Long clientId, final CodeValueData documentType, final String documentKey,
            final String description, final String status, final Long attachmentId) {
        this.id = id;
        this.attachmentId = attachmentId;
        this.clientId = clientId;
        this.documentType = documentType;
        this.documentKey = documentKey;
        this.description = description;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getAttachmentId() {
        return attachmentId;
    }

    public Long getClientId() {
        return clientId;
    }

    public CodeValueData getDocumentType() {
        return documentType;
    }

    public String getDocumentKey() {
        return documentKey;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

}
