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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductPaymentTypeConfigConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductPaymentTypeConfigDataValidator;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductPaymentTypeConfig;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductPaymentTypeConfigRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductPaymentTypeConfigWriteServiceImpl implements LoanProductPaymentTypeConfigWriteService {

    private final LoanProductPaymentTypeConfigRepositoryWrapper repositoryWrapper;
    private final LoanProductPaymentTypeConfigDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;
    private final LoanProductRepositoryWrapper loanProductRepository;

    @Transactional
    @Override
    public CommandProcessingResult createLoanProductPaymentTypeConfig(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        try {
            final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());

            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductPaymentTypeConfigConstants.NAME, jsonElement);
            final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductPaymentTypeConfigConstants.description,
                    jsonElement);
            final Long productId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, jsonElement);
            final LoanProduct loanProduct = this.loanProductRepository.findOneWithNotFoundDetection(productId);

            final JsonArray paymentTypeIds = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductPaymentTypeConfigConstants.paymentTypeIds, jsonElement);
            Set<PaymentType> paymentTypes = savePaymentSet(paymentTypeIds);

            final LoanProductPaymentTypeConfig loanProductPaymentTypeConfig = LoanProductPaymentTypeConfig.instance(name, loanProduct,
                    description, true);
            if (!paymentTypes.isEmpty()) {
                loanProductPaymentTypeConfig.setPaymentTypes(paymentTypes);
            }
            this.repositoryWrapper.saveAndFlush(loanProductPaymentTypeConfig);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(loanProductPaymentTypeConfig.getId()) //
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
    public CommandProcessingResult updateLoanProductPaymentTypeConfig(Long loanProductPaymentId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForUpdate(command.json());
        LoanProductPaymentTypeConfig loanProductPaymentTypeConfig = this.repositoryWrapper
                .findOneWithNotFoundDetection(loanProductPaymentId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);
        if (command.isChangeInStringParameterNamed(LoanProductPaymentTypeConfigConstants.NAME, loanProductPaymentTypeConfig.getName())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductPaymentTypeConfigConstants.NAME, element);
            changes.put(LoanProductPaymentTypeConfigConstants.NAME, name);
            loanProductPaymentTypeConfig.setName(name);
        }

        if (command.isChangeInStringParameterNamed(LoanProductPaymentTypeConfigConstants.description,
                loanProductPaymentTypeConfig.getDescription())) {
            final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductPaymentTypeConfigConstants.description,
                    element);
            changes.put(LoanProductPaymentTypeConfigConstants.description, description);
            loanProductPaymentTypeConfig.setDescription(description);
        }

        final Long oldLoanProductId = loanProductPaymentTypeConfig.getLoanProduct() != null
                ? loanProductPaymentTypeConfig.getLoanProduct().getId()
                : null;
        if (command.isChangeInLongParameterNamed(LoanApiConstants.productIdParameterName, oldLoanProductId)) {
            final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, element);
            LoanProduct loanProduct;
            if (loanProductId != null) {
                loanProduct = loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
                changes.put(LoanApiConstants.productIdParameterName, loanProductId);
                loanProductPaymentTypeConfig.setLoanProduct(loanProduct);
            }
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductPaymentTypeConfigConstants.paymentTypeIds, element)) {
            final JsonArray paymentTypeIds = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductPaymentTypeConfigConstants.paymentTypeIds, element);
            Set<PaymentType> paymentTypes = savePaymentSet(paymentTypeIds);

            Set<PaymentType> paymentTypesCheck = loanProductPaymentTypeConfig.getPaymentTypes();
            if (!paymentTypesCheck.equals(paymentTypes)) {
                // only update if not equal
                loanProductPaymentTypeConfig.setPaymentTypes(paymentTypes);
                changes.put(LoanProductPaymentTypeConfigConstants.paymentTypeIds, paymentTypes);
            }
        }
        try {
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(loanProductPaymentTypeConfig);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(loanProductPaymentId)
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

    protected Set<PaymentType> savePaymentSet(final JsonArray settings) {
        final Set<PaymentType> paymentTypes = new HashSet<>();
        if (settings != null && !settings.isEmpty()) {
            for (JsonElement setting : settings) {
                final Long paymentTypeId = setting.getAsLong();
                final PaymentType paymentType = this.paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
                paymentTypes.add(paymentType);
            }
        }
        return paymentTypes;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        String[] cause = StringUtils.split(realCause.getMessage(), "'");
        try {
            String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
            if (getCause.contains("name")) {
                final String name = command.stringValueOfParameterNamed(LoanProductPaymentTypeConfigConstants.NAME);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.payment.duplicate",
                        "Loan Product Payment with name `" + name + "` already exists", LoanProductPaymentTypeConfigConstants.NAME, name);
            }
            if (getCause.contains("product_id")) {
                final String loanProductData = command.stringValueOfParameterNamed(LoanProductPaymentTypeConfigConstants.loanProductData);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.payment.duplicate",
                        "Loan Product Payment `" + loanProductData + "` already exists",
                        LoanProductPaymentTypeConfigConstants.loanProductData, loanProductData);
            }
        } catch (PlatformDataIntegrityException e) {
            log.error("handleDataIntegrityIssues LoanProductPaymentErrorOccured: {}", e);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loanproduct.payment.unknown.data.integrity.issue",
                "One or more fields are in conflict.", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("LoanProductPaymentErrorOccured: {}", dve);
    }

}
