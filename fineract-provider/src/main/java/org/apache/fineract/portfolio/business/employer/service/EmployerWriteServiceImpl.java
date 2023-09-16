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
package org.apache.fineract.portfolio.business.employer.service;

import com.google.gson.JsonElement;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.business.employer.api.EmployerApiResourceConstants;
import org.apache.fineract.portfolio.business.employer.data.EmployerDataValidator;
import org.apache.fineract.portfolio.business.employer.domain.Employer;
import org.apache.fineract.portfolio.business.employer.domain.EmployerRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployerWriteServiceImpl implements EmployerWriteService {

    private final EmployerRepositoryWrapper repositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final EmployerDataValidator fromApiJsonDeserializer;
    private final ClientRepositoryWrapper clientRepositoryWrapper;
    private final FromJsonHelper fromApiJsonHelper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final PlatformSecurityContext context;

    @Transactional
    @Override
    public CommandProcessingResult updateEmployer(Long employerId, JsonCommand command) {
        //log.info("updateEmployer: {}",command.json());
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), true);
        Employer employer = this.repositoryWrapper.findOneWithNotFoundDetection(employerId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);
        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.NAME, employer.getName())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NAME, element);
            changes.put(EmployerApiResourceConstants.NAME, name);
            employer.setName(name);
        }

        final Long oldClientClassificationId = employer.getClientClassification() != null ? employer.getClientClassification().getId()
                : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID, oldClientClassificationId)) {
            final Long clientClassificationId = this.fromApiJsonHelper
                    .extractLongNamed(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID, element);
            CodeValue clientClassification;
            if (clientClassificationId != null) {
                changes.put(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID, clientClassificationId);
                clientClassification = this.codeValueRepository.findOneWithNotFoundDetection(clientClassificationId);
                employer.setClientClassification(clientClassification);
            }
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.SLUG, employer.getSlug())) {
            final String slug = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SLUG, element);
            changes.put(EmployerApiResourceConstants.SLUG, slug);
            employer.setSlug(slug);
        }

        final Long oldStaffId = employer.getStaff() != null ? employer.getStaff().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.STAFF_ID, oldStaffId)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STAFF_ID, element);
            Staff staff;
            if (staffId != null) {
                staff = staffRepositoryWrapper.findOneWithNotFoundDetection(staffId);
                changes.put(EmployerApiResourceConstants.STAFF_ID, staffId);
                employer.setStaff(staff);
            }
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.RCNUMBER, employer.getRcNumber())) {
            final String RCNUMBER = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.RCNUMBER, element);
            changes.put(EmployerApiResourceConstants.RCNUMBER, RCNUMBER);
            employer.setRcNumber(RCNUMBER);
        }

        final Long oldStateId = employer.getState() != null ? employer.getState().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.STATEID, oldStateId)) {
            final Long stateId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element);
            CodeValue state;
            if (stateId != null) {
                state = this.codeValueRepository.findOneWithNotFoundDetection(stateId);
                changes.put(EmployerApiResourceConstants.STATEID, state);
                employer.setState(state);
            }
        }

        final Long oldLgaId = employer.getLga() != null ? employer.getLga().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.LGAID, oldLgaId)) {
            final Long lgaId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element);
            CodeValue lga;
            if (lgaId != null) {
                lga = this.codeValueRepository.findOneWithNotFoundDetection(lgaId);
                changes.put(EmployerApiResourceConstants.LGAID, lgaId);
                employer.setLga(lga);
            }
        }

        final Long oldCountryId = employer.getCountry() != null ? employer.getCountry().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.COUNTRYID, oldCountryId)) {
            CodeValue country;
            final Long countryId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.COUNTRYID, element);
            if (countryId != null) {
                country = this.codeValueRepository.findOneWithNotFoundDetection(countryId);
                changes.put(EmployerApiResourceConstants.COUNTRYID, countryId);
                employer.setCountry(country);
            }
        }

        final Long oldIndustryId = employer.getIndustry() != null ? employer.getIndustry().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.INDUSTRYID, oldIndustryId)) {
            final Long industryId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element);
            CodeValue industry;
            if (industryId != null) {
                industry = this.codeValueRepository.findOneWithNotFoundDetection(industryId);
                changes.put(EmployerApiResourceConstants.INDUSTRYID, industryId);
                employer.setIndustry(industry);
            }
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.OFFICEADDRESS, employer.getOfficeAddress())) {
            final String OFFICEADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.OFFICEADDRESS, element);
            changes.put(EmployerApiResourceConstants.OFFICEADDRESS, OFFICEADDRESS);
            employer.setOfficeAddress(OFFICEADDRESS);
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.NEARESTLANDMARK, employer.getNearestLandMark())) {
            final String NEARESTLANDMARK = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NEARESTLANDMARK, element);
            changes.put(EmployerApiResourceConstants.NEARESTLANDMARK, NEARESTLANDMARK);
            employer.setNearestLandMark(NEARESTLANDMARK);
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.MOBILE_NO, employer.getMobileNo())) {
            final String MOBILE_NO = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.MOBILE_NO, element);
            changes.put(EmployerApiResourceConstants.MOBILE_NO, MOBILE_NO);
            employer.setMobileNo(MOBILE_NO);
        }

        final Long oldBusinessId = employer.getBusiness() != null ? employer.getBusiness().getId() : null;
        if (command.isChangeInLongParameterNamed(EmployerApiResourceConstants.BUSINESSID, oldBusinessId)) {
            final Long businessId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element);
            Client business;
            if (businessId != null) {
                business = this.clientRepositoryWrapper.findOneWithNotFoundDetection(businessId);
                changes.put(EmployerApiResourceConstants.BUSINESSID, businessId);
                employer.setBusiness(business);
            }
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.EXTERNALID, employer.getExternalId())) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EXTERNALID, element);
            changes.put(EmployerApiResourceConstants.EXTERNALID, externalId);
            employer.setExternalId(externalId);
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.CONTACT_PERSON, employer.getContactPerson())) {
            final String CONTACT_PERSON = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.CONTACT_PERSON, element);
            changes.put(EmployerApiResourceConstants.CONTACT_PERSON, CONTACT_PERSON);
            employer.setContactPerson(CONTACT_PERSON);
        }
        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, employer.getEmailExtension())) {
            final String EMAIL_EXTENSION = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, element);
            changes.put(EmployerApiResourceConstants.EMAIL_EXTENSION, EMAIL_EXTENSION);
            employer.setEmailExtension(EMAIL_EXTENSION);
        }

        if (command.isChangeInStringParameterNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, employer.getEmailAddress())) {
            final String EMAIL_ADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, element);
            changes.put(EmployerApiResourceConstants.EMAIL_EXTENSION, EMAIL_ADDRESS);
            employer.setEmailAddress(EMAIL_ADDRESS);
        }

        try {
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(employer);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(employerId).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteEmployer(Long id) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
        // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Transactional
    @Override
    public CommandProcessingResult activateEmployer(Long entityId, JsonCommand command) {
        this.context.authenticatedUser();
        try {
            Employer employer = this.repositoryWrapper.findOneWithNotFoundDetection(entityId);
            employer.setActive(Boolean.TRUE);
            this.repositoryWrapper.saveAndFlush(employer);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(entityId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult closeEmployer(Long entityId, JsonCommand command) {
        this.context.authenticatedUser();
        try {
            Employer employer = this.repositoryWrapper.findOneWithNotFoundDetection(entityId);
            employer.setActive(Boolean.FALSE);
            this.repositoryWrapper.saveAndFlush(employer);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(entityId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult createEmployer(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), false);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final String name = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NAME, element);
        final Long clientClassificationId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID,
                element);
        CodeValue clientClassification = null;
        if (clientClassificationId != null) {
            clientClassification = this.codeValueRepository.findOneWithNotFoundDetection(clientClassificationId);
        }

        final String slug = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SLUG, element);

        final Long staffId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STAFF_ID, element);
        Staff staff = null;
        if (staffId != null) {
            staff = staffRepositoryWrapper.findOneWithNotFoundDetection(staffId);
        }
        final String RCNUMBER = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.RCNUMBER, element);

        final Long stateId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element);
        CodeValue state = null;
        if (stateId != null) {
            state = this.codeValueRepository.findOneWithNotFoundDetection(stateId);
        }

        final Long lgaId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element);
        CodeValue lga = null;
        if (lgaId != null) {
            lga = this.codeValueRepository.findOneWithNotFoundDetection(lgaId);
        }
        CodeValue country = null;
        final Long countryId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.COUNTRYID, element);
        if (countryId != null) {
            country = this.codeValueRepository.findOneWithNotFoundDetection(countryId);
        }

        final Long industryId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element);
        CodeValue industry = null;
        if (industryId != null) {
            industry = this.codeValueRepository.findOneWithNotFoundDetection(industryId);
        }

        final String OFFICEADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.OFFICEADDRESS, element);

        final String NEARESTLANDMARK = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NEARESTLANDMARK, element);

        final String MOBILE_NO = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.MOBILE_NO, element);

        final Long businessId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element);
        Client business = null;
        if (businessId != null) {
            business = this.clientRepositoryWrapper.findOneWithNotFoundDetection(businessId);
        }

        final String externalId = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EXTERNALID, element);
        final String CONTACT_PERSON = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.CONTACT_PERSON, element);
        final String EMAIL_EXTENSION = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, element);

        final String EMAIL_ADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, element);

        Boolean active = false;
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.ACTIVE, element)) {
            active = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.ACTIVE, element);
        }

        try {
            Employer newEmployer = Employer.create(clientClassification, externalId, name, slug, MOBILE_NO, EMAIL_ADDRESS, EMAIL_EXTENSION,
                    CONTACT_PERSON, RCNUMBER, industry, business, state, country, lga, OFFICEADDRESS, NEARESTLANDMARK, active, staff);
            this.repositoryWrapper.saveAndFlush(newEmployer);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newEmployer.getId()).build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        String[] cause = StringUtils.split(realCause.getMessage(), "'");

        String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
        if (getCause.contains("name")) {
            final String name = command.stringValueOfParameterNamed(EmployerApiResourceConstants.NAME);
            throw new PlatformDataIntegrityException("error.msg.employer.duplicate", "Employer with name `" + name + "` already exists",
                    EmployerApiResourceConstants.NAME, name);
        } else if (getCause.contains("external_id")) {
            final String externalId = command.stringValueOfParameterNamed(EmployerApiResourceConstants.EXTERNALID);
            throw new PlatformDataIntegrityException("error.msg.employer.duplicate",
                    "Employer with externalId `" + externalId + "` already exists", EmployerApiResourceConstants.EXTERNALID, externalId);
        } else if (getCause.contains("rc_number")) {
            final String rcNumber = command.stringValueOfParameterNamed(EmployerApiResourceConstants.RCNUMBER);
            throw new PlatformDataIntegrityException("error.msg.employer.duplicate.mobileNo",
                    "Employer with registration `" + rcNumber + "` already exists", EmployerApiResourceConstants.RCNUMBER, rcNumber);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.employer.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("EmployerErrorOccured: {}", dve);
    }

}
