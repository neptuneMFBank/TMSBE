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
package org.apache.fineract.organisation.business.businesstime.service;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.organisation.business.businesstime.api.BusinessTimeApiResourceConstants;
import org.apache.fineract.organisation.business.businesstime.data.BusinessTimeDataValidator;
import org.apache.fineract.organisation.business.businesstime.domain.BusinessTime;
import org.apache.fineract.organisation.business.businesstime.domain.BusinessTimeRepositoryWrapper;
import org.apache.fineract.organisation.business.businesstime.exception.BusinessTimeNotFoundException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.bankTransfer.service.TransferApprovalWritePlatformServiceJpaRepositoryImpl;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class BusinessTimeWritePlatformServiceImpl implements BusinessTimeWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(TransferApprovalWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final BusinessTimeRepositoryWrapper repository;
    private final BusinessTimeDataValidator fromApiJsonDataValidator;
    private final RoleRepository roleRepository;

    public BusinessTimeWritePlatformServiceImpl(final PlatformSecurityContext context, final BusinessTimeRepositoryWrapper repository,
            final BusinessTimeDataValidator fromApiJsonDataValidator, final RoleRepository roleRepository) {
        this.context = context;
        this.repository = repository;
        this.fromApiJsonDataValidator = fromApiJsonDataValidator;
        this.roleRepository = roleRepository;
    }

    @Override
    public CommandProcessingResult create(final JsonCommand command) {
        this.context.authenticatedUser();
        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final Long roleId = command.longValueOfParameterNamed(BusinessTimeApiResourceConstants.ROLE_ID);
            if (roleId != null) {
                roleRepository.findById(roleId);
            }
            final Integer weekDayId = command.integerValueOfParameterNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID);
            final LocalTime startTime = command.localTimeValueOfParameterNamed(BusinessTimeApiResourceConstants.START_TIME);
            final LocalTime endTime = command.localTimeValueOfParameterNamed(BusinessTimeApiResourceConstants.END_TIME);

            final BusinessTime businessTime = BusinessTime.instance(roleId, weekDayId, startTime, endTime);

            this.repository.saveAndFlush(businessTime);
            return new CommandProcessingResultBuilder() //
                    .withEntityId(businessTime.getId()) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult update(final JsonCommand command, final Long businessTimeId) {

        try {
            this.context.authenticatedUser();
            final BusinessTime businessTime = this.repository.findIdWithNotFoundDetection(businessTimeId);
            this.fromApiJsonDataValidator.validateForUpdate(command.json());

            final Map<String, Object> changes = businessTime.update(command);

            if (changes.containsKey(BusinessTimeApiResourceConstants.ROLE_ID)) {
                final Long roleId = command.longValueOfParameterNamed(BusinessTimeApiResourceConstants.ROLE_ID);
                roleRepository.findById(roleId);
            }

            if (!changes.isEmpty()) {
                this.repository.saveAndFlush(businessTime);
            }

            return new CommandProcessingResultBuilder() //
                    .withEntityId(businessTime.getId()) //
                    .with(changes).build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public CommandProcessingResult delete(final Long businessTimeId) {

        this.context.authenticatedUser();
        final BusinessTime businessTime = this.repository.findIdWithNotFoundDetection(businessTimeId);

        this.repository.delete(businessTime);

        return new CommandProcessingResultBuilder() //
                .withEntityId(businessTime.getId()) //
                .build();
    }

    @Override
    public CommandProcessingResult deleteByRole(final Long roleId) {

        this.context.authenticatedUser();
        final Collection<BusinessTime> businessTimes = this.repository.findByRoleId(roleId);
        if (businessTimes.isEmpty()) {
            throw new BusinessTimeNotFoundException("No business time for role with id " + roleId);
        }
        for (BusinessTime businessTime : businessTimes) {
            this.repository.delete(businessTime);
        }

        return new CommandProcessingResultBuilder() //
                .withEntityId(roleId) //
                .build();
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dae) {

        if (realCause.getMessage().contains("business_time_UNIQUE_role_weekday")) {

            final String roleId = command.stringValueOfParameterNamed(BusinessTimeApiResourceConstants.ROLE_ID);
            final String weekDayId = command.stringValueOfParameterNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID);
            throw new PlatformDataIntegrityException("error.msg.business.time.duplicate.role",
                    "Business time with role `" + roleId + "` and weekDayId `" + weekDayId + "` already exists ", "weekDayId", weekDayId);
        }
        logAsErrorUnexpectedDataIntegrityException(dae);
        throw new PlatformDataIntegrityException("error.msg.Business.Time.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dae) {
        LOG.error("Error occured.", dae);
    }
}
