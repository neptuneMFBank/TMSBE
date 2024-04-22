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
package org.apache.fineract.portfolio.business.merchant.inventory.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryValidator {

    public static final String RESOURCE_NAME = "INVENTORY";

    public static final String nameParamName = "name";
    public static final String descriptionParamName = "description";
    public static final String priceParamName = "price";
    public static final String skuCodeParamName = "skuCode";
    public static final String discountRateParamName = "discountRate";
    public static final String localeParamName = "locale";
    public static final String clientIdParamName = "clientId";

    protected static final Set<String> supportedParams = new HashSet<>(Arrays.asList(nameParamName, descriptionParamName, priceParamName,
            skuCodeParamName, discountRateParamName, localeParamName, clientIdParamName));

    protected static final Set<String> supportedParamsForUpdate = new HashSet<>(Arrays.asList(descriptionParamName, priceParamName,
            skuCodeParamName, discountRateParamName, localeParamName));

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public InventoryValidator(FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateCreate(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, InventoryValidator.supportedParams);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(InventoryValidator.RESOURCE_NAME);

        final String name = this.fromApiJsonHelper.extractStringNamed(InventoryValidator.nameParamName, element);
        baseDataValidator.reset().parameter(InventoryValidator.nameParamName).value(name).notBlank().notExceedingLengthOf(100);

        final String description = this.fromApiJsonHelper.extractStringNamed(InventoryValidator.descriptionParamName, element);
        baseDataValidator.reset().parameter(InventoryValidator.descriptionParamName).value(description).notBlank().notExceedingLengthOf(400);

        final BigDecimal price = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InventoryValidator.priceParamName, element);
        baseDataValidator.reset().parameter(InventoryValidator.priceParamName).value(price).notNull().positiveAmount();

        final String skuCode = this.fromApiJsonHelper.extractStringNamed(InventoryValidator.skuCodeParamName, element);
        baseDataValidator.reset().parameter(InventoryValidator.skuCodeParamName).value(skuCode).notBlank().notExceedingLengthOf(100);

        if (this.fromApiJsonHelper.parameterExists(InventoryValidator.discountRateParamName, element)) {
            final BigDecimal discountRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InventoryValidator.discountRateParamName, element);
            baseDataValidator.reset().parameter(InventoryValidator.discountRateParamName).value(discountRate).notNull().zeroOrPositiveAmount();
        }
        final Long clientId = this.fromApiJsonHelper.extractLongNamed(InventoryValidator.clientIdParamName, element);
        if (clientId != null) {
            baseDataValidator.reset().parameter(InventoryValidator.clientIdParamName).value(clientId).longGreaterThanZero();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForUpdate(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, InventoryValidator.supportedParamsForUpdate);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(InventoryValidator.RESOURCE_NAME);

        if (this.fromApiJsonHelper.parameterExists(InventoryValidator.descriptionParamName, element)) {
            final String description = this.fromApiJsonHelper.extractStringNamed(InventoryValidator.descriptionParamName, element);
            baseDataValidator.reset().parameter(InventoryValidator.descriptionParamName).value(description).notBlank().notExceedingLengthOf(400);
        }
        if (this.fromApiJsonHelper.parameterExists(InventoryValidator.priceParamName, element)) {
            final BigDecimal price = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InventoryValidator.priceParamName, element);
            baseDataValidator.reset().parameter(InventoryValidator.priceParamName).value(price).notNull().positiveAmount();
        }

        if (this.fromApiJsonHelper.parameterExists(InventoryValidator.skuCodeParamName, element)) {
            final String skiCode = this.fromApiJsonHelper.extractStringNamed(InventoryValidator.skuCodeParamName, element);
            baseDataValidator.reset().parameter(InventoryValidator.skuCodeParamName).value(skiCode).notBlank().notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(InventoryValidator.discountRateParamName, element)) {
            final BigDecimal discountRate = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InventoryValidator.discountRateParamName, element);
            baseDataValidator.reset().parameter(InventoryValidator.discountRateParamName).value(discountRate).notNull().zeroOrPositiveAmount();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }
}
