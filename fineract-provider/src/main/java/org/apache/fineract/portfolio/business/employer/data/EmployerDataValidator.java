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
package org.apache.fineract.portfolio.business.employer.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class EmployerDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> CREATE_EMPLOYER_REQUEST_DATA_PARAMETERS = EmployerApiResourceConstants.REQUEST_DATA_PARAMETERS;

    private static final Set<String> UPDATE_EMPLOYER_REQUEST_DATA_PARAMETERS = EmployerApiResourceConstants.REQUEST_DATA_PARAMETERS;

    @Autowired
    public EmployerDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, CREATE_EMPLOYER_REQUEST_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmployerApiResourceConstants.resourceNameForPermissions);

        // EmployerApiResourceConstants.clientTypeIdParamName: IS USED TO CREATE CLIENT TYPE FOR EMPLOYER OWNING AN
        // ACCOUNT BY DEFAULT
        final Long clientTypeId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.clientTypeIdParamName, element);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.clientTypeIdParamName).value(clientTypeId).notNull()
                .integerGreaterThanZero();

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element) != null) {
            final Long businessId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.BUSINESSID).value(businessId).notNull()
                    .integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.externalId, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.externalId, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.externalId).value(externalId).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.MOBILE_NO, element)) {
            final String MOBILE_NO = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.MOBILE_NO, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.MOBILE_NO).value(MOBILE_NO).ignoreIfNull().notBlank()
                    .validateNigeriaMobile(true);
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.CONTACT_PERSON, element)) {
            final String CONTACT_PERSON = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.CONTACT_PERSON, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.CONTACT_PERSON).value(CONTACT_PERSON).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SOURCENAME, element)) {
            final String SOURCENAME = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SOURCENAME, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SOURCENAME).value(SOURCENAME).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_EXTENSION, element)) {
            final String EMAIL_EXTENSION = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_EXTENSION).value(EMAIL_EXTENSION).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_ADDRESS, element)) {
            final String EMAIL_ADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_ADDRESS).value(EMAIL_ADDRESS).notBlank()
                    .emailValidator();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NAME).value(name).notBlank();
        }
        /*
         * else { baseDataValidator.reset().parameter(EmployerApiResourceConstants.NAME).value("").mandatoryFields(); }
         */

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SLUG, element)) {
            final String slug = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SLUG, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SLUG).value(slug).ignoreIfNull().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.RCNUMBER, element)) {
            final String RCNUMBER = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.RCNUMBER, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.RCNUMBER).value(RCNUMBER).notExceedingLengthOf(500);
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element) != null) {
            final long STATEID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.STATEID).value(STATEID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.PARENTID, element) != null) {
            final long PARENTID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.PARENTID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.PARENTID).value(PARENTID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.SECTORID, element) != null) {
            final long SECTORID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.SECTORID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SECTORID).value(SECTORID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element) != null) {
            final long INDUSTRYID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.INDUSTRYID).value(INDUSTRYID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element) != null) {
            final long LGAID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.LGAID).value(LGAID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.OFFICEADDRESS, element)) {
            final String OFFICEADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.OFFICEADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.OFFICEADDRESS).value(OFFICEADDRESS).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NEARESTLANDMARK, element)) {
            final String NEARESTLANDMARK = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NEARESTLANDMARK, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NEARESTLANDMARK).value(NEARESTLANDMARK).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.ACTIVE, element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.ACTIVE, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.ACTIVE).value(active).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SHOW_SIGNATURE, element)) {
            final Boolean showSignature = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.SHOW_SIGNATURE, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SHOW_SIGNATURE).value(showSignature).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    public void validateNameUpdateOfEmployer(final String newName, final String oldName) {

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmployerApiResourceConstants.resourceNameForPermissions);

        baseDataValidator.reset().parameter("New Fullname").value(newName).notNull();
        baseDataValidator.reset().parameter("Old Fullname").value(oldName).notNull();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForUpdate(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, UPDATE_EMPLOYER_REQUEST_DATA_PARAMETERS);

        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmployerApiResourceConstants.resourceNameForPermissions);

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.externalId, element)) {
            final String externalId = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.externalId, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.externalId).value(externalId).notBlank();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.MOBILE_NO, element)) {
            final String MOBILE_NO = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.MOBILE_NO, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.MOBILE_NO).value(MOBILE_NO).ignoreIfNull().notBlank()
                    .validateNigeriaMobile(true);
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.CONTACT_PERSON, element)) {
            final String CONTACT_PERSON = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.CONTACT_PERSON, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.CONTACT_PERSON).value(CONTACT_PERSON).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SOURCENAME, element)) {
            final String SOURCENAME = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SOURCENAME, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SOURCENAME).value(SOURCENAME).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_EXTENSION, element)) {
            final String EMAIL_EXTENSION = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_EXTENSION, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_EXTENSION).value(EMAIL_EXTENSION).notBlank();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.EMAIL_ADDRESS, element)) {
            final String EMAIL_ADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.EMAIL_ADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMAIL_ADDRESS).value(EMAIL_ADDRESS).notBlank()
                    .emailValidator();
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NAME, element)) {
            final String name = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NAME, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NAME).value(name);
        }
        /*
         * else { baseDataValidator.reset().parameter(EmployerApiResourceConstants.NAME).value("").mandatoryFields(); }
         */

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SLUG, element)) {
            final String slug = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.SLUG, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SLUG).value(slug).ignoreIfNull().notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.RCNUMBER, element)) {
            final String RCNUMBER = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.RCNUMBER, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.RCNUMBER).value(RCNUMBER).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element) != null) {
            final long STATEID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.STATEID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.STATEID).value(STATEID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.PARENTID, element) != null) {
            final long PARENTID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.PARENTID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.PARENTID).value(PARENTID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.SECTORID, element) != null) {
            final long SECTORID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.SECTORID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SECTORID).value(SECTORID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.clientTypeIdParamName, element) != null) {
            final long clientTypeId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.clientTypeIdParamName, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.clientTypeIdParamName).value(clientTypeId).notBlank()
                    .longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element) != null) {
            final long businessId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.BUSINESSID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.BUSINESSID).value(businessId).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element) != null) {
            final long INDUSTRYID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.INDUSTRYID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.INDUSTRYID).value(INDUSTRYID).notBlank().longGreaterThanZero();
        }
        if (this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element) != null) {
            final long LGAID = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.LGAID, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.LGAID).value(LGAID).notBlank().longGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.OFFICEADDRESS, element)) {
            final String OFFICEADDRESS = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.OFFICEADDRESS, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.OFFICEADDRESS).value(OFFICEADDRESS).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }
        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.NEARESTLANDMARK, element)) {
            final String NEARESTLANDMARK = this.fromApiJsonHelper.extractStringNamed(EmployerApiResourceConstants.NEARESTLANDMARK, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.NEARESTLANDMARK).value(NEARESTLANDMARK).ignoreIfNull()
                    .notExceedingLengthOf(500);
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.ACTIVE, element)) {
            final Boolean active = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.ACTIVE, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.ACTIVE).value(active).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.SHOW_SIGNATURE, element)) {
            final Boolean showSignature = this.fromApiJsonHelper.extractBooleanNamed(EmployerApiResourceConstants.SHOW_SIGNATURE, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.SHOW_SIGNATURE).value(showSignature).validateForBooleanValue();
        }

        throwExceptionIfValidationWarningsExist(dataValidationErrors);

    }

    public void validateForCreateEmployerLoanProduct(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
                EmployerApiResourceConstants.REQUEST_EMPLOYER_LOAN_PRODUCTS_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(EmployerApiResourceConstants.resourceNameForPermissions);

        final Long loanProductId = this.fromApiJsonHelper.extractLongNamed(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_ID, element);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_ID).value(loanProductId).notNull()
                .integerGreaterThanZero();

        final JsonObject topLevelJsonElement = element.getAsJsonObject();
        final Locale locale = this.fromApiJsonHelper.extractLocaleParameter(topLevelJsonElement);

        final BigDecimal downPaymentLimit = this.fromApiJsonHelper.extractBigDecimalNamed("downPaymentLimit", element, locale);
        baseDataValidator.reset().parameter("downPaymentLimit").value(downPaymentLimit).ignoreIfNull().zeroOrPositiveAmount();

        final BigDecimal maxNominalInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalNamed("maxNominalInterestRatePerPeriod",
                element, locale);
        baseDataValidator.reset().parameter("maxNominalInterestRatePerPeriod").value(maxNominalInterestRatePerPeriod).ignoreIfNull()
                .positiveAmount();

        final BigDecimal minNominalInterestRatePerPeriod = this.fromApiJsonHelper.extractBigDecimalNamed("minNominalInterestRatePerPeriod",
                element, locale);
        baseDataValidator.reset().parameter("minNominalInterestRatePerPeriod").value(minNominalInterestRatePerPeriod).ignoreIfNull()
                .positiveAmount();

        final BigDecimal interestRate = this.fromApiJsonHelper
                .extractBigDecimalNamed(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_INTEREST, element, locale);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_INTEREST).value(interestRate).ignoreIfNull()
                .positiveAmount();

        final BigDecimal maxPrincipal = this.fromApiJsonHelper.extractBigDecimalNamed("maxPrincipal", element, locale);
        baseDataValidator.reset().parameter("maxPrincipal").value(maxPrincipal).ignoreIfNull().positiveAmount();

        final BigDecimal minPrincipal = this.fromApiJsonHelper.extractBigDecimalNamed("minPrincipal", element, locale);
        baseDataValidator.reset().parameter("minPrincipal").value(minPrincipal).ignoreIfNull().positiveAmount();

        final BigDecimal principal = this.fromApiJsonHelper
                .extractBigDecimalNamed(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_PRINCIPAL, element, locale);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_PRINCIPAL).value(principal).ignoreIfNull()
                .positiveAmount();

        // final BigDecimal dsr =
        // this.fromApiJsonHelper.extractBigDecimalNamed(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_DSR,element,
        // locale);
        // baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_DSR).value(dsr).ignoreIfNull().positiveAmount();
        final Long maxNumberOfRepayments = this.fromApiJsonHelper.extractLongNamed("maxNumberOfRepayments", element);
        baseDataValidator.reset().parameter("maxNumberOfRepayments").value(maxNumberOfRepayments).ignoreIfNull().integerGreaterThanZero();

        final Long minNumberOfRepayments = this.fromApiJsonHelper.extractLongNamed("minNumberOfRepayments", element);
        baseDataValidator.reset().parameter("minNumberOfRepayments").value(minNumberOfRepayments).ignoreIfNull().integerGreaterThanZero();

        final Long termFrequency = this.fromApiJsonHelper
                .extractLongNamed(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_TERM_FREQUENCY, element);
        baseDataValidator.reset().parameter(EmployerApiResourceConstants.EMPLOYER_LOAN_PRODUCT_TERM_FREQUENCY).value(termFrequency)
                .ignoreIfNull().integerGreaterThanZero();

        final Boolean downPaymentPaidFull = this.fromApiJsonHelper.extractBooleanNamed("downPaymentPaidFull", element);
        if (this.fromApiJsonHelper.parameterExists("downPaymentPaidFull", element)) {
            baseDataValidator.reset().parameter("downPaymentPaidFull").value(downPaymentPaidFull).validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists("noCharge", element)) {
            final Boolean noCharge = this.fromApiJsonHelper.extractBooleanNamed("noCharge", element);
            baseDataValidator.reset().parameter("noCharge").value(noCharge).validateForBooleanValue();
        }
        final Boolean acceptDisbursementServiceFeeFromExternal = this.fromApiJsonHelper
                .extractBooleanNamed("acceptDisbursementServiceFeeFromExternal", element);
        if (this.fromApiJsonHelper.parameterExists("acceptDisbursementServiceFeeFromExternal", element)) {
            baseDataValidator.reset().parameter("acceptDisbursementServiceFeeFromExternal").value(acceptDisbursementServiceFeeFromExternal)
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.teamLeadDisburseParamName, element)) {
            final Boolean teamLeadDisburse = this.fromApiJsonHelper
                    .extractBooleanNamed(EmployerApiResourceConstants.teamLeadDisburseParamName, element);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.teamLeadDisburseParamName).value(teamLeadDisburse)
                    .validateForBooleanValue();
        }

        if (this.fromApiJsonHelper.parameterExists(EmployerApiResourceConstants.teamLeadMaxDisburseParamName, element)) {
            final BigDecimal teamLeadMaxDisburse = this.fromApiJsonHelper
                    .extractBigDecimalNamed(EmployerApiResourceConstants.teamLeadMaxDisburseParamName, element, locale);
            baseDataValidator.reset().parameter(EmployerApiResourceConstants.teamLeadMaxDisburseParamName).value(teamLeadMaxDisburse)
                    .ignoreIfNull().zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxAge", element)) {
            // maxAge, minNetPay, topUpNumberOfRepayment, minServiceYear, maxServiceYear, salesReview, underwriterReview
            final Integer maxAge = this.fromApiJsonHelper.extractIntegerNamed("maxAge", element, Locale.getDefault());
            baseDataValidator.reset().parameter("maxAge").value(maxAge).ignoreIfNull().integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists("minServiceYear", element)) {
            final BigDecimal minServiceYear = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("minServiceYear", element);
            baseDataValidator.reset().parameter("minServiceYear").value(minServiceYear).ignoreIfNull().zeroOrPositiveAmount();
        }
        if (this.fromApiJsonHelper.parameterExists("maxServiceYear", element)) {
            final Integer maxServiceYear = this.fromApiJsonHelper.extractIntegerNamed("maxServiceYear", element, Locale.getDefault());
            baseDataValidator.reset().parameter("maxServiceYear").value(maxServiceYear).ignoreIfNull().integerGreaterThanZero();
        }
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

}
