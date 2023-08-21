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
package org.apache.fineract.infrastructure.documentmanagement.service.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.business.CodeRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.infrastructure.documentmanagement.serialization.business.DocumentConfigDataValidator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.business.ClientDocumentConfig;
import org.apache.fineract.portfolio.client.domain.business.ClientDocumentRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentConfigWriteServiceImpl implements DocumentConfigWriteService {

    private final PlatformSecurityContext context;
    private final DocumentConfigDataValidator fromApiJsonDeserializer;
    private final CodeRepositoryWrapper codeRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientDocumentRepositoryWrapper clientDocumentRepositoryWrapper;

    @Autowired
    public DocumentConfigWriteServiceImpl(final PlatformSecurityContext context,
            final DocumentConfigDataValidator fromApiJsonDeserializer,
            final FromJsonHelper fromApiJsonHelper,
            final ClientDocumentRepositoryWrapper clientDocumentRepositoryWrapper,
            final CodeRepositoryWrapper codeRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.clientDocumentRepositoryWrapper = clientDocumentRepositoryWrapper;
        this.codeRepository = codeRepository;
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @Override
    public CommandProcessingResult createDocumentConfig(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        try {
            final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());
            Long entityId;

            final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, jsonElement);
            final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam, jsonElement);
            final String typeParam = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, jsonElement);
            final JsonArray settings = this.fromApiJsonHelper.extractJsonArrayNamed(DocumentConfigApiConstants.settingsParam, jsonElement);
            Set<Code> codes = saveDocumentSet(settings);

            //client
            //loans
            if (is(typeParam, "client")) {

                final Integer formId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(DocumentConfigApiConstants.formIdParam, jsonElement);
                final ClientDocumentConfig clientDocumentConfig
                        = ClientDocumentConfig.instance(name, formId, description, true);
                if (!codes.isEmpty()) {
                    clientDocumentConfig.setCodes(codes);
                }
                this.clientDocumentRepositoryWrapper.saveAndFlush(clientDocumentConfig);
                entityId = clientDocumentConfig.getId();

            } //else  if (is(typeParam, "loans")) {
            //}
            else {
                throw new UnrecognizedQueryParamException("type", typeParam);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(entityId) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    protected Set<Code> saveDocumentSet(final JsonArray settings) {
        final Set<Code> codes = new HashSet<>();
        if (settings != null && !settings.isEmpty()) {
            for (JsonElement setting : settings) {
                final Long codeId = setting.getAsLong();
                final Code code = this.codeRepository.findOneWithNotFoundDetection(codeId);
                codes.add(code);
            }
        }
        return codes;
    }

    @Override
    public CommandProcessingResult updateDocumentConfig(Long entityId, JsonCommand command) {
        this.context.authenticatedUser();

        try {
            this.fromApiJsonDeserializer.validateForCreate(command.json());
            final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());

            final String typeParam = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, jsonElement);
            final JsonArray settings = this.fromApiJsonHelper.extractJsonArrayNamed(DocumentConfigApiConstants.settingsParam, jsonElement);
            Set<Code> codes = saveDocumentSet(settings);

            final Map<String, Object> changes = new LinkedHashMap<>(9);

            //client
            //loans
            if (is(typeParam, "client")) {
                final ClientDocumentConfig clientDocumentConfig
                        = this.clientDocumentRepositoryWrapper.findOneWithNotFoundDetection(entityId, typeParam);

                if (command.isChangeInStringParameterNamed(DocumentConfigApiConstants.nameParam, clientDocumentConfig.getName())) {
                    final String newValue = command.stringValueOfParameterNamed(DocumentConfigApiConstants.nameParam);
                    changes.put(DocumentConfigApiConstants.nameParam, newValue);
                    clientDocumentConfig.setName(newValue);
                }

                if (command.isChangeInStringParameterNamed(DocumentConfigApiConstants.descriptionParam, clientDocumentConfig.getName())) {
                    final String newValue = command.stringValueOfParameterNamed(DocumentConfigApiConstants.descriptionParam);
                    changes.put(DocumentConfigApiConstants.descriptionParam, newValue);
                    clientDocumentConfig.setDescription(newValue);
                }

                if (command.isChangeInIntegerParameterNamed(DocumentConfigApiConstants.formIdParam, clientDocumentConfig.getLegalFormId())) {
                    final Integer newValue = command.integerValueOfParameterNamed(DocumentConfigApiConstants.formIdParam);
                    changes.put(DocumentConfigApiConstants.formIdParam, newValue);
                    clientDocumentConfig.setLegalFormId(newValue);
                }

                Set<Code> codesCheck = clientDocumentConfig.getCodes();
                if (!codesCheck.equals(codes)) {
                    //only update if not equal
                    clientDocumentConfig.setCodes(codes);
                }

                if (!changes.isEmpty()) {
                    this.clientDocumentRepositoryWrapper.saveAndFlush(clientDocumentConfig);
                }
                entityId = clientDocumentConfig.getId();

            } //else  if (is(typeParam, "loans")) {
            //}
            else {
                throw new UnrecognizedQueryParamException("type", typeParam);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(entityId) //
                    .with(changes)
                    .build();
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

        if (realCause.getMessage().contains("name_UNIQUE")) {

            final String name = command.stringValueOfParameterNamed(DocumentConfigApiConstants.nameParam);
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId",
                    "Name `" + name + "` already exists", DocumentConfigApiConstants.nameParam, name);
        } else if (realCause.getMessage().contains("legal_form_id_UNIQUE")) {
            final String legalFormId = command.stringValueOfParameterNamed(DocumentConfigApiConstants.formIdParam);
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo",
                    "Form with `" + legalFormId + "` already exists", DocumentConfigApiConstants.formIdParam, legalFormId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occured.", dve);
    }

}
