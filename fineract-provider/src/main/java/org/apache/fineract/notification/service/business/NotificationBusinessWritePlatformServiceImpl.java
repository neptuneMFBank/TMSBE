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
package org.apache.fineract.notification.service.business;

import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.service.NotificationMapperReadRepositoryWrapperImpl;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationBusinessWritePlatformServiceImpl implements NotificationBusinessWritePlatformService {

    private final PlatformSecurityContext context;
    private final NotificationMapperReadRepositoryWrapperImpl notificationMapperReadRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public CommandProcessingResult updateNotificationReadStatus(final Long notificationId, final JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            final Long appUserId = currentUser.getId();
            if (notificationId == null) {
                String sql = "UPDATE notification_mapper SET is_read = true WHERE is_read = false and user_id = ?";
                this.jdbcTemplate.update(sql, appUserId);
            } else {
                this.notificationMapperReadRepositoryWrapper.findOneWithNotFoundDetection(notificationId);
                String sql = "UPDATE notification_mapper SET is_read = true WHERE is_read = false and user_id = ? and id = ?";
                this.jdbcTemplate.update(sql, appUserId, notificationId);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(notificationId) //
                    .withSubEntityId(appUserId)
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleOfficeDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleOfficeDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        log.error("Notification Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }
}
