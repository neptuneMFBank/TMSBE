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
package org.apache.fineract.portfolio.product.domain.business;

import java.util.List;
import org.apache.fineract.infrastructure.documentmanagement.exception.business.DocumentConfigNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link DocumentProductConfig} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class DocumentProductRepositoryWrapper {

    private final DocumentProductConfigRepository repository;

    @Autowired
    public DocumentProductRepositoryWrapper(final DocumentProductConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DocumentProductConfig findOneWithNotFoundDetection(final Long id// , final String type
    ) {
        return this.repository.findById(id).orElseThrow(() -> new DocumentConfigNotFoundException(// type,
                id));
    }

    @Transactional
    public DocumentProductConfig saveAndFlush(final DocumentProductConfig documentProductConfig) {
        return this.repository.saveAndFlush(documentProductConfig);
    }

    @Transactional
    public void saveAllAndFlush(final List<DocumentProductConfig> documentProductConfigs) {
        this.repository.saveAllAndFlush(documentProductConfigs);
    }

    @Transactional
    public DocumentProductConfig save(final DocumentProductConfig documentProductConfig) {
        return this.repository.save(documentProductConfig);
    }

    public void flush() {
        this.repository.flush();
    }

    public void delete(final Long documentProductConfigId) {
        this.repository.deleteById(documentProductConfigId);
    }

}
