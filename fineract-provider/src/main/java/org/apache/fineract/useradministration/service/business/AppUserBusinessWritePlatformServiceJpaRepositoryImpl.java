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
package org.apache.fineract.useradministration.service.business;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.apache.fineract.useradministration.service.AppUserWritePlatformService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppUserBusinessWritePlatformServiceJpaRepositoryImpl implements AppUserBusinessWritePlatformService {

    private final PlatformSecurityContext context;
    private final AppUserWritePlatformService appUserWritePlatformService;
    private final UserBusinessDataValidator fromApiJsonDeserializer;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "usersBusiness", allEntries = true),
        @CacheEvict(value = "usersBusinessPasswordByUsername", allEntries = true)})
    public CommandProcessingResult updateUserPassword(final JsonCommand command) {
        final AppUser appUser = this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdatePassword(command.json());
        final Long userId = appUser.getId();
        return appUserWritePlatformService.updateUser(userId, command);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "usersBusiness", allEntries = true),
        @CacheEvict(value = "usersBusinessInfoByUsername", allEntries = true)})
    public CommandProcessingResult updateUserInfo(final Long userId, final JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        return appUserWritePlatformService.updateUser(userId, command);
    }

    @Transactional
    @Override
    public CommandProcessingResult enableUser(Long userId) {
        final AppUser appUser = this.context.authenticatedUser();
        rejectActionOnSelfUser(appUser, userId);
        final AppUser userToUpdate = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        try {
            userToUpdate.enableUser();
            this.appUserRepository.saveAndFlush(userToUpdate);
            return new CommandProcessingResultBuilder().withEntityId(userId).build();
        } catch (final Exception e) {
            log.warn("enableUser: {}", e);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "User not Enabled.");
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult disableUser(Long userId) {
        final AppUser appUser = this.context.authenticatedUser();
        rejectActionOnSelfUser(appUser, userId);
        final AppUser userToUpdate = this.appUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        try {
            userToUpdate.disableUser();
            this.appUserRepository.saveAndFlush(userToUpdate);
            return new CommandProcessingResultBuilder().withEntityId(userId).build();
        } catch (final Exception e) {
            log.warn("disableUser: {}", e);
            throw new PlatformDataIntegrityException("error.msg.unknown.data.integrity.issue",
                    "User not Disabled.");
        }
    }

    protected void rejectActionOnSelfUser(final AppUser appUser, Long userId) throws UserNotFoundException {
        if (Objects.equals(appUser.getId(), userId) && appUser.isSelfServiceUser() == false) {
            throw new UserNotFoundException("User cannot perform action on self.", userId);
        }
    }

}
