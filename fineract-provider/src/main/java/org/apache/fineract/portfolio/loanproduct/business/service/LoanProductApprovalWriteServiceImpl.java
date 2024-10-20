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
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductApprovalApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalDataValidator;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApproval;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalConfigRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.exception.LoanProductApprovalNotFoundException;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductRepositoryWrapper;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.domain.RoleRepository;
import org.apache.fineract.useradministration.exception.RoleNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductApprovalWriteServiceImpl implements LoanProductApprovalWriteService {

    private final LoanProductApprovalRepositoryWrapper repositoryWrapper;
    private final LoanProductApprovalDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final RoleRepository roleRepository;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final LoanProductApprovalConfigRepositoryWrapper loanProductApprovalConfigRepositoryWrapper;
    private final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper;

    @Transactional
    @Override
    public CommandProcessingResult updateLoanProductApproval(Long loanProductApprovalId, JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), true);
        LoanProductApproval loanProductApproval = this.repositoryWrapper.findOneWithNotFoundDetection(loanProductApprovalId);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final Map<String, Object> changes = new LinkedHashMap<>(9);
        if (command.isChangeInStringParameterNamed(LoanProductApprovalApiResourceConstants.NAME, loanProductApproval.getName())) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductApprovalApiResourceConstants.NAME, element);
            changes.put(LoanProductApprovalApiResourceConstants.NAME, name);
            loanProductApproval.setName(name);
        }

        final Long oldLoanProductId = loanProductApproval.getLoanProduct() != null ? loanProductApproval.getLoanProduct().getId() : null;
        if (command.isChangeInLongParameterNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, oldLoanProductId)) {
            final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID,
                    element);
            LoanProduct loanProduct;
            if (loanProductId != null) {
                loanProduct = loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
                changes.put(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, loanProductId);
                loanProductApproval.setLoanProduct(loanProduct);
            }
        }

        final Long oldSavingsProductId = loanProductApproval.getSavingsProduct() != null ? loanProductApproval.getSavingsProduct().getId()
                : null;
        if (command.isChangeInLongParameterNamed(LoanProductApprovalApiResourceConstants.SAVINGSPRODUCTID, oldSavingsProductId)) {
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.SAVINGSPRODUCTID,
                    element);
            SavingsProduct savingsProduct;
            if (savingsProductId != null) {
                savingsProduct = savingsProductRepositoryWrapper.findOneWithNotFoundDetection(savingsProductId);
                changes.put(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, savingsProductId);
                loanProductApproval.setSavingsProduct(savingsProduct);
            }
        }
        try {
            if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element)) {
                setLoanProductApprovalConfig(element, loanProductApproval);
                final boolean updated = loanProductApproval.update(loanProductApproval.getLoanProductApprovalConfig());
                if (updated) {
                    final JsonArray values = this.fromApiJsonHelper
                            .extractJsonArrayNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element);
                    changes.put(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, values.toString());
                }
            }
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(loanProductApproval);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(loanProductApprovalId)
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
    public CommandProcessingResult createLoanProductApproval(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json(), false);
        final JsonElement element = this.fromApiJsonHelper.parse(command.json());
        final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductApprovalApiResourceConstants.NAME, element);
        boolean noLoansSavingsSelected = true;
        LoanProduct loanProduct = null;
        if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, element)) {
            final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID,
                    element);
            loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
            noLoansSavingsSelected = false;
        }
        SavingsProduct savingsProduct = null;
        if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.SAVINGSPRODUCTID, element)) {
            final Long savingsProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.SAVINGSPRODUCTID,
                    element);
            savingsProduct = this.savingsProductRepositoryWrapper.findOneWithNotFoundDetection(savingsProductId);
            noLoansSavingsSelected = false;
        }
        if (noLoansSavingsSelected) {
            throw new LoanProductApprovalNotFoundException("No product selected");
        }
        try {
            LoanProductApproval newLoanProductApproval = LoanProductApproval.create(name, loanProduct, savingsProduct);
            setLoanProductApprovalConfig(element, newLoanProductApproval);
            this.repositoryWrapper.saveAndFlush(newLoanProductApproval);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newLoanProductApproval.getId())
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

    protected void setLoanProductApprovalConfig(final JsonElement element, LoanProductApproval newLoanProductApproval) {
        // final Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();
        if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element)) {
            final JsonArray loanProductApprovalConfigArray = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element);
            for (JsonElement jsonElement : loanProductApprovalConfigArray) {
                final JsonObject jsonObject = jsonElement.getAsJsonObject();
                final Long roleId = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.ROLEID).getAsLong();
                final Role role = this.roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
                final Integer rank = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.RANK).getAsInt();
                BigDecimal minApprovalAmount = BigDecimal.ZERO;
                if (jsonObject.has(LoanProductApprovalApiResourceConstants.MINAPPROVALAMOUNT)
                        && jsonObject.get(LoanProductApprovalApiResourceConstants.MINAPPROVALAMOUNT).isJsonPrimitive()) {
                    minApprovalAmount = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.MINAPPROVALAMOUNT)
                            .getAsBigDecimal();
                }
                BigDecimal maxApprovalAmount = BigDecimal.ZERO;
                if (jsonObject.has(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT)
                        && jsonObject.get(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT).isJsonPrimitive()) {
                    maxApprovalAmount = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT)
                            .getAsBigDecimal();
                }
                Long id = null;
                if (jsonObject.has(LoanProductApprovalApiResourceConstants.ID)
                        && jsonObject.get(LoanProductApprovalApiResourceConstants.ID).isJsonPrimitive()) {
                    id = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.ID).getAsLong();
                }
                LoanProductApprovalConfig loanProductApprovalConfigJsonObject;
                if (id != null) {
                    // update record
                    loanProductApprovalConfigJsonObject = this.loanProductApprovalConfigRepositoryWrapper.findOneWithNotFoundDetection(id);
                    loanProductApprovalConfigJsonObject.setRank(rank);
                    loanProductApprovalConfigJsonObject.setRole(role);
                    loanProductApprovalConfigJsonObject.setMinApprovalAmount(minApprovalAmount);
                    loanProductApprovalConfigJsonObject.setMaxApprovalAmount(maxApprovalAmount);

                } else {
                    loanProductApprovalConfigJsonObject = LoanProductApprovalConfig.create(role, minApprovalAmount, maxApprovalAmount,
                            rank);
                }
                // loanProductApprovalConfig.add(loanProductApprovalConfigJsonObject);
                newLoanProductApproval.addLoanProductApprovalConfig(loanProductApprovalConfigJsonObject);
            }
        }
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        // String[] cause = StringUtils.split(realCause.getMessage(), "'");
        try {
            // String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
            String getCause = StringUtils.defaultIfBlank(realCause.getMessage(), "");
            if (getCause.contains("name")) {
                final String name = command.stringValueOfParameterNamed(LoanProductApprovalApiResourceConstants.NAME);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                        "Loan Product Approval with name `" + name + "` already exists", LoanProductApprovalApiResourceConstants.NAME,
                        name);
            } else if (getCause.contains("rlpa_UNIQUE_rank")) {
                final String rank = command.stringValueOfParameterNamed(LoanProductApprovalApiResourceConstants.RANK);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                        "Loan Product Approval with index `" + rank + "` already exists", LoanProductApprovalApiResourceConstants.RANK,
                        rank);
            } else if (getCause.contains("loan_product")) {
                final String loanProductId = command.stringValueOfParameterNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID);
                throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                        "Loan Product Approval with product id `" + loanProductId + "` already exists",
                        LoanProductApprovalApiResourceConstants.LOANPRODUCTID, loanProductId);
            }
        } catch (PlatformDataIntegrityException e) {
            log.error("handleDataIntegrityIssues LoanProductApprovalErrorOccured: {}", e);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.unknown.data.integrity.issue",
                "One or more fields are in conflict.", "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("LoanProductApprovalErrorOccured: {}", dve);
    }

}
