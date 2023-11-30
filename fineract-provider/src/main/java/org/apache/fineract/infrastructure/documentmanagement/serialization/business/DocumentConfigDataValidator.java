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
package org.apache.fineract.infrastructure.documentmanagement.serialization.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class DocumentConfigDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DocumentConfigDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                DocumentConfigApiConstants.DOCUMENT_CONFIG_CREATE_RESPONSE_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DocumentConfigApiConstants.resourceName);

        final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, element);
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.nameParam).value(name).notBlank();

        Integer typeParam = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(DocumentConfigApiConstants.typeParam, element);
        if (typeParam != null) {
            final GlobalEntityType globalEntityType = GlobalEntityType.fromInt(typeParam);
            if (globalEntityType == null) {
                typeParam = null;
            }
        }
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.typeParam).value(typeParam).notNull().integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(DocumentConfigApiConstants.descriptionParam, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam, element);
            baseDataValidator.reset().parameter(DocumentConfigApiConstants.descriptionParam).value(description).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(DocumentConfigApiConstants.formIdParam, element)) {
            final Integer formId = this.fromApiJsonHelper.extractIntegerSansLocaleNamed(DocumentConfigApiConstants.formIdParam, element);
            baseDataValidator.reset().parameter(DocumentConfigApiConstants.formIdParam).value(formId).notNull()
                    .isOneOfTheseValues(LegalForm.ENTITY.getValue(), LegalForm.PERSON.getValue(), LegalForm.VENDOR.getValue());
        }
        if (this.fromApiJsonHelper.parameterExists(DocumentConfigApiConstants.settingsParam, element)) {
            final JsonArray settings = this.fromApiJsonHelper.extractJsonArrayNamed(DocumentConfigApiConstants.settingsParam, element);
            baseDataValidator.reset().parameter(DocumentConfigApiConstants.settingsParam).value(settings).jsonArrayNotEmpty();
        }
        // if (this.fromApiJsonHelper.parameterExists(DocumentConfigApiConstants.productIdsParam, element)) {
        // final JsonArray productIds =
        // this.fromApiJsonHelper.extractJsonArrayNamed(DocumentConfigApiConstants.productIdsParam, element);
        // baseDataValidator.reset().parameter(DocumentConfigApiConstants.productIdsParam).value(productIds).jsonArrayNotEmpty();
        // }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
