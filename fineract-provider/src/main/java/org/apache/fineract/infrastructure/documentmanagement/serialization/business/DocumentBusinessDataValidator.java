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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.documentmanagement.api.business.DocumentConfigApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class DocumentBusinessDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public DocumentBusinessDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String entityType, final Long entityId, final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                DocumentConfigApiConstants.DOCUMENT_CREATE_RESPONSE_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DocumentConfigApiConstants.resourceName);

        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityTypeParam).value(entityType).notBlank();
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityIdParam).value(entityId).longGreaterThanZero();

        final String name = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.nameParam, element);
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.nameParam).value(name).notBlank();

        final String typeParam = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.typeParam, element);
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.typeParam).value(typeParam).notBlank();

        String location = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.locationParam, element);
        if (StringUtils.isNotBlank(location)) {
            // check base64 is correct
            try {
                Base64.getDecoder().decode(location);
            } catch (IllegalArgumentException e) {
                location = null;
            }
        }
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.locationParam).value(location).notBlank();

        if (this.fromApiJsonHelper.parameterExists(DocumentConfigApiConstants.descriptionParam, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.descriptionParam, element);
            baseDataValidator.reset().parameter(DocumentConfigApiConstants.descriptionParam).value(description).notBlank();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForCreateArray(final String entityType, final Long entityId, final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DocumentConfigApiConstants.resourceName);

        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityTypeParam).value(entityType).notBlank();
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityIdParam).value(entityId).longGreaterThanZero();

        final JsonArray jsonArray = element.getAsJsonArray();
        baseDataValidator.reset().parameter("Document List").value(jsonArray).jsonArrayNotEmpty();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateForImage(final String entityType, final Long entityId, final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                DocumentConfigApiConstants.IMAGE_CREATE_RESPONSE_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(DocumentConfigApiConstants.resourceName);

        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityTypeParam).value(entityType).notBlank();
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.entityIdParam).value(entityId).longGreaterThanZero();

        final String avatarBase64 = this.fromApiJsonHelper.extractStringNamed(DocumentConfigApiConstants.avatarBase64Param, element);
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.avatarBase64Param).value(avatarBase64).notBlank();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
