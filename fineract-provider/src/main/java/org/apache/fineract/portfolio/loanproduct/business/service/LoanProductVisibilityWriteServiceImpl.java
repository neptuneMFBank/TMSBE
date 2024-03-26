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
package org.apache.fineract.portfolio.loanproduct.business.service;

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
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductVisibilityApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductVisibilityDataValidator;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductVisibilityConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductVisibilityMapping;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductVisibilityRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanproductVisibilityClientclassificationMapping;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanproductVisibilityClienttypeMapping;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanproductVisibilityLegalenumMapping;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductVisibilityWriteServiceImpl implements LoanProductVisibilityWriteService {

    private final LoanProductVisibilityDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final LoanProductVisibilityRepositoryWrapper repositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Transactional
    @Override
    public CommandProcessingResult createLoanProductVisibility(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());

        final String name = command.stringValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.NAME);;
        final String description = command.stringValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.DESCRIPTION);

        final String[] loanProductArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LOANPRODUCT);
        List<LoanProduct> loanProductList = new ArrayList<>();

        if (loanProductArray != null && loanProductArray.length > 0) {
            for (String loanProductId : loanProductArray) {
                LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(loanProductId));
                loanProductList.add(loanProduct);
            }
        }

        final String[] clientClassificationArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
        List<CodeValue> clientClassificationList = new ArrayList<>();

        if (clientClassificationArray != null && clientClassificationArray.length > 0) {
            for (String clientClassificationId : clientClassificationArray) {
                CodeValue clientClassification = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLASSIFICATION,
                        Long.valueOf(clientClassificationId));
                clientClassificationList.add(clientClassification);
            }
        }

        final String[] clientTypeArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTTYPE);
        List<CodeValue> clientTypeList = new ArrayList<>();

        if (clientTypeArray != null && clientTypeArray.length > 0) {
            for (String clientTypeId : clientTypeArray) {
                CodeValue clientType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                        Long.valueOf(clientTypeId));
                clientTypeList.add(clientType);
            }
        }

        final String[] legalEnumArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LEGALENUM);

        List<Integer> legalEnumList = new ArrayList<>();

        if (legalEnumArray != null && legalEnumArray.length > 0) {
            for (String legalEnum : legalEnumArray) {
                Integer legalFormValue = null;
                LegalForm legalForm = LegalForm.fromInt(Integer.valueOf(legalEnum));
                if (legalForm != null) {
                    legalFormValue = legalForm.getValue();
                    legalEnumList.add(legalFormValue);
                }
            }
        }

        try {
            final LoanProductVisibilityConfig newLoanProductVisibilityConfig = LoanProductVisibilityConfig.createConfig(name, description, loanProductList, clientClassificationList, clientTypeList, legalEnumList);

            this.repositoryWrapper.saveAndFlush(newLoanProductVisibilityConfig);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newLoanProductVisibilityConfig.getId())
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

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("loanproduct_visibility_config_name")) {

            final String name = command.stringValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.NAME);
            throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.duplicate.name",
                    "A config with name `" + name + "` already exists", "name", name);
        } else if (realCause.getMessage().contains("config_id_loanproduct_id")) {
            final String[] loanProductArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LOANPRODUCT);
            throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.duplicate.loan.product",
                    "config  has duplicate loan product `" + Arrays.toString(loanProductArray), "Loan Product", Arrays.toString(loanProductArray));
        } else if (realCause.getMessage().contains("config_id_clientclassification_id")) {
            final String[] clientClassificationArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
            throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.duplicate.cleint.classifications",
                    "config  has duplicate client classification `" + Arrays.toString(clientClassificationArray), "clientClassifications", Arrays.toString(clientClassificationArray));
        } else if (realCause.getMessage().contains("config_id_clienttypeid")) {
            final String[] clientTypeArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTTYPE);
            throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.duplicate.client.types",
                    "config  has duplicate client types `" + Arrays.toString(clientTypeArray), "clientTypes", Arrays.toString(clientTypeArray));
        } else if (realCause.getMessage().contains("config_id_legalenum_id")) {
            final String[] legalEnumArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LEGALENUM);
            throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.duplicate.legal.enums",
                    "config  has duplicate legal enums `" + Arrays.toString(legalEnumArray), "legalEnums", Arrays.toString(legalEnumArray));
        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loan.product.visibility.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("LoanProductVisibilityErrorOccured: {}", dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProductVisibility(Long loanProductVisibilityId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json());

        LoanProductVisibilityConfig loanProductVisibilityConfigForUpdate = this.repositoryWrapper.findOneWithNotFoundDetection(loanProductVisibilityId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(LoanProductVisibilityApiResourceConstants.NAME, loanProductVisibilityConfigForUpdate.getDescription())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductVisibilityApiResourceConstants.NAME, element);
            changes.put(LoanProductVisibilityApiResourceConstants.NAME, name);
            loanProductVisibilityConfigForUpdate.setName(name);

        }
        if (command.isChangeInStringParameterNamed(LoanProductVisibilityApiResourceConstants.DESCRIPTION, loanProductVisibilityConfigForUpdate.getDescription())) {
            final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductVisibilityApiResourceConstants.DESCRIPTION, element);
            changes.put(LoanProductVisibilityApiResourceConstants.DESCRIPTION, description);
            loanProductVisibilityConfigForUpdate.setDescription(description);
        }

        boolean isClientClientClassificationUpdated = false;
        List<CodeValue> clientClassificationList = new ArrayList<>();

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            final String[] clientClassificationArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
            Set<Long> newClientClassificationIdList = new HashSet<>();

            if (clientClassificationArray != null && clientClassificationArray.length > 0) {
                for (String clientClassificationId : clientClassificationArray) {
                    CodeValue clientClassification = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION,
                            Long.valueOf(clientClassificationId));
                    clientClassificationList.add(clientClassification);
                    newClientClassificationIdList.add(Long.valueOf(clientClassificationId));
                }
            }

            Set<Long> clientClassificationIdList = new HashSet<>();
            for (LoanproductVisibilityClientclassificationMapping currentSet : loanProductVisibilityConfigForUpdate.getLoanproductVisibilityClientclassificationMapping()) {
                clientClassificationIdList.add(currentSet.getClientClassification().getId());
            }

            if (!clientClassificationIdList.equals(newClientClassificationIdList)) {
                isClientClientClassificationUpdated = true;
                loanProductVisibilityConfigForUpdate.setLoanproductVisibilityClientclassificationMapping(new ArrayList<>());
                changes.put(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, clientClassificationArray);
            }

        }
        boolean isClientClientTypeUpdated = false;
        List<CodeValue> clientTypeList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            final String[] clientTypeArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.CLIENTTYPE);
            Set<Long> newClientTypeIdList = new HashSet<>();

            if (clientTypeArray != null && clientTypeArray.length > 0) {
                for (String clientTypeId : clientTypeArray) {
                    CodeValue clientType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(LoanProductVisibilityApiResourceConstants.CLIENTTYPE,
                            Long.valueOf(clientTypeId));
                    clientTypeList.add(clientType);
                    newClientTypeIdList.add(Long.valueOf(clientTypeId));
                }
            }

            Set<Long> clientTypeIdList = new HashSet<>();
            for (LoanproductVisibilityClienttypeMapping currentSet : loanProductVisibilityConfigForUpdate.getLoanproductVisibilityClienttypeMapping()) {
                clientTypeIdList.add(currentSet.getClientType().getId());
            }

            if (!clientTypeIdList.equals(newClientTypeIdList)) {
                isClientClientTypeUpdated = true;
                loanProductVisibilityConfigForUpdate.setLoanproductVisibilityClienttypeMapping(new ArrayList<>());
                changes.put(LoanProductVisibilityApiResourceConstants.CLIENTTYPE, clientTypeArray);
            }

        }
        boolean isLegalEnumUpdated = false;
        List<Integer> legalEnumList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            final String[] legalEnumArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LEGALENUM);
            Set<Long> newLegalEnumIdList = new HashSet<>();

            if (legalEnumArray != null && legalEnumArray.length > 0) {
                for (String legalEnum : legalEnumArray) {
                    Integer legalFormValue = null;
                    LegalForm legalForm = LegalForm.fromInt(Integer.valueOf(legalEnum));
                    if (legalForm != null) {
                        legalFormValue = legalForm.getValue();

                        legalEnumList.add(legalFormValue);
                        newLegalEnumIdList.add(Long.valueOf(legalFormValue));
                    }
                }
            }
            Set<Long> legalEnumIdList = new HashSet<>();
            for (LoanproductVisibilityLegalenumMapping currentSet : loanProductVisibilityConfigForUpdate.getLoanproductVisibilityLegalenumMapping()) {
                legalEnumIdList.add(currentSet.getLegalEnum().longValue());
            }

            if (!legalEnumIdList.equals(newLegalEnumIdList)) {
                isLegalEnumUpdated = true;
                loanProductVisibilityConfigForUpdate.setLoanproductVisibilityLegalenumMapping(new ArrayList<>());
                changes.put(LoanProductVisibilityApiResourceConstants.LEGALENUM, legalEnumArray);
            }

        }
        boolean isLoanProductUpdated = false;
        List<LoanProduct> loanProductList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            final String[] loanProductArray = command.arrayValueOfParameterNamed(LoanProductVisibilityApiResourceConstants.LOANPRODUCT);
            Set<Long> newLoanProductIdList = new HashSet<>();

            if (loanProductArray != null && loanProductArray.length > 0) {
                for (String loanProductId : loanProductArray) {
                    LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(loanProductId));
                    loanProductList.add(loanProduct);
                    newLoanProductIdList.add(Long.valueOf(loanProductId));
                }
            }

            Set<Long> loanProductIdList = new HashSet<>();
            for (LoanProductVisibilityMapping currentSet : loanProductVisibilityConfigForUpdate.getLoanProductVisibilityMapping()) {
                loanProductIdList.add(currentSet.getLoanProduct().getId());
            }

            if (!loanProductIdList.equals(newLoanProductIdList)) {
                isLoanProductUpdated = true;
                loanProductVisibilityConfigForUpdate.setLoanProductVisibilityMapping(new ArrayList<>());
                changes.put(LoanProductVisibilityApiResourceConstants.LOANPRODUCT, loanProductArray);
            }
        }
        try {
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(loanProductVisibilityConfigForUpdate);
            }
            if (isClientClientClassificationUpdated) {
                loanProductVisibilityConfigForUpdate
                        .setLoanproductVisibilityClientclassificationMapping(clientClassificationList);
            }
            if (isClientClientTypeUpdated) {
                loanProductVisibilityConfigForUpdate
                        .setLoanproductVisibilityClienttypeMapping(clientTypeList);
            }
            if (isLoanProductUpdated) {
                loanProductVisibilityConfigForUpdate
                        .setLoanProductVisibilityMapping(loanProductList);
            }
            if (isLegalEnumUpdated) {
                loanProductVisibilityConfigForUpdate
                        .setLoanproductVisibilityLegalenumMapping(legalEnumList);
            }
            if (isClientClientClassificationUpdated || isClientClientTypeUpdated || isLoanProductUpdated || isLegalEnumUpdated) {
                this.repositoryWrapper.saveAndFlush(loanProductVisibilityConfigForUpdate);

            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(loanProductVisibilityId)
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

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long loanProductVisibilityId) {

        try {
            LoanProductVisibilityConfig loanProductVisibilityConfig = this.repositoryWrapper.findOneWithNotFoundDetection(loanProductVisibilityId);
            this.repositoryWrapper.delete(loanProductVisibilityConfig);
            this.repositoryWrapper.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(null, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder().withEntityId(loanProductVisibilityId).build();
    }
}
