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

import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.apache.fineract.useradministration.service.AppUserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppUserRepositoryWrapper {

    private final AppUserRepository appUserRepository;

    @Autowired
    public AppUserRepositoryWrapper(final AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser fetchSystemUser() {
        AppUser user = this.appUserRepository.findAppUserByName(AppUserConstants.SYSTEM_USER_NAME);
        if (user == null) {
            throw new UserNotFoundException(AppUserConstants.SYSTEM_USER_NAME);
        }
        return user;
    }

    public AppUser findAppUserByName(final String username) {
        AppUser user = this.appUserRepository.findAppUserByName(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }

    public AppUser findAppUserByEmail(final String email) {
        AppUser user = this.appUserRepository.findAppUserByEmail(email);
        if (user == null) {
            throw new UserNotFoundException(email);
        }
        return user;
    }

    public AppUser findOneWithNotFoundDetection(final Long id) {
        return this.appUserRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public AppUser findFirstByStaffId(final Long staffId) {
        return this.appUserRepository.findFirstByStaffId(staffId).orElse(null);
        // .orElseThrow(() -> new UserNotFoundException("User with staffId " + staffId + " does not exits.", staffId));
    }
}
