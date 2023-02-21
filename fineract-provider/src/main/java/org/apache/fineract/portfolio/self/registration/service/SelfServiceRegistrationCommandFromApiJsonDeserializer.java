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
package org.apache.fineract.portfolio.self.registration.service;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.self.registration.SelfServiceApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class SelfServiceRegistrationCommandFromApiJsonDeserializer {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public SelfServiceRegistrationCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SelfServiceApiConstants.GETSTARTED_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("self");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        String firstName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.firstNameParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.firstNameParamName).value(firstName).notBlank()
                .notExceedingLengthOf(100);

        String lastName = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.lastNameParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.lastNameParamName).value(lastName).notBlank().notExceedingLengthOf(100);

        String email = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.emailParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.emailParamName).value(email).notNull().notBlank()
                .notExceedingLengthOf(100);

        String mobileNumber = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.mobileNumberParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.mobileNumberParamName).value(mobileNumber).notNull()
                .notExceedingLengthOf(11).validatePhoneNumber();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForResend(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SelfServiceApiConstants.RESEND_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("self");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        Long id = this.fromApiJsonHelper.extractLongNamed(SelfServiceApiConstants.requestIdParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.requestIdParamName).value(id).notNull().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateMode(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, SelfServiceApiConstants.VALIDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("self");

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        String authenticationMode = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.authenticationModeParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.authenticationModeParamName).value(authenticationMode).notBlank()
                .isOneOfTheseStringValues(SelfServiceApiConstants.emailModeParamName, SelfServiceApiConstants.mobileModeParamName);

        String value = this.fromApiJsonHelper.extractStringNamed(SelfServiceApiConstants.valueParamName, element);
        baseDataValidator.reset().parameter(SelfServiceApiConstants.valueParamName).value(value).notNull().notBlank()
                .notExceedingLengthOf(100);

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist", "Validation errors exist.",
                    dataValidationErrors);
        }
    }
}
