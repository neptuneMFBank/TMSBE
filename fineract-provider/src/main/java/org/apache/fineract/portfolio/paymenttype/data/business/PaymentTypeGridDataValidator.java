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
package org.apache.fineract.portfolio.paymenttype.data.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.paymenttype.api.business.PaymentTypeGridApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTypeGridDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public PaymentTypeGridDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, PaymentTypeGridApiResourceConstants.REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PaymentTypeGridApiResourceConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String name = this.fromApiJsonHelper.extractStringNamed(PaymentTypeGridApiResourceConstants.NAME, element);
        baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.NAME).value(name).notBlank().notExceedingLengthOf(255);

        final Boolean isGrid = this.fromApiJsonHelper
                .extractBooleanNamed(PaymentTypeGridApiResourceConstants.ISGRID, element);
        baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.ISGRID).value(isGrid)
                .ignoreIfNull().validateForBooleanValue();

        final Boolean isCommision = this.fromApiJsonHelper
                .extractBooleanNamed(PaymentTypeGridApiResourceConstants.ISCOMMISION, element);
        baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.ISCOMMISION).value(isCommision)
                .ignoreIfNull().validateForBooleanValue();

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.GRID_JSON, element)) {
            final JsonArray gridJson = this.fromApiJsonHelper.extractJsonArrayNamed(PaymentTypeGridApiResourceConstants.GRID_JSON, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.GRID_JSON).value(gridJson).notBlank();
        }
        final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE, element, Locale.getDefault());
        baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE).value(paymentTypeId).notNull().integerZeroOrGreater();

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE, element)) {
            final Integer calculationType = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE).value(calculationType).notNull().integerZeroOrGreater();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.AMOUNT, element)) {
            final BigDecimal amount = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(PaymentTypeGridApiResourceConstants.AMOUNT, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.AMOUNT).value(amount).notNull()
                    .zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.PERCENT, element)) {
            final BigDecimal percent = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(PaymentTypeGridApiResourceConstants.PERCENT, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PERCENT).value(percent).notNull()
                    .zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.CHARGE_DATA, element)) {
            final Integer chargeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.CHARGE_DATA, element, Locale.getDefault());
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.CHARGE_DATA).value(chargeId).notNull().integerZeroOrGreater();
        }
        final String locale = this.fromApiJsonHelper.extractStringNamed(PaymentTypeGridApiResourceConstants.LOCALE, element);
        baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.LOCALE).value(locale).notBlank().notExceedingLengthOf(255);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, PaymentTypeGridApiResourceConstants.REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(PaymentTypeGridApiResourceConstants.RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(PaymentTypeGridApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.NAME).value(name).notBlank().notExceedingLengthOf(255);
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.ISGRID, element)) {
            final Boolean isGrid = this.fromApiJsonHelper
                    .extractBooleanNamed(PaymentTypeGridApiResourceConstants.ISGRID, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.ISGRID).value(isGrid)
                    .ignoreIfNull().validateForBooleanValue();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.ISCOMMISION, element)) {
            final Boolean isCommision = this.fromApiJsonHelper
                    .extractBooleanNamed(PaymentTypeGridApiResourceConstants.ISCOMMISION, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.ISCOMMISION).value(isCommision)
                    .ignoreIfNull().validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE, element)) {
            final Integer paymentTypeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PAYMENT_TYPE).value(paymentTypeId).notNull().integerZeroOrGreater();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE, element)) {
            final Integer calculationType = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE, element, Locale.getDefault());
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PAYMENTCALCULATIONTYPE).value(calculationType).notNull().integerZeroOrGreater();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.AMOUNT, element)) {
            final BigDecimal amount = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(PaymentTypeGridApiResourceConstants.AMOUNT, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.AMOUNT).value(amount).notNull()
                    .zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.PERCENT, element)) {
            final BigDecimal percent = this.fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(PaymentTypeGridApiResourceConstants.PERCENT, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.PERCENT).value(percent).notNull()
                    .zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.GRID_JSON, element)) {
            final JsonArray gridJson = this.fromApiJsonHelper.extractJsonArrayNamed(PaymentTypeGridApiResourceConstants.GRID_JSON, element);
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.GRID_JSON).value(gridJson).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(PaymentTypeGridApiResourceConstants.CHARGE_DATA, element)) {
            final Integer chargeId = this.fromApiJsonHelper.extractIntegerNamed(PaymentTypeGridApiResourceConstants.CHARGE_DATA, element, Locale.getDefault());
            baseDataValidator.reset().parameter(PaymentTypeGridApiResourceConstants.CHARGE_DATA).value(chargeId).notNull().integerZeroOrGreater();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
