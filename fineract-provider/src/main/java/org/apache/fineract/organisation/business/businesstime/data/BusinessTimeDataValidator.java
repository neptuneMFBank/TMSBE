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
package org.apache.fineract.organisation.business.businesstime.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.organisation.business.businesstime.api.BusinessTimeApiResourceConstants;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BusinessTimeDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public BusinessTimeDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, BusinessTimeApiResourceConstants.RESQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(BusinessTimeApiResourceConstants.RESOURCE);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer roleId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(BusinessTimeApiResourceConstants.ROLE_ID, element);
        baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.ROLE_ID).value(roleId).notBlank().integerZeroOrGreater();

        final Integer weekDayId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID, element);
        baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.WEEK_DAY_ID).value(weekDayId).notBlank().integerZeroOrGreater();

        LocalTime endTime = null;
        LocalTime startTime = null;
        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.START_TIME, element)) {
            startTime = this.fromApiJsonHelper.extractLocalTimeNamed(BusinessTimeApiResourceConstants.START_TIME, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.START_TIME).value(startTime).ignoreIfNull();
        }
        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.END_TIME, element)) {
            endTime = this.fromApiJsonHelper.extractLocalTimeNamed(BusinessTimeApiResourceConstants.END_TIME, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.END_TIME).value(endTime).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.START_TIME, element)
                || this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.END_TIME, element)) {
            final String timeFormat = this.fromApiJsonHelper.extractStringNamed(BusinessTimeApiResourceConstants.TIME_FORMAT, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.TIME_FORMAT).value(timeFormat).notBlank();

        }

        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.START_TIME).value(startTime)
                    .failWithCodeNoParameterAddedToErrorCode("starttime.cannot.be.after.endtime");
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, BusinessTimeApiResourceConstants.RESQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(BusinessTimeApiResourceConstants.RESOURCE);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final Integer roleId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(BusinessTimeApiResourceConstants.ROLE_ID, element);
        baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.ROLE_ID).value(roleId).notNull().integerZeroOrGreater();

        final Integer weekDayId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID, element);
        baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.WEEK_DAY_ID).value(weekDayId).notNull().integerZeroOrGreater();
        LocalTime endTime = null;
        LocalTime startTime = null;
        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.START_TIME, element)) {
            startTime = this.fromApiJsonHelper.extractLocalTimeNamed(BusinessTimeApiResourceConstants.START_TIME, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.START_TIME).value(startTime).ignoreIfNull();

        }
        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.END_TIME, element)) {
            endTime = this.fromApiJsonHelper.extractLocalTimeNamed(BusinessTimeApiResourceConstants.END_TIME, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.END_TIME).value(endTime).ignoreIfNull();
        }

        if (this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.START_TIME, element)
                || this.fromApiJsonHelper.parameterExists(BusinessTimeApiResourceConstants.END_TIME, element)) {
            final String timeFormat = this.fromApiJsonHelper.extractStringNamed(BusinessTimeApiResourceConstants.TIME_FORMAT, element);
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.TIME_FORMAT).value(timeFormat).notBlank();

        }
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.START_TIME).value(startTime)
                    .failWithCodeNoParameterAddedToErrorCode("starttime.cannot.be.after.endtime");
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
