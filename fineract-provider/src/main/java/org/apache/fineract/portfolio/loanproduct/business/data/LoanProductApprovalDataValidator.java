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
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.business.employer.api.EmployerApiResourceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoanProductApprovalDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> CREATE_EMPLOYER_REQUEST_DATA_PARAMETERS = EmployerApiResourceConstants.REQUEST_DATA_PARAMETERS;
    private static final Set<String> UPDATE_EMPLOYER_REQUEST_DATA_PARAMETERS = EmployerApiResourceConstants.REQUEST_UPDATE_DATA_PARAMETERS;

    @Autowired
    public LoanProductApprovalDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json, final boolean isUpdate) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();
        if (isUpdate) {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_EMPLOYER_REQUEST_DATA_PARAMETERS);
        } else {
            this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_EMPLOYER_REQUEST_DATA_PARAMETERS);
        }
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmployerApiResourceConstants.RESOURCENAME);

        // NAME, SLUG, RCNUMBER, STATEID, COUNTRYID,
        // LGAID, OFFICEADDRESS, NEARESTLANDMARK, ACTIVE, MOBILE_NO, EMAIL_ADDRESS, EMAIL_EXTENSION, CONTACT_PERSON,
        // INDUSTRYID, CLIENT_CLASSIFICATION_ID, BUSINESSID, EXTERNALID, STAFF_ID
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NAME).value(name).notBlank();
        }
        final Long clientClassificationId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID,
                element);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.CLIENT_CLASSIFICATION_ID).value(clientClassificationId).notBlank()
                .integerGreaterThanZero();

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SLUG, element)) {
            final String slug = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SLUG, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SLUG).value(slug).ignoreIfNull().notExceedingLengthOf(20);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.STAFF_ID, element)) {
            final Long staffId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STAFF_ID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.STAFF_ID).value(staffId).notExceedingLengthOf(150);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.RCNUMBER, element)) {
            final String RCNUMBER = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.RCNUMBER, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.RCNUMBER).value(RCNUMBER).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element) != null) {
            final long STATEID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.STATEID).value(STATEID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element) != null) {
            final long LGAID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.LGAID).value(LGAID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.COUNTRYID, element) != null) {
            final long COUNTRYID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.COUNTRYID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.COUNTRYID).value(COUNTRYID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element) != null) {
            final long INDUSTRYID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.INDUSTRYID).value(INDUSTRYID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.OFFICEADDRESS, element)) {
            final String OFFICEADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.OFFICEADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.OFFICEADDRESS).value(OFFICEADDRESS).ignoreIfNull()
                    .notExceedingLengthOf(225);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NEARESTLANDMARK, element)) {
            final String NEARESTLANDMARK = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NEARESTLANDMARK, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NEARESTLANDMARK).value(NEARESTLANDMARK).notBlank()
                    .notExceedingLengthOf(225);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.MOBILE_NO, element)) {
            final String MOBILE_NO = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.MOBILE_NO, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.MOBILE_NO).value(MOBILE_NO).notBlank();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element) != null) {
            final Long businessId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.BUSINESSID).value(businessId).notBlank()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EXTERNALID, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EXTERNALID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EXTERNALID).value(externalId).notBlank()
                    .notExceedingLengthOf(100);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.CONTACT_PERSON, element)) {
            final String CONTACT_PERSON = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.CONTACT_PERSON, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.CONTACT_PERSON).value(CONTACT_PERSON).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_EXTENSION, element)) {
            final String EMAIL_EXTENSION = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_EXTENSION).value(EMAIL_EXTENSION).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_ADDRESS, element)) {
            final String EMAIL_ADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_ADDRESS).value(EMAIL_ADDRESS).notBlank().isValidEmail();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.ACTIVE, element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.ACTIVE, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.ACTIVE).value(active).validateForBooleanValue();
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
