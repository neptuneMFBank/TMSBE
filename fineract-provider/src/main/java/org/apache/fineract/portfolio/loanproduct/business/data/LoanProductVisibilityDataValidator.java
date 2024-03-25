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
package org.apache.fineract.portfolio.loanproduct.business.data;

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
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductApprovalApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductVisibilityApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoanProductVisibilityDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public LoanProductVisibilityDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LoanProductVisibilityApiResourceConstants.REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(LoanProductVisibilityApiResourceConstants.RESOURCENAME);

        final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductVisibilityApiResourceConstants.NAME, element);
        baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.NAME).value(name).notExceedingLengthOf(150).notBlank();

        final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductVisibilityApiResourceConstants.DESCRIPTION, element);
        baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.DESCRIPTION).value(description).notExceedingLengthOf(150).notBlank();

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            final String[] loanProducts = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.LOANPRODUCT,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.LOANPRODUCT).value(loanProducts).notNull().arrayNotEmpty();

        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            final String[] clientClassification = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION).value(clientClassification).notNull().arrayNotEmpty();

        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            final String[] clientTypes = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.CLIENTTYPE,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.CLIENTTYPE).value(clientTypes).notNull().arrayNotEmpty();

        }
        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            final String[] legalEnums = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.LEGALENUM,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.LEGALENUM).value(legalEnums).notNull().arrayNotEmpty();

        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LoanProductVisibilityApiResourceConstants.REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(LoanProductApprovalApiResourceConstants.RESOURCENAME);

        boolean atLeastOneParameterPassedForUpdate = false;

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.DESCRIPTION, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String description = this.fromApiJsonHelper.extractStringNamed(LoanProductVisibilityApiResourceConstants.DESCRIPTION, element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.DESCRIPTION).value(description).notExceedingLengthOf(150).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LOANPRODUCT, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] loanProducts = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.LOANPRODUCT,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.LOANPRODUCT).value(loanProducts).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] clientClassification = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION).value(clientClassification).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.CLIENTTYPE, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] clientTypes = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.CLIENTTYPE,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.CLIENTTYPE).value(clientTypes).notNull();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductVisibilityApiResourceConstants.LEGALENUM, element)) {
            atLeastOneParameterPassedForUpdate = true;
            final String[] legalEnums = this.fromApiJsonHelper.extractArrayNamed(LoanProductVisibilityApiResourceConstants.LEGALENUM,
                    element);
            baseDataValidator.reset().parameter(LoanProductVisibilityApiResourceConstants.LEGALENUM).value(legalEnums).notNull();
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
