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
package org.apache.fineract.portfolio.business.overdraft.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.business.overdraft.api.OverdraftApiResourceConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OverdraftDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> REQUEST_ACTION_DATA_PARAMETERS = OverdraftApiResourceConstants.REQUEST_ACTION_DATA_PARAMETERS;

    @Autowired
    public OverdraftDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REQUEST_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        //SAVINGS_ID, AMOUNT, LoanApiConstants.localeParameterName,
//            NOMINALINTEREST, STARTDATE, NUMBER_OF_DAYS
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(OverdraftApiResourceConstants.RESOURCENAME);

        final Long savingsId = this.fromApiJsonHelper.extractLongNamed(OverdraftApiResourceConstants.SAVINGS_ID, element);
        baseDataValidator.reset().parameter(OverdraftApiResourceConstants.SAVINGS_ID).value(savingsId).notBlank().integerGreaterThanZero();

        final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.AMOUNT, element);
        baseDataValidator.reset().parameter(OverdraftApiResourceConstants.AMOUNT).value(amount).notNull().zeroOrPositiveAmount();

        final BigDecimal nominalAnnualInterestRateOverdraft = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.NOMINALINTEREST, element);
        baseDataValidator.reset().parameter(OverdraftApiResourceConstants.NOMINALINTEREST).value(nominalAnnualInterestRateOverdraft).notNull().zeroOrPositiveAmount();

        final Long numberOfDays = this.fromApiJsonHelper.extractLongNamed(OverdraftApiResourceConstants.NUMBER_OF_DAYS, element);
        baseDataValidator.reset().parameter(OverdraftApiResourceConstants.NUMBER_OF_DAYS).value(numberOfDays).notBlank().integerGreaterThanZero();

        final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(OverdraftApiResourceConstants.STARTDATE, element);
        baseDataValidator.reset().parameter(OverdraftApiResourceConstants.STARTDATE).value(startDate).notNull().validateDateBefore(DateUtils.getBusinessLocalDate());

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REQUEST_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(OverdraftApiResourceConstants.RESOURCENAME);

        if (this.fromApiJsonHelper.parameterExists(OverdraftApiResourceConstants.AMOUNT, element)) {
            final BigDecimal amount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.AMOUNT, element);
            baseDataValidator.reset().parameter(OverdraftApiResourceConstants.AMOUNT).value(amount).notNull().zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(OverdraftApiResourceConstants.NOMINALINTEREST, element)) {
            final BigDecimal nominalAnnualInterestRateOverdraft = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(OverdraftApiResourceConstants.NOMINALINTEREST, element);
            baseDataValidator.reset().parameter(OverdraftApiResourceConstants.NOMINALINTEREST).value(nominalAnnualInterestRateOverdraft).notNull().zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists(OverdraftApiResourceConstants.NUMBER_OF_DAYS, element)) {
            final Long numberOfDays = this.fromApiJsonHelper.extractLongNamed(OverdraftApiResourceConstants.NUMBER_OF_DAYS, element);
            baseDataValidator.reset().parameter(OverdraftApiResourceConstants.NUMBER_OF_DAYS).value(numberOfDays).notBlank().integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(OverdraftApiResourceConstants.STARTDATE, element)) {
            final LocalDate startDate = this.fromApiJsonHelper.extractLocalDateNamed(OverdraftApiResourceConstants.STARTDATE, element);
            baseDataValidator.reset().parameter(OverdraftApiResourceConstants.STARTDATE).value(startDate).notNull().validateDateBefore(DateUtils.getBusinessLocalDate());
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForStop(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, REQUEST_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(OverdraftApiResourceConstants.RESOURCENAME);

        final String note = this.fromApiJsonHelper.extractStringNamed(SavingsApiConstants.noteParamName, element);
        baseDataValidator.reset().parameter(SavingsApiConstants.noteParamName).value(note).notBlank();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
