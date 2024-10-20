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
package org.apache.fineract.organisation.staff.service.business;

import com.google.gson.JsonElement;
import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepository;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.apache.fineract.organisation.staff.serialization.business.StaffBusinessCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffBusinessWritePlatformServiceJpaRepositoryImpl implements StaffBusinessWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(StaffBusinessWritePlatformServiceJpaRepositoryImpl.class);

    private final StaffBusinessCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final StaffRepository staffRepository;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final FromJsonHelper fromJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public StaffBusinessWritePlatformServiceJpaRepositoryImpl(final StaffBusinessCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final StaffRepository staffRepository, final OfficeRepositoryWrapper officeRepositoryWrapper,
            final FromJsonHelper fromJsonHelper, final CodeValueRepositoryWrapper codeValueRepositoryWrapper,
            final StaffRepositoryWrapper staffRepositoryWrapper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.staffRepository = staffRepository;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.fromJsonHelper = fromJsonHelper;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;
        this.staffRepositoryWrapper = staffRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createStaff(final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Long officeId = command.longValueOfParameterNamed("officeId");

            final Office staffOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
            final Staff staff = Staff.fromJson(staffOffice, command);

            final JsonElement jsonElement = this.fromJsonHelper.parse(command.json());
            if (this.fromJsonHelper.parameterExists(ClientBusinessApiConstants.emailAddressParamName, jsonElement)) {
                final String emailAddress = this.fromJsonHelper.extractStringNamed(ClientBusinessApiConstants.emailAddressParamName,
                        jsonElement);
                staff.setEmailAddress(emailAddress);
            }
            if (this.fromJsonHelper.parameterExists("organisationalRoleTypeId", jsonElement)) {
                final Long organisationalRoleTypeId = this.fromJsonHelper.extractLongNamed("organisationalRoleTypeId", jsonElement);
                this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(organisationalRoleTypeId);
                staff.setOrganisationalRoleType(organisationalRoleTypeId.intValue());
            }

            if (this.fromJsonHelper.parameterExists("organisationalRoleParentStaffId", jsonElement)) {
                final Long organisationalRoleParentStaffId = this.fromJsonHelper.extractLongNamed("organisationalRoleParentStaffId",
                        jsonElement);
                final Staff organisationalRoleParentStaff = this.staffRepositoryWrapper
                        .findOneWithNotFoundDetection(organisationalRoleParentStaffId);
                staff.setOrganisationalRoleParentStaff(organisationalRoleParentStaff);
            }

            this.staffRepository.saveAndFlush(staff);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(staff.getId()).withOfficeId(officeId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleStaffDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateStaff(final Long staffId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json(), staffId);

            final Staff staffForUpdate = this.staffRepository.findById(staffId).orElseThrow(() -> new StaffNotFoundException(staffId));
            final Map<String, Object> changesOnly = staffForUpdate.update(command);

            if (changesOnly.containsKey("officeId")) {
                final Long officeId = (Long) changesOnly.get("officeId");
                final Office newOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);
                staffForUpdate.changeOffice(newOffice);
            }

            if (command.isChangeInIntegerParameterNamed("organisationalRoleTypeId", staffForUpdate.getOrganisationalRoleType())) {
                final Long organisationalRoleTypeId = command.longValueOfParameterNamed("organisationalRoleTypeId");
                this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(organisationalRoleTypeId);
                staffForUpdate.setOrganisationalRoleType(organisationalRoleTypeId.intValue());
                changesOnly.put("organisationalRoleTypeId", organisationalRoleTypeId);
            }

            if (command.isChangeInIntegerParameterNamed("organisationalRoleParentStaffId", staffForUpdate.getOrganisationalRoleType())) {
                final Long organisationalRoleParentStaffId = command.longValueOfParameterNamed("organisationalRoleParentStaffId");
                final Staff organisationalRoleParentStaff = this.staffRepositoryWrapper
                        .findOneWithNotFoundDetection(organisationalRoleParentStaffId);
                staffForUpdate.setOrganisationalRoleParentStaff(organisationalRoleParentStaff);
                changesOnly.put("organisationalRoleParentStaffId", organisationalRoleParentStaffId);
            }

            if (!changesOnly.isEmpty()) {
                this.staffRepository.saveAndFlush(staffForUpdate);
            }

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(staffId)
                    .withOfficeId(staffForUpdate.officeId()).with(changesOnly).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleStaffDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleStaffDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleStaffDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.externalId",
                    "Staff with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("email_address")) {
            final String emailAddress = command.stringValueOfParameterNamed("emailAddress");
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.emailAddress",
                    "Staff with emailAddress `" + emailAddress + "` already exists", "emailAddress", emailAddress);
        } else if (realCause.getMessage().contains("display_name")) {
            final String lastname = command.stringValueOfParameterNamed("lastname");
            String displayName = lastname;
            if (!StringUtils.isBlank(displayName)) {
                final String firstname = command.stringValueOfParameterNamed("firstname");
                displayName = lastname + ", " + firstname;
            }
            throw new PlatformDataIntegrityException("error.msg.staff.duplicate.displayName",
                    "A staff with the given display name '" + displayName + "' already exists", "displayName", displayName);
        }

        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.staff.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource: " + realCause.getMessage());
    }
}
