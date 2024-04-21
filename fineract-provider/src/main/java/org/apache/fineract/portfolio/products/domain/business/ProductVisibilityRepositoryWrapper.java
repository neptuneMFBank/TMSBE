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
package org.apache.fineract.portfolio.products.domain.business;

import org.apache.fineract.portfolio.products.exception.business.ProductVisibilityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductVisibilityRepositoryWrapper {

    private final ProductVisibilityRepository repository;

    @Autowired
    public ProductVisibilityRepositoryWrapper(final ProductVisibilityRepository repository) {
        this.repository = repository;
    }

    @Transactional()
    public void saveAndFlush(final ProductVisibilityConfig loanProductVisibilityConfig) {
        this.repository.saveAndFlush(loanProductVisibilityConfig);
    }

     @Transactional()
    public void save(final ProductVisibilityConfig loanProductVisibilityConfig) {
        this.repository.save(loanProductVisibilityConfig);
    }

    @Transactional(readOnly = true)
    public ProductVisibilityConfig findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new ProductVisibilityNotFoundException(id));
    }

    @Transactional()
    public void delete(final ProductVisibilityConfig loanProductVisibilityConfig) {
        this.repository.delete(loanProductVisibilityConfig);
    }

    @Transactional()
    public void flush() {
        this.repository.flush();
    }
}
