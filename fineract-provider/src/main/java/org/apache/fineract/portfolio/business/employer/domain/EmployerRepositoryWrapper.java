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
package org.apache.fineract.portfolio.business.employer.domain;

import java.util.List;
import org.apache.fineract.portfolio.business.employer.exception.EmployerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployerRepositoryWrapper {

    private final EmployerRepository repository;

    @Autowired
    public EmployerRepositoryWrapper(final EmployerRepository repository) {
        this.repository = repository;
    }

    public void saveAndFlush(final Employer employer) {
        this.repository.saveAndFlush(employer);
    }

    public boolean existsByIdAndBusinessId(final Long id, final Long businessId) {
        return this.repository.existsByIdAndBusinessId(id, businessId);
    }

    @Transactional(readOnly = true)
    public Employer findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new EmployerNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Employer findByName(String fullName) {
        Employer employer = this.repository.findByName(fullName);
        if (employer == null) {
            throw new EmployerNotFoundException(fullName);
        }
        return employer;
    }

    // public List<Employer> findByParentId(final Long parentId) {
    // return this.repository.findByParentId(parentId);
    // }
    @Transactional(readOnly = true)
    public List<Employer> findByBusinessId(final Long businessId) {
        return this.repository.findByBusinessId(businessId);
    }

}
