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
package org.apache.fineract.portfolio.business.accounttier.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.business.accounttier.api.AccountTierApiResouceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountTierDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountTierDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        Integer parentId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.PARENT_ID, element);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        if (parentId == null) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    AccountTierApiResouceConstants.PARENT_REQUEST_DATA_PARAMETERS);
        } else {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, AccountTierApiResouceConstants.REQUEST_DATA_PARAMETERS);

        }
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AccountTierApiResouceConstants.RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, element)) {
            final Integer activationChannelId = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID).value(activationChannelId).notBlank()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, element)) {
            final BigDecimal cummulativeBalance = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.CUMULATIVE_BALANCE).value(cummulativeBalance).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, element)) {
            final BigDecimal dailyWithdrawalLimit = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT).value(dailyWithdrawalLimit).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, element)) {
            final BigDecimal singDepositLimit = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT).value(singDepositLimit).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.DESCRIPTION, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.DESCRIPTION, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.DESCRIPTION).value(description).notBlank()
                    .notExceedingLengthOf(400);
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.LOCALE, element)) {
            final String locale = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.LOCALE, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.LOCALE).value(locale).notBlank().notExceedingLengthOf(400);
        }
        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.PARENT_ID, element)) {
            parentId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.PARENT_ID, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.PARENT_ID).value(parentId).notBlank()
                    .integerGreaterThanZero();
        }
        if (parentId == null) {
            final Integer clientTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID,
                    element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.CLIENT_TYPE_ID).value(clientTypeId).notBlank()
                    .integerGreaterThanZero();
        }
        final String name = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.NAME, element);
        baseDataValidator.reset().parameter(AccountTierApiResouceConstants.NAME).value(name).notBlank().notExceedingLengthOf(250);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        Integer parentId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.PARENT_ID, element);
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        if (parentId == null) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    AccountTierApiResouceConstants.PARENT_REQUEST_DATA_PARAMETERS);
        } else {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, AccountTierApiResouceConstants.REQUEST_DATA_PARAMETERS);

        }
        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AccountTierApiResouceConstants.RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, element)) {
            final Integer activationChannelId = this.fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.ACTIVATION_CHANNEL_ID).value(activationChannelId).notBlank()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, element)) {
            final BigDecimal cummulativeBalance = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.CUMULATIVE_BALANCE, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.CUMULATIVE_BALANCE).value(cummulativeBalance).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, element)) {
            final BigDecimal dailyWithdrawalLimit = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.DALIY_WITHDRAWAL_LIMIT).value(dailyWithdrawalLimit).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, element)) {
            final BigDecimal singDepositLimit = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.SINGLE_DEPOSIT_LIMIT).value(singDepositLimit).notNull()
                    .zeroOrPositiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.DESCRIPTION, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.DESCRIPTION, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.DESCRIPTION).value(description).notBlank()
                    .notExceedingLengthOf(400);
        }

        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.LOCALE, element)) {
            final String locale = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.LOCALE, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.LOCALE).value(locale).notBlank().notExceedingLengthOf(400);
        }
        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.PARENT_ID, element)) {
            parentId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.PARENT_ID, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.PARENT_ID).value(parentId).integerGreaterThanZero();
        }
        if (parentId == null && this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.PARENT_ID, element)) {
            final Integer clientTypeId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(AccountTierApiResouceConstants.CLIENT_TYPE_ID,
                    element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.CLIENT_TYPE_ID).value(clientTypeId).notBlank()
                    .integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(AccountTierApiResouceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(AccountTierApiResouceConstants.NAME, element);
            baseDataValidator.reset().parameter(AccountTierApiResouceConstants.NAME).value(name).notBlank().notExceedingLengthOf(250);
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
