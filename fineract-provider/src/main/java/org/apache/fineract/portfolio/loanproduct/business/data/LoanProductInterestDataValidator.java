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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
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
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductInterestApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoanProductInterestDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> CREATE_REQUEST_DATA_PARAMETERS = LoanProductInterestApiResourceConstants.REQUEST_DATA_PARAMETERS;
    private static final Set<String> UPDATE_REQUEST_DATA_PARAMETERS = LoanProductInterestApiResourceConstants.REQUEST_UPDATE_DATA_PARAMETERS;

    @Autowired
    public LoanProductInterestDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json, final boolean isUpdate) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        if (isUpdate) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_REQUEST_DATA_PARAMETERS);
        } else {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_REQUEST_DATA_PARAMETERS);
        }
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(LoanProductInterestApiResourceConstants.RESOURCENAME);

        if (isUpdate == false || this.fromApiJsonHelper.parameterExists(LoanProductInterestApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(LoanProductInterestApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(LoanProductInterestApiResourceConstants.NAME).value(name).notBlank();
        }

        if (isUpdate == false || this.fromApiJsonHelper.parameterExists(LoanProductInterestApiResourceConstants.LOANPRODUCTID, element)) {
            final long LOANPRODUCTID = this.fromApiJsonHelper.extractLongNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTID,
                    element);
            baseDataValidator.reset().parameter(LoanProductInterestApiResourceConstants.LOANPRODUCTID).value(LOANPRODUCTID).notBlank()
                    .longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element)) {
            final JsonArray LOANPRODUCTINTERESTCONFIGDATA = this.fromApiJsonHelper
                    .extractJsonArrayNamed(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA, element);
            baseDataValidator.reset().parameter(LoanProductInterestApiResourceConstants.LOANPRODUCTINTERESTCONFIGDATA)
                    .value(LOANPRODUCTINTERESTCONFIGDATA).notNull().jsonArrayNotEmpty();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
