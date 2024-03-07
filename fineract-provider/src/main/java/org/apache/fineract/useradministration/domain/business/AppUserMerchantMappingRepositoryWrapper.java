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
package org.apache.fineract.useradministration.domain.business;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserMerchantMappingRepositoryWrapper {

    private final AppUserMerchantMappingRepository repository;

    @Autowired
    public AppUserMerchantMappingRepositoryWrapper(final AppUserMerchantMappingRepository repository) {
        this.repository = repository;
    }

    public List<AppUserMerchantMapping> findByClientId(long clientId) {
        return this.repository.findByClientId(clientId);
    }

    public List<Long> findAppUserByClientId(long clientId) {
        return this.repository.findAppUserByClientId(clientId);
    }

    public List<AppUserMerchantMapping> findAll() {
        return this.repository.findAll();
    }

    public void save(final AppUserMerchantMapping entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final AppUserMerchantMapping entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final AppUserMerchantMapping entity) {
        this.repository.delete(entity);
    }
}
