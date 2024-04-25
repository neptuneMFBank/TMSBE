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

import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.service.PlatformEmailService;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class JpaUserDomainService implements UserDomainService {

    private final AppUserRepository userRepository;
    private final PlatformPasswordEncoder applicationPasswordEncoder;
    private final PlatformEmailService emailService;

    @Autowired
    public JpaUserDomainService(final AppUserRepository userRepository, final PlatformPasswordEncoder applicationPasswordEncoder,
            final PlatformEmailService emailService) {
        this.userRepository = userRepository;
        this.applicationPasswordEncoder = applicationPasswordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public void create(final AppUser appUser, final Boolean sendPasswordToEmail) {

        generateKeyUsedForPasswordSalting(appUser);

        final String unencodedPassword = appUser.getPassword();

        final String encodePassword = this.applicationPasswordEncoder.encode(appUser);
        final Long staffId = appUser.getStaffId();
        if (staffId != null) {
            final boolean firstTimeLoginRemaining = true;
            appUser.updatePassword(encodePassword, firstTimeLoginRemaining);
        } else {
            appUser.updatePassword(encodePassword);
        }

        this.userRepository.saveAndFlush(appUser);

        if (sendPasswordToEmail.booleanValue()) {
            this.emailService.sendToUserAccount(appUser.getOffice().getName(), appUser.getFirstname(), appUser.getEmail(),
                    appUser.getUsername(), unencodedPassword);
        }
    }

    @Transactional
    @Override
    public void createCustomer(final AppUser appUser, final boolean firstTimeLoginRemaining) {
        try {
            generateKeyUsedForPasswordSalting(appUser);

            final String encodePassword = this.applicationPasswordEncoder.encode(appUser);
            appUser.updatePassword(encodePassword, firstTimeLoginRemaining);

            this.userRepository.saveAndFlush(appUser);
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve);
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(throwable, dve);
        }
    }

    private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
        this.userRepository.save(appUser);
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final Throwable realCause, final Exception dve) {
        logAsErrorUnexpectedDataIntegrityException(dve);

        if (realCause.getMessage().contains("Duplicate")) {
            throw new PlatformDataIntegrityException("error.msg.client.duplicate", "Client already exists");
        }

        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occured.", dve);
    }
}
