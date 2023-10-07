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
package org.apache.fineract.portfolio.account.data.business;

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
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.api.business.AccountTransfersBusinessApiConstants;
import static org.apache.fineract.portfolio.account.api.business.AccountTransfersBusinessApiConstants.SINGLE_TEMPLATE_DATA_PARAMETERS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class AccountTransfersBusinessDataValidator {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public AccountTransfersBusinessDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForTemplate(final String json) {

        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        final Set<String> supportedParameters = SINGLE_TEMPLATE_DATA_PARAMETERS;

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(AccountTransfersBusinessApiConstants.ACCOUNT_TRANSFER_RESOURCE_NAME);

        final String keyParam = AccountTransfersBusinessApiConstants.keyParam;
        if (this.fromApiJsonHelper.parameterExists(keyParam, element)) {
            final String key = this.fromApiJsonHelper.extractStringNamed(keyParam, element);
            baseDataValidator.reset().parameter(keyParam).value(key).notBlank();
        }

        final String valueParam = AccountTransfersBusinessApiConstants.valueParam;
        if (this.fromApiJsonHelper.parameterExists(valueParam, element)) {
            final String value = this.fromApiJsonHelper.extractStringNamed(valueParam, element);
            baseDataValidator.reset().parameter(valueParam).value(value).notBlank();
        }

        final String fromAccountIdParam = AccountTransfersBusinessApiConstants.fromAccountIdParamName;
        final Long fromAccountId = this.fromApiJsonHelper.extractLongNamed(fromAccountIdParam, element);
        baseDataValidator.reset().parameter(valueParam).value(fromAccountId).notNull().longGreaterThanZero();

        final String toAccountTypeParam = AccountTransfersBusinessApiConstants.toAccountTypeParamName;
        if (this.fromApiJsonHelper.parameterExists(toAccountTypeParam, element)) {
            final Long toAccountType = this.fromApiJsonHelper.extractLongNamed(toAccountTypeParam, element);
            baseDataValidator.reset().parameter(valueParam).value(toAccountType).notNull().isOneOfTheseValues(PortfolioAccountType.LOAN.getValue(), PortfolioAccountType.SAVINGS.getValue());
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
