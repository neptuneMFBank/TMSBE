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
package org.apache.fineract.portfolio.client.domain.business;

import org.apache.fineract.infrastructure.documentmanagement.exception.business.DocumentConfigNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link ClientDocumentConfig} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class ClientDocumentRepositoryWrapper {

    private final ClientDocumentConfigRepository repository;

    @Autowired
    public ClientDocumentRepositoryWrapper(final ClientDocumentConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ClientDocumentConfig findOneWithNotFoundDetection(final Long id//, final String type
    ) {
        return this.repository.findById(id).orElseThrow(() -> new DocumentConfigNotFoundException(//type,
                id));
    }

    @Transactional(readOnly = true)
    public ClientDocumentConfig findOneByLegalFormId(final Integer legalFormId) {
        return this.repository.findOneByLegalFormId(legalFormId).orElse(null);
    }

    @Transactional
    public ClientDocumentConfig saveAndFlush(final ClientDocumentConfig clientDocumentConfig) {
        return this.repository.saveAndFlush(clientDocumentConfig);
    }

    @Transactional
    public ClientDocumentConfig save(final ClientDocumentConfig clientDocumentConfig) {
        return this.repository.save(clientDocumentConfig);
    }

    public void flush() {
        this.repository.flush();
    }

    public void delete(final Long clientDocumentConfigId) {
        this.repository.deleteById(clientDocumentConfigId);
    }

}
