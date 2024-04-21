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
package org.apache.fineract.portfolio.products.data.business;


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
import org.apache.fineract.portfolio.products.api.business.ProductVisibilityApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProductVisibilityDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ProductVisibilityDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json, final String entityName) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        if (entityName.equals(ProductVisibilityApiResourceConstants.LOAN_VISIBILITY_RESOURCENAME)) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    ProductVisibilityApiResourceConstants.REQUEST_DATA_LOAN_VISIBILITY_PARAMETERS);
        } else if (entityName.equals(ProductVisibilityApiResourceConstants.SAVINGS_VISIBILITY_RESOURCENAME)) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    ProductVisibilityApiResourceConstants.REQUEST_DATA_SAVINGS_VISIBILITY_PARAMETERS);
        }
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("PRODUCT_VISIBILITY");

        final String name = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.NAME, element);
        baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.NAME).value(name).notExceedingLengthOf(150).notBlank();

        final String description = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.DESCRIPTION, element);
        baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.DESCRIPTION).value(description).notExceedingLengthOf(150)
                .notBlank();

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            final String[] loanProducts = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.LOANPRODUCT,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.LOANPRODUCT).value(loanProducts).notNull()
                    .arrayNotEmpty();
        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            final String[] clientClassification = this.fromApiJsonHelper
                    .extractArrayNamed(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION).value(clientClassification)
                    .notNull().arrayNotEmpty();

        }
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            final String[] clientTypes = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.CLIENTTYPE,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.CLIENTTYPE).value(clientTypes).notNull()
                    .arrayNotEmpty();

        }
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            final String[] legalEnums = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.LEGALENUM, element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.LEGALENUM).value(legalEnums).notNull()
                    .arrayNotEmpty();

        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT, element)) {
            final String[] savingsProducts = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT).value(savingsProducts).notNull()
                    .arrayNotEmpty();

        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json, final String entityName) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();

        if (entityName.equals(ProductVisibilityApiResourceConstants.LOAN_VISIBILITY_RESOURCENAME)) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    ProductVisibilityApiResourceConstants.REQUEST_DATA_LOAN_VISIBILITY_PARAMETERS);
        } else if (entityName.equals(ProductVisibilityApiResourceConstants.SAVINGS_VISIBILITY_RESOURCENAME)) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                    ProductVisibilityApiResourceConstants.REQUEST_DATA_SAVINGS_VISIBILITY_PARAMETERS);
        }
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource("PRODUCT_VISIBILITY");

        boolean atLeastOneParameterPassedForUpdate = false;

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.DESCRIPTION, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String description = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.DESCRIPTION,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.DESCRIPTION).value(description)
                    .notExceedingLengthOf(150).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.NAME, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String name = this.fromApiJsonHelper.extractStringNamed(ProductVisibilityApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.NAME).value(name).notExceedingLengthOf(150)
                    .notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] loanProducts = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.LOANPRODUCT,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.LOANPRODUCT).value(loanProducts).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] clientClassification = this.fromApiJsonHelper
                    .extractArrayNamed(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION).value(clientClassification)
                    .notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] clientTypes = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.CLIENTTYPE,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.CLIENTTYPE).value(clientTypes).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] legalEnums = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.LEGALENUM, element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.LEGALENUM).value(legalEnums).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] SavingsProducts = this.fromApiJsonHelper.extractArrayNamed(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT,
                    element);
            baseDataValidator.reset().parameter(ProductVisibilityApiResourceConstants.SAVINGSPRODUCT).value(SavingsProducts).notNull();
        }

        if (!atLeastOneParameterPassedForUpdate) {
            final Object forceError = null;
            baseDataValidator.reset().anyOfNotNull(forceError);
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {

            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
