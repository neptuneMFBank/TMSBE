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
package org.apache.fineract.portfolio.loanproduct.business.domain;

import org.apache.fineract.portfolio.loanproduct.business.exception.LoanProductVisibilityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductVisibilityRepositoryWrapper {

    private final LoanProductVisibilityRepository repository;

    @Autowired
    public LoanProductVisibilityRepositoryWrapper(final LoanProductVisibilityRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public void saveAndFlush(final LoanProductVisibilityConfig loanProductVisibilityConfig) {
        this.repository.saveAndFlush(loanProductVisibilityConfig);
    }

    @Transactional(readOnly = true)
    public LoanProductVisibilityConfig findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new LoanProductVisibilityNotFoundException(id));
    }

    @Transactional()
    public void delete(final LoanProductVisibilityConfig loanProductVisibilityConfig) {
        this.repository.delete(loanProductVisibilityConfig);
    }

    @Transactional()
    public void flush() {
        this.repository.flush();
    }
}
