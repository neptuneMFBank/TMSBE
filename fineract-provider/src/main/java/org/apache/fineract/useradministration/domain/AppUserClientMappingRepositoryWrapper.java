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
package org.apache.fineract.useradministration.domain;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserClientMappingRepositoryWrapper {

    private final AppUserClientMappingRepository repository;

    @Autowired
    public AppUserClientMappingRepositoryWrapper(final AppUserClientMappingRepository repository) {
        this.repository = repository;
    }

    public List<AppUserClientMapping> findByClientId(long clientId) {
        return this.repository.findByClientId(clientId);
    }

    public List<Long> findAppUserByClientId(long clientId) {
        return this.repository.findAppUserByClientId(clientId);
    }

    public List<AppUserClientMapping> findAll() {
        return this.repository.findAll();
    }

    public void save(final AppUserClientMapping entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final AppUserClientMapping entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final AppUserClientMapping entity) {
        this.repository.delete(entity);
    }
}
