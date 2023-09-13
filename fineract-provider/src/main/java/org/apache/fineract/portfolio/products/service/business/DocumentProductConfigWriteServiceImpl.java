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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.domain.business.ClientDocumentConfig;
import org.apache.fineract.portfolio.client.domain.business.ClientDocumentRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.product.domain.business.DocumentProductConfig;
import org.apache.fineract.portfolio.product.domain.business.DocumentProductRepositoryWrapper;
import org.apache.fineract.portfolio.products.api.business.DocumentProductConfigApiConstants;
import org.apache.fineract.portfolio.products.serialization.business.DocumentProductConfigDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DocumentProductConfigWriteServiceImpl implements DocumentProductConfigWriteService {

    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final DocumentProductConfigDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientDocumentRepositoryWrapper clientDocumentRepositoryWrapper;
    private final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper;
    private final DocumentProductRepositoryWrapper documentProductRepositoryWrapper;

    @Autowired
    public DocumentProductConfigWriteServiceImpl(final PlatformSecurityContext context,
            final DocumentProductConfigDataValidator fromApiJsonDeserializer, final FromJsonHelper fromApiJsonHelper,
            final ClientDocumentRepositoryWrapper clientDocumentRepositoryWrapper,
            final LoanProductRepositoryWrapper loanProductRepositoryWrapper,
            final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper,
            final DocumentProductRepositoryWrapper documentProductRepositoryWrapper) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.clientDocumentRepositoryWrapper = clientDocumentRepositoryWrapper;
        this.savingsProductRepositoryWrapper = savingsProductRepositoryWrapper;
        this.documentProductRepositoryWrapper = documentProductRepositoryWrapper;
        this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createProductDocumentConfig(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());
        final JsonElement jsonElement = this.fromApiJsonHelper.parse(command.json());

        // loanProductIdsParam, savingsProductIdsParam, configDataIdParam
        final Long configDataIdParam = this.fromApiJsonHelper.extractLongNamed(DocumentProductConfigApiConstants.configDataIdParam,
                jsonElement);
        final ClientDocumentConfig clientDocumentConfig = clientDocumentRepositoryWrapper.findOneWithNotFoundDetection(configDataIdParam);

        try {

            final JsonArray savingsProductIdsParam = this.fromApiJsonHelper
                    .extractJsonArrayNamed(DocumentProductConfigApiConstants.savingsProductIdsParam, jsonElement);
            saveSavingsProduct(clientDocumentConfig, savingsProductIdsParam);

            final JsonArray loanProductIdsParam = this.fromApiJsonHelper
                    .extractJsonArrayNamed(DocumentProductConfigApiConstants.loanProductIdsParam, jsonElement);
            saveLoanProduct(clientDocumentConfig, loanProductIdsParam);

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(throwable, dve);
            return CommandProcessingResult.empty();
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(configDataIdParam) //
                .build();
    }

    protected void saveSavingsProduct(final ClientDocumentConfig clientDocumentConfig, final JsonArray settings) {
        final Set<DocumentProductConfig> documentProductConfigs = new HashSet<>();
        if (settings != null && !settings.isEmpty()) {
            for (JsonElement setting : settings) {
                final Long savingsProductId = setting.getAsLong();
                final SavingsProduct savingsProduct = this.savingsProductRepositoryWrapper.findOneWithNotFoundDetection(savingsProductId);
                final DocumentProductConfig documentProductConfig = new DocumentProductConfig(clientDocumentConfig, null, savingsProduct);
                documentProductConfigs.add(documentProductConfig);
            }
            this.documentProductRepositoryWrapper.saveAllAndFlush(documentProductConfigs.stream().collect(Collectors.toList()));
        }
    }

    protected void saveLoanProduct(final ClientDocumentConfig clientDocumentConfig, final JsonArray settings) {
        final Set<DocumentProductConfig> documentProductConfigs = new HashSet<>();
        if (settings != null && !settings.isEmpty()) {
            for (JsonElement setting : settings) {
                final Long loanProductId = setting.getAsLong();
                final LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
                final DocumentProductConfig documentProductConfig = new DocumentProductConfig(clientDocumentConfig, loanProduct, null);
                documentProductConfigs.add(documentProductConfig);
            }
            this.documentProductRepositoryWrapper.saveAllAndFlush(documentProductConfigs.stream().collect(Collectors.toList()));
        }
    }

    @Override
    public CommandProcessingResult deleteProductDocumentConfig(Long entityId, JsonCommand command) {
        this.context.authenticatedUser();
        this.clientDocumentRepositoryWrapper.findOneWithNotFoundDetection(entityId);
        this.clientDocumentRepositoryWrapper.deleteById(entityId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(entityId) //
                .build();

    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("loan_product_UNIQUE")) {
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.loan_product", "Loan product already exists",
                    DocumentProductConfigApiConstants.loanProductIdsParam);
        } else if (realCause.getMessage().contains("savings_product_UNIQUE")) {
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.savings_product", "Savings product already exists",
                    DocumentProductConfigApiConstants.savingsProductIdsParam);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occured.", dve);
    }

}
