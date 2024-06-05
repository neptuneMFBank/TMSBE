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
package org.apache.fineract.organisation.business.businesstime.domain;

import java.util.Collection;
import org.apache.fineract.organisation.business.businesstime.exception.BusinessTimeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusinessTimeRepositoryWrapper {

    private final BusinessTimeRepository repository;

    @Autowired
    public BusinessTimeRepositoryWrapper(final BusinessTimeRepository repository) {
        this.repository = repository;
    }

    public BusinessTime findByRoleIdAndWeekDayId(Long roleId, Integer weekDayId) {
        return this.repository.findByRoleIdAndWeekDayId(roleId, weekDayId);
    }

    public Collection<BusinessTime> findByRoleId(Long roleId) {
        return this.repository.findByRoleId(roleId);
    }

    public BusinessTime findIdWithNotFoundDetection(Long businessTimeId) {
        return this.repository.findById(businessTimeId).orElseThrow(() -> new BusinessTimeNotFoundException(businessTimeId));
    }

    public void saveAndFlush(BusinessTime businessTime) {
        this.repository.saveAndFlush(businessTime);
    }

    public void delete(BusinessTime businessTime) {
        this.repository.delete(businessTime);
    }
}
