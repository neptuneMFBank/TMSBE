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
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductInterestApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductInterestDataValidator;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterest;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestConfigRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductInterestWriteServiceImpl implements LoanProductInterestWriteService {

    private final LoanProductInterestRepositoryWrapper repositoryWrapper;
    private final LoanProductInterestDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;

    private final LoanProductInterestConfigRepositoryWrapper loanProductInterestConfigRepositoryWrapper;

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProductInterest(Long loanProductInterestId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), true);
        LoanProductInterest loanProductInterest = this.repositoryWrapper.findOneWithNotFoundDetection(loanProductInterestId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);
        if (command.isChangeInStringParameterNamed(LoanProductInterestApiResourceConstants.NAME, loanProductInterest.getName())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductInterestApiResourceConstants.NAME, element);
            changes.put(LoanProductInterestApiResourceConstants.NAME, name);
            loanProductInterest.setName(name);
        }
        if (command.isChangeInStringParameterNamed(LoanProductInterestApiResourceConstants.DESCRIPTION, loanProductInterest.getDescription())) {
            final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductInterestApiResourceConstants.DESCRIPTION, element);
            changes.put(LoanProductInterestApiResourceConstants.DESCRIPTION, description);
            loanProductInterest.setDescription(description);
        }

        final Long oldLoanProductId = loanProductInterest.getLoanProduct() != null ? loanProductInterest.getLoanProduct().getId() : null;
        if (command.isChangeInLongParameterNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTID, oldLoanProductId)) {
            final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTID,
                    element);
            LoanProduct loanProduct;
            if (loanProductId != null) {
                loanProduct = loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
                changes.put(LoanProductInterestApiResourceConstants.LOANPRODUCTID, loanProductId);
                loanProductInterest.setLoanProduct(loanProduct);
            }
        }
        try {
            if (this.fromApiJsonHelper.parameterExists(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element)) {
                setLoanProductInterestConfig(element, loanProductInterest);
                final boolean updated = loanProductInterest.update(loanProductInterest.getLoanProductInterestConfig());
                if (updated) {
                    final JsonArray values = this.fromApiJsonHelper
                            .extractJsonArrayNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element);
                    changes.put(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, values.toString());
                }
            }
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(loanProductInterest);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(loanProductInterestId)
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
    public CommandProcessingResult createLoanProductInterest(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), false);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductInterestApiResourceConstants.NAME, element);
        final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductInterestApiResourceConstants.DESCRIPTION, element);
        final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTID, element);
        final LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);

        try {
            LoanProductInterest newLoanProductInterest = LoanProductInterest.create(name, description, loanProduct);
            setLoanProductInterestConfig(element, newLoanProductInterest);
            this.repositoryWrapper.saveAndFlush(newLoanProductInterest);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newLoanProductInterest.getId())
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

    protected void setLoanProductInterestConfig(final JsonElement element, LoanProductInterest newLoanProductInterest) {
        // final Set<LoanProductInterestConfig> loanProductInterestConfig = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element)) {
            final JsonArray loanProductInterestConfigArray = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element);
            for (JsonElement jsonElement : loanProductInterestConfigArray) {
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                BigDecimal minTenor = BigDecimal.ZERO;
                if (jsonObject.has(LoanProductInterestApiResourceConstants.MINTENOR)
                        && jsonObject.get(LoanProductInterestApiResourceConstants.MINTENOR).isJsonPrimitive()) {
                    minTenor = jsonObject.getAsJsonPrimitive(LoanProductInterestApiResourceConstants.MINTENOR)
                            .getAsBigDecimal();
                }
                BigDecimal maxTenor = BigDecimal.ZERO;
                if (jsonObject.has(LoanProductInterestApiResourceConstants.MAXTENOR)
                        && jsonObject.get(LoanProductInterestApiResourceConstants.MAXTENOR).isJsonPrimitive()) {
                    maxTenor = jsonObject.getAsJsonPrimitive(LoanProductInterestApiResourceConstants.MAXTENOR)
                            .getAsBigDecimal();
                }
                BigDecimal nominalInterestRatePerPeriod = BigDecimal.ZERO;
                if (jsonObject.has(LoanProductInterestApiResourceConstants.INTERESTRATEPERPERIOD)
                        && jsonObject.get(LoanProductInterestApiResourceConstants.INTERESTRATEPERPERIOD).isJsonPrimitive()) {
                    nominalInterestRatePerPeriod = jsonObject.getAsJsonPrimitive(LoanProductInterestApiResourceConstants.INTERESTRATEPERPERIOD)
                            .getAsBigDecimal();
                }
                Long id = null;
                if (jsonObject.has(LoanProductInterestApiResourceConstants.ID)
                        && jsonObject.get(LoanProductInterestApiResourceConstants.ID).isJsonPrimitive()) {
                    id = jsonObject.getAsJsonPrimitive(LoanProductInterestApiResourceConstants.ID).getAsLong();
                }
                LoanProductInterestConfig loanProductInterestConfigJsonObject;
                if (id != null) {
                    // update record
                    loanProductInterestConfigJsonObject = this.loanProductInterestConfigRepositoryWrapper.findOneWithNotFoundDetection(id);
                    loanProductInterestConfigJsonObject.setMinTenor(minTenor);
                    loanProductInterestConfigJsonObject.setMaxTenor(maxTenor);
                    loanProductInterestConfigJsonObject.setNominalInterestRatePerPeriod(nominalInterestRatePerPeriod);

                } else {
                    loanProductInterestConfigJsonObject = LoanProductInterestConfig.create(minTenor, maxTenor,
                            nominalInterestRatePerPeriod);
                }
                // loanProductInterestConfig.add(loanProductInterestConfigJsonObject);
                newLoanProductInterest.addLoanProductInterestConfig(loanProductInterestConfigJsonObject);
            }
        }
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
                final String name = command.stringValueOfParameterNamed(LoanProductInterestApiResourceConstants.NAME);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                        "Loan Product Interest with name `" + name + "` already exists", LoanProductInterestApiResourceConstants.NAME,
                        name);
            } else if (getCause.contains("loan_product")) {
                final String loanProductId = command.stringValueOfParameterNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTID);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                        "Loan Product Interest with product id `" + loanProductId + "` already exists",
                        LoanProductInterestApiResourceConstants.LOANPRODUCTID, loanProductId);
            }
        } catch (PlatformDataIntegrityException e) {
            log.error("handleDataIntegrityIssues LoanProductInterestErrorOccured: {}", e);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loanproduct.interest.unknown.data.integrity.issue",
                "One or more fields are in conflict.", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("LoanProductInterestErrorOccured: {}", dve);
    }

}
