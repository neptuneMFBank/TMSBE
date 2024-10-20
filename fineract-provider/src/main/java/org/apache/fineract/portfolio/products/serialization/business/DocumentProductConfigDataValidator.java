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
package org.apache.fineract.portfolio.products.serialization.business;

import com.google.gson.JsonArray;
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
import org.apache.fineract.portfolio.products.api.business.DocumentProductConfigApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class DocumentProductConfigDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DocumentProductConfigDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        // loanProductIdsParam, savingsProductIdsParam, configDataIdParam
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                DocumentProductConfigApiConstants.DOCUMENT_PRODUCT_CONFIG_CREATE_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DocumentProductConfigApiConstants.resourceName);

        Integer configDataIdParam = this.fromApiJsonHelper
                .extractIntegerSansLocaleNamed(DocumentProductConfigApiConstants.configDataIdParam, element);
        baseDataValidator.reset().parameter(DocumentProductConfigApiConstants.configDataIdParam).value(configDataIdParam).notNull()
                .integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(DocumentProductConfigApiConstants.savingsProductIdsParam, element)) {
            final JsonArray savingsProductIdsParam = this.fromApiJsonHelper
                    .extractJsonArrayNamed(DocumentProductConfigApiConstants.savingsProductIdsParam, element);
            baseDataValidator.reset().parameter(DocumentProductConfigApiConstants.savingsProductIdsParam).value(savingsProductIdsParam)
                    .jsonArrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(DocumentProductConfigApiConstants.loanProductIdsParam, element)) {
            final JsonArray loanProductIdsParam = this.fromApiJsonHelper
                    .extractJsonArrayNamed(DocumentProductConfigApiConstants.loanProductIdsParam, element);
            baseDataValidator.reset().parameter(DocumentProductConfigApiConstants.loanProductIdsParam).value(loanProductIdsParam)
                    .jsonArrayNotEmpty();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
