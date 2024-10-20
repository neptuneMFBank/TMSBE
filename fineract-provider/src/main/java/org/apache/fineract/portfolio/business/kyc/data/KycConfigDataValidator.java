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
package org.apache.fineract.portfolio.business.kyc.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KycConfigDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                KycConfigApiConstants.KYC_CONFIG_CREATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final String description = this.fromApiJsonHelper.extractStringNamed(KycConfigApiConstants.descriptionParamName, element);
        baseDataValidator.reset().parameter(KycConfigApiConstants.descriptionParamName).value(description).notExceedingLengthOf(150)
                .notBlank();

        final String[] kycParams = this.fromApiJsonHelper.extractArrayNamed(KycConfigApiConstants.KycParamCodeValueIdsParamName, element);
        baseDataValidator.reset().parameter(KycConfigApiConstants.KycParamCodeValueIdsParamName).value(kycParams).notNull().arrayNotEmpty();

        final Long kycConfigCodeValueId = this.fromApiJsonHelper.extractLongNamed(KycConfigApiConstants.KycConfigCodeValueIdParamName,
                element);
        baseDataValidator.reset().parameter(ClientApiConstants.officeIdParamName).value(kycConfigCodeValueId).notNull()
                .integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                KycConfigApiConstants.KYC_CONFIG_UPDATE_REQUEST_DATA_PARAMETERS);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(KycConfigApiConstants.KYC_CONFIG_RESOURCE_NAME);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        if (this.fromApiJsonHelper.parameterExists(KycConfigApiConstants.descriptionParamName, element)) {

            final String description = this.fromApiJsonHelper.extractStringNamed(KycConfigApiConstants.descriptionParamName, element);
            baseDataValidator.reset().parameter(KycConfigApiConstants.descriptionParamName).value(description).notExceedingLengthOf(150)
                    .notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(KycConfigApiConstants.KycParamCodeValueIdsParamName, element)) {

            final String[] kycParams = this.fromApiJsonHelper.extractArrayNamed(KycConfigApiConstants.KycParamCodeValueIdsParamName,
                    element);
            baseDataValidator.reset().parameter(KycConfigApiConstants.KycParamCodeValueIdsParamName).value(kycParams).notNull()
                    .arrayNotEmpty();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
