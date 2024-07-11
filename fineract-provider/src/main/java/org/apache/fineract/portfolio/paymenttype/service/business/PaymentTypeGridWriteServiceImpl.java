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
package org.apache.fineract.portfolio.paymenttype.service.business;

import java.math.BigDecimal;
import java.util.Map;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.api.business.PaymentTypeGridApiResourceConstants;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridDataValidator;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.business.PaymentTypeGrid;
import org.apache.fineract.portfolio.paymenttype.domain.business.PaymentTypeGridRepositoryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PaymentTypeGridWriteServiceImpl implements PaymentTypeGridWriteService {

    private final PaymentTypeGridDataValidator fromApiJsonDeserializer;
    private final PlatformSecurityContext context;
    private final PaymentTypeGridRepositoryWrapper repositoryWrapper;
    private final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper;
    private final ChargeRepositoryWrapper chargeRepositoryWrapper;

    @Autowired
    public PaymentTypeGridWriteServiceImpl(final PaymentTypeGridDataValidator fromApiJsonDeserializer,
            final PlatformSecurityContext context, final PaymentTypeGridRepositoryWrapper repositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper, final ChargeRepositoryWrapper chargeRepositoryWrapper) {
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.context = context;
        this.repositoryWrapper = repositoryWrapper;
        this.paymentTypeRepositoryWrapper = paymentTypeRepositoryWrapper;
        this.chargeRepositoryWrapper = chargeRepositoryWrapper;
    }

    @Transactional
    @Override
    public CommandProcessingResult createPaymentTypeGrid(JsonCommand command) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateForCreate(command.json());

        try {
            final String name = command.stringValueOfParameterNamed(PaymentTypeGridApiResourceConstants.NAME);
            final String gridJson = command.stringValueOfParameterNamed(PaymentTypeGridApiResourceConstants.GRID_JSON);
            final Boolean isCommision = command.booleanObjectValueOfParameterNamed(PaymentTypeGridApiResourceConstants.ISCOMMISION);
            final Boolean isGrid = command.booleanObjectValueOfParameterNamed(PaymentTypeGridApiResourceConstants.ISGRID);
            final BigDecimal amount = command.bigDecimalValueOfParameterNamed(PaymentTypeGridApiResourceConstants.AMOUNT);
            final Integer paymentCalculationType = command
                    .integerValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE);
            final Long paymentTypeId = command.longValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE);
            final BigDecimal percent = command.bigDecimalValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PERCENT);
            final Long chargeDataId = command.longValueOfParameterNamed(PaymentTypeGridApiResourceConstants.CHARGE_DATA);
            Charge charge = this.chargeRepositoryWrapper.findOneWithNotFoundDetection(chargeDataId);
            PaymentType paymentType = paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
            PaymentTypeGrid paymentTypeGrid = PaymentTypeGrid.instance(paymentType, name, gridJson, isGrid, isCommision,
                    paymentCalculationType, amount, percent, charge);

            this.repositoryWrapper.saveAndFlush(paymentTypeGrid);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(paymentTypeGrid.getId()).build();

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

        if (realCause.getMessage().contains("payment_type_grid_name_UNIQUE'")) {

            final String name = command.stringValueOfParameterNamed(PaymentTypeGridApiResourceConstants.NAME);
            throw new PlatformDataIntegrityException("error.msg.payment.type.grid.duplicate.name",
                    "Payment Type grid with name `" + name + "` already exists", "name", name);
        }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.payment.Type.Grid.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("PaynentTyeGridErrorOccured: {}", dve);
    }

    @Transactional
    @Override
    public CommandProcessingResult updatePaymentTypeGrid(Long paymentTypeGridId, JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final PaymentTypeGrid paymentTypeGrid = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeGridId);
            final Long paymentTypeId = command.longValueOfParameterNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE);

            final Map<String, Object> changes = paymentTypeGrid.update(command);

            if (changes.containsKey(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE)) {
                PaymentType paymentType = paymentTypeRepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
                paymentTypeGrid.setPaymentType(paymentType);
            }
            if (!changes.isEmpty()) {
                this.repositoryWrapper.saveAndFlush(paymentTypeGrid);
            }

            return new CommandProcessingResultBuilder().with(changes).withEntityId(paymentTypeGrid.getId()).build();

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
    public CommandProcessingResult delete(final Long paymentTypeGridId) {

        try {
            PaymentTypeGrid paymentTypeGrid = this.repositoryWrapper.findOneWithNotFoundDetection(paymentTypeGridId);
            this.repositoryWrapper.delete(paymentTypeGrid);
            this.repositoryWrapper.flush();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            final Throwable throwable = dve.getMostSpecificCause();
            handleDataIntegrityIssues(null, throwable, dve);
            return CommandProcessingResult.empty();
        }
        return new CommandProcessingResultBuilder().withEntityId(paymentTypeGridId).build();
    }
}
