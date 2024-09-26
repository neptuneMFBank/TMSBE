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
package org.apache.fineract.portfolio.business.kyc.service;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigApiConstants;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigDataValidator;
import org.apache.fineract.portfolio.business.kyc.domain.KycConfig;
import org.apache.fineract.portfolio.business.kyc.domain.KycConfigMapping;
import org.apache.fineract.portfolio.business.kyc.domain.KycConfigRepository;
import org.apache.fineract.portfolio.business.kyc.domain.KycConfigRepositoryWrapper;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class KycConfigWriteServiceImpl implements KycConfigWriteService {

    private final KycConfigDataValidator fromApiJsonDataValidator;
    private final KycConfigRepository kycConfigRepository;
    private final KycConfigRepositoryWrapper kycConfigRepositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromApiJsonHelper;

    @Override
    @Transactional
    public CommandProcessingResult create(final JsonCommand command) {
        this.context.authenticatedUser();
        try {
            this.fromApiJsonDataValidator.validateForCreate(command.json());

            final String description = command.stringValueOfParameterNamed(KycConfigApiConstants.descriptionParamName);
            final Long kycLevelCodeValueId = command.longValueOfParameterNamed(KycConfigApiConstants.KycConfigCodeValueIdParamName);
            CodeValue kycLevelCodeValue = this.codeValueRepositoryWrapper
                    .findOneByCodeNameAndIdWithNotFoundDetection(KycConfigApiConstants.kycLevelParamName, kycLevelCodeValueId);

            final String[] KycParamArray = command.arrayValueOfParameterNamed(KycConfigApiConstants.KycParamCodeValueIdsParamName);
            List<CodeValue> kycParamList = new ArrayList<>();

            if (KycParamArray != null && KycParamArray.length > 0) {
                for (String kycParam : KycParamArray) {
                    CodeValue kycParamCodeValue = this.codeValueRepositoryWrapper
                            .findOneByCodeNameAndIdWithNotFoundDetection(KycConfigApiConstants.kycCodeParamName, Long.valueOf(kycParam));
                    kycParamList.add(kycParamCodeValue);
                }
            }

            final KycConfig kycConfig = KycConfig.createConfig(description, kycParamList, kycLevelCodeValue);
            this.kycConfigRepository.saveAndFlush(kycConfig);

            return new CommandProcessingResultBuilder() //
                    .withEntityId(kycConfig.getId()) //
                    .build();
        } catch (final DataAccessException e) {
            handleDataIntegrityIssues(command, e.getMostSpecificCause(), e);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            handleDataIntegrityIssues(command, ExceptionUtils.getRootCause(dve.getCause()), dve);
            return CommandProcessingResult.empty();
        }
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("unique_kyc_tier_cv_id'")) {

            final Long kycConfigCVId = command.longValueOfParameterNamed(KycConfigApiConstants.KycConfigCodeValueIdParamName);
            throw new PlatformDataIntegrityException("error.msg.kyc.config.duplicate.kyc",
                    "A config  `" + kycConfigCVId + "` already exists", "kyc", kycConfigCVId);
        } else if (realCause.getMessage().contains("unique_kyc_config_kyc_tier_param")) {
            final String[] kycParamArray = command.arrayValueOfParameterNamed(KycConfigApiConstants.KycParamCodeValueIdsParamName);
            throw new PlatformDataIntegrityException("error.msg.kyc.config.duplicate.kyc.param",
                    "config  has duplicate kyc params `" + Arrays.toString(kycParamArray), "kycParams", Arrays.toString(kycParamArray));
        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.kyc.config.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("kycConfigErrorOccured: {}", dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateKycConfig(Long kycConfigId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDataValidator.validateForUpdate(command.json());
        KycConfig kycConfigForUpdate = this.kycConfigRepositoryWrapper.findOneWithNotFoundDetection(kycConfigId);

        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>();

        if (command.isChangeInStringParameterNamed(KycConfigApiConstants.descriptionParamName, kycConfigForUpdate.getDescription())) {
            final String description = this.fromApiJsonHelper.extractStringNamed(KycConfigApiConstants.descriptionParamName, element);
            changes.put(KycConfigApiConstants.descriptionParamName, description);
            kycConfigForUpdate.setDescription(description);
        }

        boolean isKycParamUpdated = false;
        List<CodeValue> kycParamList = new ArrayList<>();

        if (this.fromApiJsonHelper.parameterExists(KycConfigApiConstants.KycParamCodeValueIdsParamName, element)) {
            final String[] kycParamArray = command.arrayValueOfParameterNamed(KycConfigApiConstants.KycParamCodeValueIdsParamName);
            Set<Long> newKycParamIdList = new HashSet<>();

            if (kycParamArray != null && kycParamArray.length > 0) {
                for (String kycParamId : kycParamArray) {
                    CodeValue kycParamCodeValue = this.codeValueRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(kycParamId));
                    kycParamList.add(kycParamCodeValue);
                    newKycParamIdList.add(Long.valueOf(kycParamId));
                }
            }

            Set<Long> kycParamIdList = new HashSet<>();
            for (KycConfigMapping currentSet : kycConfigForUpdate.getKycConfigMapping()) {
                kycParamIdList.add(currentSet.getKycTierParam().getId());
            }

            if (!kycParamIdList.equals(newKycParamIdList)) {
                isKycParamUpdated = true;
                kycConfigForUpdate.setKycConfigMapping(new HashSet<>());
                changes.put(KycConfigApiConstants.KycParamCodeValueIdsParamName, kycParamArray);
            }
        }

        try {
            if (!changes.isEmpty()) {
                this.kycConfigRepository.saveAndFlush(kycConfigForUpdate);
                if (isKycParamUpdated) {
                    kycConfigForUpdate.setKycConfigMapping(kycParamList);
                    this.kycConfigRepository.saveAndFlush(kycConfigForUpdate);
                }
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(kycConfigId).build();

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
    public CommandProcessingResult delete(final Long kycConfigId) {
        this.context.authenticatedUser();
        try {
            KycConfig kycConfig = this.kycConfigRepositoryWrapper.findOneWithNotFoundDetection(kycConfigId);
            this.kycConfigRepository.delete(kycConfig);
            this.kycConfigRepository.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(null, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder().withEntityId(kycConfigId).build();
    }
}
