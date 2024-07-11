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
package org.apache.fineract.portfolio.business.bankTransfer.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.business.bankTransfer.api.TransferApprovalApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransferApprovalDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public TransferApprovalDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                TransferApprovalApiResourceConstants.RESQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(TransferApprovalApiResourceConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(TransferApprovalApiResourceConstants.AMOUNT,
                element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.AMOUNT).value(amount).notNull().zeroOrPositiveAmount();
        //
        // if (this.fromApiJsonHelper.parameterExists(TransferApprovalApiResourceConstants.STATUS, element)) {
        // final Integer status =
        // this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.STATUS, element);
        // baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.STATUS).value(status).ignoreIfNull().integerZeroOrGreater();
        // }

        final Integer transferType = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.TRANSFER_TYPE, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TRANSFER_TYPE).value(transferType).notNull()
                .integerZeroOrGreater();

        final Integer fromAccountId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_ID, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.FROM_ACCOUNT_ID).value(fromAccountId).notNull()
                .integerZeroOrGreater();

        final Integer fromAccountType = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_TYPE, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.FROM_ACCOUNT_TYPE).value(fromAccountType).notNull()
                .integerZeroOrGreater();

        final String fromAccountNumber = this.fromApiJsonHelper.extractStringNamed(TransferApprovalApiResourceConstants.FROM_ACCOUNT_NUMBER,
                element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.FROM_ACCOUNT_NUMBER).value(fromAccountNumber).notBlank()
                .notExceedingLengthOf(10);

        if (this.fromApiJsonHelper.parameterExists(TransferApprovalApiResourceConstants.TO_ACCOUNT_ID, element)) {
            final Integer toAccountId = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_ID, element);
            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TO_ACCOUNT_ID).value(toAccountId).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        if (this.fromApiJsonHelper.parameterExists(TransferApprovalApiResourceConstants.TO_ACCOUNT_TYPE, element)) {
            final Integer toAccountType = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_TYPE, element);
            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TO_ACCOUNT_TYPE).value(toAccountType).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        final String toAccountNumber = this.fromApiJsonHelper.extractStringNamed(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER,
                element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER).value(toAccountNumber).notBlank()
                .notExceedingLengthOf(10);

        if (this.fromApiJsonHelper.parameterExists(TransferApprovalApiResourceConstants.noteParameterName, element)) {
            final String note = this.fromApiJsonHelper.extractStringNamed(TransferApprovalApiResourceConstants.noteParameterName, element);
            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TO_ACCOUNT_NUMBER).value(note).notBlank();
        }
        final Integer activationChannelId = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.ACTIVATION_CHANNEL_ID, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.ACTIVATION_CHANNEL_ID).value(activationChannelId)
                .ignoreIfNull().integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(TransferApprovalApiResourceConstants.TO_BANK_ID, element)) {
            final Integer toBankId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(TransferApprovalApiResourceConstants.TO_BANK_ID,
                    element);
            baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.TO_BANK_ID).value(toBankId).ignoreIfNull()
                    .integerZeroOrGreater();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateApproval(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> approvalParameters = new HashSet<>(Arrays.asList(
                // TransferApprovalApiResourceConstants.approvedOnDateParameterName,
                TransferApprovalApiResourceConstants.noteParameterName, TransferApprovalApiResourceConstants.localeParameterName,
                TransferApprovalApiResourceConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, approvalParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(TransferApprovalApiResourceConstants.RESOURCE_NAME);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        // final LocalDate approvedOnDate =
        // this.fromApiJsonHelper.extractLocalDateNamed(TransferApprovalApiResourceConstants.approvedOnDateParameterName,
        // element);
        // baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.approvedOnDateParameterName).value(approvedOnDate).notNull();
        final String note = this.fromApiJsonHelper.extractStringNamed(TransferApprovalApiResourceConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateRejection(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Set<String> rejectParameters = new HashSet<>(Arrays.asList(
                // TransferApprovalApiResourceConstants.rejectedOnDateParameterName,
                TransferApprovalApiResourceConstants.noteParameterName, TransferApprovalApiResourceConstants.localeParameterName,
                TransferApprovalApiResourceConstants.dateFormatParameterName));

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, rejectParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("loanapplication");

        final JsonElement element = this.fromApiJsonHelper.parse(json);
        // final LocalDate rejectedOnDate =
        // this.fromApiJsonHelper.extractLocalDateNamed(TransferApprovalApiResourceConstants.rejectedOnDateParameterName,
        // element);
        // baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.rejectedOnDateParameterName).value(rejectedOnDate).notNull();

        final String note = this.fromApiJsonHelper.extractStringNamed(TransferApprovalApiResourceConstants.noteParameterName, element);
        baseDataValidator.reset().parameter(TransferApprovalApiResourceConstants.noteParameterName).value(note).notExceedingLengthOf(1000);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
