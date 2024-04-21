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
package org.apache.fineract.portfolio.products.service.business;

import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.LOAN_PRODUCTS;
import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.SAVINGS_PRODUCTS;

import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.persistence.PersistenceException;
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
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductVisibilityMapping;
import org.apache.fineract.portfolio.products.exception.business.ProductVisibilityNotFoundException;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductVisibilityWriteService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.products.api.business.ProductVisibilityApiResourceConstants;
import org.apache.fineract.portfolio.products.data.business.ProductVisibilityDataValidator;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityClientclassificationMapping;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityClienttypeMapping;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityConfig;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityLegalenumMapping;
import org.apache.fineract.portfolio.products.domain.business.ProductVisibilityRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductVisibilityMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductVisibilityWriteServiceImpl implements LoanProductVisibilityWriteService {

    private final ProductVisibilityDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper;
    private final ProductVisibilityRepositoryWrapper repositoryWrapper;
    private final CodeValueRepositoryWrapper codeValueRepository;

    @Autowired
    public ProductVisibilityWriteServiceImpl(final ProductVisibilityDataValidator fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper,
            final PlatformSecurityContext context, final LoanProductRepositoryWrapper loanProductRepositoryWrapper,
            final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper, final ProductVisibilityRepositoryWrapper repositoryWrapper,
            final CodeValueRepositoryWrapper codeValueRepository) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.context = context;
        this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;
        this.savingsProductRepositoryWrapper = savingsProductRepositoryWrapper;
        this.repositoryWrapper = repositoryWrapper;
        this.codeValueRepository = codeValueRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createLoanProductVisibility(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), command.entityName());

        final String name = command.stringValueOfParameterNamed(ProductVisibilityApiResourceConstants.NAME);

        final String description = command.stringValueOfParameterNamed(ProductVisibilityApiResourceConstants.DESCRIPTION);

        final String[] loanProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LOANPRODUCT);
        List<LoanProduct> loanProductList = new ArrayList<>();

        if (loanProductArray != null && loanProductArray.length > 0) {
            for (String loanProductId : loanProductArray) {
                LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(loanProductId));
                loanProductList.add(loanProduct);
            }
        }

        final String[] savingsProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT);
        List<SavingsProduct> savingsProductList = new ArrayList<>();

        if (savingsProductArray != null && savingsProductArray.length > 0) {
            for (String savingsProductId : savingsProductArray) {
                SavingsProduct savingsProduct = this.savingsProductRepositoryWrapper
                        .findOneWithNotFoundDetection(Long.valueOf(savingsProductId));
                savingsProductList.add(savingsProduct);
            }
        }

        final String[] clientClassificationArray = command
                .arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
        List<CodeValue> clientClassificationList = new ArrayList<>();

        if (clientClassificationArray != null && clientClassificationArray.length > 0) {
            for (String clientClassificationId : clientClassificationArray) {
                CodeValue clientClassification = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_CLASSIFICATION, Long.valueOf(clientClassificationId));
                clientClassificationList.add(clientClassification);
            }
        }

        final String[] clientTypeArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTTYPE);
        List<CodeValue> clientTypeList = new ArrayList<>();

        if (clientTypeArray != null && clientTypeArray.length > 0) {
            for (String clientTypeId : clientTypeArray) {
                CodeValue clientType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                        Long.valueOf(clientTypeId));
                clientTypeList.add(clientType);
            }
        }

        final String[] legalEnumArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LEGALENUM);

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

        ProductVisibilityConfig newProductVisibilityConfig = null;
        try {
            if (command.entityName().equals(ProductVisibilityApiResourceConstants.LOAN_VISIBILITY_RESOURCENAME)) {
                newProductVisibilityConfig = ProductVisibilityConfig.createConfig(name, description, loanProductList,
                        clientClassificationList, clientTypeList, legalEnumList, LOAN_PRODUCTS.getValue(), null);
            } else if (command.entityName().equals(ProductVisibilityApiResourceConstants.SAVINGS_VISIBILITY_RESOURCENAME)) {
                newProductVisibilityConfig = ProductVisibilityConfig.createConfig(name, description, null, clientClassificationList,
                        clientTypeList, legalEnumList, SAVINGS_PRODUCTS.getValue(), savingsProductList);
            }
            this.repositoryWrapper.saveAndFlush(newProductVisibilityConfig);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newProductVisibilityConfig.getId())
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

        if (realCause.getMessage().contains("product_visibility_config_name'")) {

            final String name = command.stringValueOfParameterNamed(ProductVisibilityApiResourceConstants.NAME);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.name",
                    "A config with name `" + name + "` already exists", "name", name);
        } else if (realCause.getMessage().contains("config_id_loanproduct_id")) {
            final String[] loanProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LOANPRODUCT);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.loan.product",
                    "config  has duplicate loan product `" + Arrays.toString(loanProductArray), "Loan Product",
                    Arrays.toString(loanProductArray));
        } else if (realCause.getMessage().contains("config_id_clientclassification_id")) {
            final String[] clientClassificationArray = command
                    .arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.cleint.classifications",
                    "config  has duplicate client classification `" + Arrays.toString(clientClassificationArray), "clientClassifications",
                    Arrays.toString(clientClassificationArray));
        } else if (realCause.getMessage().contains("config_id_clienttypeid")) {
            final String[] clientTypeArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTTYPE);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.client.types",
                    "config  has duplicate client types `" + Arrays.toString(clientTypeArray), "clientTypes",
                    Arrays.toString(clientTypeArray));
        } else if (realCause.getMessage().contains("config_id_legalenum_id")) {
            final String[] legalEnumArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LEGALENUM);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.legal.enums",
                    "config  has duplicate legal enums `" + Arrays.toString(legalEnumArray), "legalEnums", Arrays.toString(legalEnumArray));
        } else if (realCause.getMessage().contains("config_id_savingsproduct_id")) {
            final String[] savingsProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT);
            throw new PlatformDataIntegrityException("error.msg.product.visibility.duplicate.savings.product",
                    "config  has duplicate savings product `" + Arrays.toString(savingsProductArray), "Savings Product",
                    Arrays.toString(savingsProductArray));
        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.product.visibility.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("ProductVisibilityErrorOccured: {}", dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProductVisibility(Long productVisibilityId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json(), command.entityName());
        ProductVisibilityConfig productVisibilityConfigForUpdate = this.repositoryWrapper.findOneWithNotFoundDetection(productVisibilityId);

        if (command.entityName().equals(ProductVisibilityApiResourceConstants.LOAN_VISIBILITY_RESOURCENAME)
                && !Objects.equals(productVisibilityConfigForUpdate.getProductType(), LOAN_PRODUCTS.getValue())) {
            throw new ProductVisibilityNotFoundException(LOAN_PRODUCTS.getCode() + " visibility with " + productVisibilityId + " does not exist");

        } else if (command.entityName().equals(ProductVisibilityApiResourceConstants.SAVINGS_VISIBILITY_RESOURCENAME)
                && !Objects.equals(productVisibilityConfigForUpdate.getProductType(), SAVINGS_PRODUCTS.getValue())) {
            throw new ProductVisibilityNotFoundException(SAVINGS_PRODUCTS.getCode() + " visibility with " + productVisibilityId + " does not exist");
        }

        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);

        if (command.isChangeInStringParameterNamed(ProductVisibilityApiResourceConstants.NAME,
                productVisibilityConfigForUpdate.getName())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.NAME, element);
            changes.put(ProductVisibilityApiResourceConstants.NAME, name);
            productVisibilityConfigForUpdate.setName(name);

        }
        if (command.isChangeInStringParameterNamed(ProductVisibilityApiResourceConstants.DESCRIPTION,
                productVisibilityConfigForUpdate.getDescription())) {
            final String description = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.DESCRIPTION,
                    element);
            changes.put(ProductVisibilityApiResourceConstants.DESCRIPTION, description);
            productVisibilityConfigForUpdate.setDescription(description);
        }

        boolean isClientClientClassificationUpdated = false;
        List<CodeValue> clientClassificationList = new ArrayList<>();

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            final String[] clientClassificationArray = command
                    .arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION);
            Set<Long> newClientClassificationIdList = new HashSet<>();

            if (clientClassificationArray != null && clientClassificationArray.length > 0) {
                for (String clientClassificationId : clientClassificationArray) {

                    CodeValue clientClassification = this.codeValueRepository.findOneWithNotFoundDetection(
                            Long.valueOf(clientClassificationId));

                    clientClassificationList.add(clientClassification);
                    newClientClassificationIdList.add(Long.valueOf(clientClassificationId));
                }
            }

            Set<Long> clientClassificationIdList = new HashSet<>();
            for (ProductVisibilityClientclassificationMapping currentSet : productVisibilityConfigForUpdate
                    .getProductVisibilityClientclassificationMapping()) {
                clientClassificationIdList.add(currentSet.getClientClassification().getId());
            }

            if (!clientClassificationIdList.equals(newClientClassificationIdList)) {
                isClientClientClassificationUpdated = true;
                productVisibilityConfigForUpdate.setProductVisibilityClientclassificationMapping(new ArrayList<>());
                changes.put(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, clientClassificationArray);
            }

        }
        boolean isClientClientTypeUpdated = false;
        List<CodeValue> clientTypeList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            final String[] clientTypeArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.CLIENTTYPE);
            Set<Long> newClientTypeIdList = new HashSet<>();

            if (clientTypeArray != null && clientTypeArray.length > 0) {
                for (String clientTypeId : clientTypeArray) {
                    CodeValue clientType = this.codeValueRepository.findOneWithNotFoundDetection(
                            Long.valueOf(clientTypeId));

                    clientTypeList.add(clientType);
                    newClientTypeIdList.add(Long.valueOf(clientTypeId));
                }
            }

            Set<Long> clientTypeIdList = new HashSet<>();
            for (ProductVisibilityClienttypeMapping currentSet : productVisibilityConfigForUpdate.getProductVisibilityClienttypeMapping()) {
                clientTypeIdList.add(currentSet.getClientType().getId());
            }

            if (!clientTypeIdList.equals(newClientTypeIdList)) {
                isClientClientTypeUpdated = true;
                productVisibilityConfigForUpdate.setProductVisibilityClienttypeMapping(new ArrayList<>());
                changes.put(ProductVisibilityApiResourceConstants.CLIENTTYPE, clientTypeArray);
            }

        }
        boolean isLegalEnumUpdated = false;
        List<Integer> legalEnumList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            final String[] legalEnumArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LEGALENUM);
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
            for (ProductVisibilityLegalenumMapping currentSet : productVisibilityConfigForUpdate.getProductVisibilityLegalenumMapping()) {
                legalEnumIdList.add(currentSet.getLegalEnum().longValue());
            }

            if (!legalEnumIdList.equals(newLegalEnumIdList)) {
                isLegalEnumUpdated = true;
                productVisibilityConfigForUpdate.setProductVisibilityLegalenumMapping(new ArrayList<>());
                changes.put(ProductVisibilityApiResourceConstants.LEGALENUM, legalEnumArray);
            }

        }
        boolean isLoanProductUpdated = false;
        List<LoanProduct> loanProductList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            final String[] loanProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.LOANPRODUCT);
            Set<Long> newLoanProductIdList = new HashSet<>();

            if (loanProductArray != null && loanProductArray.length > 0) {
                for (String loanProductId : loanProductArray) {
                    LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(loanProductId));
                    loanProductList.add(loanProduct);
                    newLoanProductIdList.add(Long.valueOf(loanProductId));
                }
            }

            Set<Long> loanProductIdList = new HashSet<>();
            for (LoanProductVisibilityMapping currentSet : productVisibilityConfigForUpdate.getLoanProductVisibilityMapping()) {
                loanProductIdList.add(currentSet.getLoanProduct().getId());
            }

            if (!loanProductIdList.equals(newLoanProductIdList)) {
                isLoanProductUpdated = true;
                productVisibilityConfigForUpdate.setLoanProductVisibilityMapping(new ArrayList<>());
                changes.put(ProductVisibilityApiResourceConstants.LOANPRODUCT, loanProductArray);
            }
        }

        boolean isSavingsProductUpdated = false;
        List<SavingsProduct> savingsProductList = new ArrayList<>();
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT, element)) {
            final String[] savingsProductArray = command.arrayValueOfParameterNamed(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT);
            Set<Long> newSavingsProductIdList = new HashSet<>();

            if (savingsProductArray != null && savingsProductArray.length > 0) {
                for (String savingsProductId : savingsProductArray) {
                    SavingsProduct savingsProduct = this.savingsProductRepositoryWrapper
                            .findOneWithNotFoundDetection(Long.valueOf(savingsProductId));
                    savingsProductList.add(savingsProduct);
                    newSavingsProductIdList.add(Long.valueOf(savingsProductId));
                }
            }

            Set<Long> savingsProductIdList = new HashSet<>();
            for (SavingsProductVisibilityMapping currentSet : productVisibilityConfigForUpdate.getSavingsProductVisibilityMapping()) {
                savingsProductIdList.add(currentSet.getSavingsProduct().getId());
            }

            if (!savingsProductIdList.equals(newSavingsProductIdList)) {
                isSavingsProductUpdated = true;
                productVisibilityConfigForUpdate.setSavingsProductVisibilityMapping(new ArrayList<>());
                changes.put(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT, savingsProductArray);
            }
        }

        try {
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(productVisibilityConfigForUpdate);

                if (isClientClientClassificationUpdated) {
                    productVisibilityConfigForUpdate.setProductVisibilityClientclassificationMapping(clientClassificationList);
                }
                if (isClientClientTypeUpdated) {
                    productVisibilityConfigForUpdate.setProductVisibilityClienttypeMapping(clientTypeList);
                }
                if (isLoanProductUpdated) {
                    productVisibilityConfigForUpdate.setLoanProductVisibilityMapping(loanProductList);
                }
                if (isLegalEnumUpdated) {
                    productVisibilityConfigForUpdate.setProductVisibilityLegalenumMapping(legalEnumList);
                }
                if (isSavingsProductUpdated) {
                    productVisibilityConfigForUpdate.setSavingsProductVisibilityMapping(savingsProductList);
                }
                if (isClientClientClassificationUpdated || isClientClientTypeUpdated || isLoanProductUpdated || isLegalEnumUpdated
                        || isSavingsProductUpdated) {
                    this.repositoryWrapper.saveAndFlush(productVisibilityConfigForUpdate);
                }
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(productVisibilityId)
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
    public CommandProcessingResult delete(final Long loanProductVisibilityId
    ) {

        try {
            ProductVisibilityConfig loanProductVisibilityConfig = this.repositoryWrapper
                    .findOneWithNotFoundDetection(loanProductVisibilityId);
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
