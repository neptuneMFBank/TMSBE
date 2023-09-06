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
package org.apache.fineract.portfolio.client.data.business;

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
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.CLIENT_IDENTIFIER_BUSINESS_DATA_PARAMETERS;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.descriptionParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.documentKeyParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.documentTypeIdParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.locationParam;
import static org.apache.fineract.portfolio.client.data.business.ClientIdentifierBusinessApiCollectionConstants.typeParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class ClientIdentifierBusinessDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ClientIdentifierBusinessDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                CLIENT_IDENTIFIER_BUSINESS_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(ClientIdentifierBusinessApiCollectionConstants.resourceNameForPermissions);

        final Long documentTypeId = this.fromApiJsonHelper.extractLongNamed(documentTypeIdParam, element);
        baseDataValidator.reset().parameter(documentTypeIdParam).value(documentTypeId).longGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(documentKeyParam, element)) {
            final String documentKey = this.fromApiJsonHelper.extractStringNamed(documentKeyParam, element);
            baseDataValidator.reset().parameter(documentKeyParam).value(documentKey).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(documentKeyParam, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(descriptionParam, element);
            baseDataValidator.reset().parameter(descriptionParam).value(description).notBlank();
        }
        final String type = this.fromApiJsonHelper.extractStringNamed(typeParam, element);
        baseDataValidator.reset().parameter(typeParam).value(type).notBlank();

        String location = this.fromApiJsonHelper.extractStringNamed(locationParam, element);
        if (StringUtils.isNotBlank(location)) {
            // check base64 is correct
            try {
                Base64.getDecoder().decode(location);
            } catch (IllegalArgumentException e) {
                location = null;
            }
        }
        baseDataValidator.reset().parameter(DocumentConfigApiConstants.locationParam).value(location).notBlank();
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
