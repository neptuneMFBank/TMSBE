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
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductApprovalApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalDataValidator;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApproval;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalConfigRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductApprovalRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
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
            final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, element);
            LoanProduct loanProduct;
            if (loanProductId != null) {
                loanProduct = loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);
                changes.put(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, loanProductId);
                loanProductApproval.setLoanProduct(loanProduct);
            }
        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element)) {
            final Set<LoanProductApprovalConfig> loanProductApprovalConfigNew = setLoanProductApprovalConfig(element);
            final boolean updated = loanProductApproval.update(loanProductApprovalConfigNew);
            if (updated) {
                final String values
                        = this.fromApiJsonHelper.toJson(loanProductApprovalConfigNew);
                changes.put(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, values);
            }
        }
        try {
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(loanProductApproval);
            }
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).with(changes).withEntityId(loanProductApprovalId).build();

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
        final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID, element);
        final LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(loanProductId);

        Set<LoanProductApprovalConfig> loanProductApprovalConfig = setLoanProductApprovalConfig(element);
        try {
            LoanProductApproval newLoanProductApproval = LoanProductApproval.create(name, loanProduct, loanProductApprovalConfig);
            this.repositoryWrapper.saveAndFlush(newLoanProductApproval);
            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(newLoanProductApproval.getId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }

    protected Set<LoanProductApprovalConfig> setLoanProductApprovalConfig(final JsonElement element) {
        try {
            final Set<LoanProductApprovalConfig> loanProductApprovalConfig = new HashSet<>();
            if (this.fromApiJsonHelper.parameterExists(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element)) {
                final JsonArray loanProductApprovalConfigArray
                        = this.fromApiJsonHelper.extractJsonArrayNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTAPPROVALCONFIGDATA, element);
                for (JsonElement jsonElement : loanProductApprovalConfigArray) {
                    final JsonObject jsonObject = jsonElement.getAsJsonObject();
                    final Long roleId = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.ROLEID).getAsLong();
                    final Role role = this.roleRepository.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
                    final Integer rank = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.RANK).getAsInt();
                    BigDecimal maxApprovalAmount = BigDecimal.ZERO;
                    if (jsonObject.has(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT)
                            && jsonObject.get(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT).isJsonPrimitive()) {
                        maxApprovalAmount = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT)
                                .getAsBigDecimal();
                    }
                    Long id = null;
                    if (jsonObject.has(LoanProductApprovalApiResourceConstants.ID)
                            && jsonObject.get(LoanProductApprovalApiResourceConstants.ID).isJsonPrimitive()) {
                        id = jsonObject.getAsJsonPrimitive(LoanProductApprovalApiResourceConstants.MAXAPPROVALAMOUNT)
                                .getAsLong();
                    }
                    LoanProductApprovalConfig loanProductApprovalConfigJsonObject;
                    if (id != null) {
                        //update record
                        loanProductApprovalConfigJsonObject = this.loanProductApprovalConfigRepositoryWrapper.findOneWithNotFoundDetection(id);
                        loanProductApprovalConfigJsonObject.setRank(rank);
                        loanProductApprovalConfigJsonObject.setRole(role);
                        loanProductApprovalConfigJsonObject.setMaxApprovalAmount(maxApprovalAmount);
                    } else {
                        loanProductApprovalConfigJsonObject
                                = LoanProductApprovalConfig.create(role, maxApprovalAmount, rank);
                    }
                    loanProductApprovalConfig.add(loanProductApprovalConfigJsonObject);
                }
            }
            return loanProductApprovalConfig;
        } catch (Exception e) {
            log.warn("setLoanProductApprovalConfig: {}", e);
            throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.config.issue", "Loan product approval config error.", e.getMessage());
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
            final String name = command.stringValueOfParameterNamed(LoanProductApprovalApiResourceConstants.NAME);
            throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate", "Loan Product Approval with name `" + name + "` already exists",
                    LoanProductApprovalApiResourceConstants.NAME, name);
        } else if (getCause.contains("loan_product_id")) {
            final String loanProductId = command.stringValueOfParameterNamed(LoanProductApprovalApiResourceConstants.LOANPRODUCTID);
            throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.duplicate",
                    "Loan Product Approval with product id `" + loanProductId + "` already exists", LoanProductApprovalApiResourceConstants.LOANPRODUCTID, loanProductId);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.loanproduct.approval.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("LoanProductApprovalErrorOccured: {}", dve);
    }

}
