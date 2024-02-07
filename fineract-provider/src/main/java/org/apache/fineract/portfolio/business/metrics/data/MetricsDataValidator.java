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
package org.apache.fineract.portfolio.business.metrics.data;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
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
import org.apache.fineract.portfolio.business.metrics.api.MetricsApiResourceConstants;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import static org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants.expectedDisbursementDateParameterName;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricsDataValidator {

    private final FromJsonHelper fromApiJsonHelper;
    private static final Set<String> LOAN_ACTION_DATA_PARAMETERS = MetricsApiResourceConstants.LOAN_ACTION_DATA_PARAMETERS;
    private static final Set<String> OVERDRAFT_ACTION_DATA_PARAMETERS = MetricsApiResourceConstants.OVERDRAFT_ACTION_DATA_PARAMETERS;

    @Autowired
    public MetricsDataValidator(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForLoanApprovalUndoReject(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LOAN_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MetricsApiResourceConstants.RESOURCENAME);

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParamName).value(note).notBlank();

        if (this.fromApiJsonHelper.parameterExists(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element)) {
            final Long undoToMetricsId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element);
            baseDataValidator.reset().parameter(MetricsApiResourceConstants.UNDO_TO_METRICS_ID).value(undoToMetricsId).notBlank()
                    .integerGreaterThanZero();
        }
        if (this.fromApiJsonHelper.parameterExists(SavingsApiConstants.paymentTypeIdParamName, element)) {
            final Long paymentTypeId = this.fromApiJsonHelper.extractLongNamed(SavingsApiConstants.paymentTypeIdParamName, element);
            baseDataValidator.reset().parameter(SavingsApiConstants.paymentTypeIdParamName).value(paymentTypeId).notBlank()
                    .integerGreaterThanZero();
        }

        if (this.fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, element)) {
            final String expectedDisbursementDateStr = this.fromApiJsonHelper.extractStringNamed(expectedDisbursementDateParameterName,
                    element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDateStr).notBlank();

            final LocalDate expectedDisbursementDate = this.fromApiJsonHelper.extractLocalDateNamed(expectedDisbursementDateParameterName, element);
            baseDataValidator.reset().parameter(expectedDisbursementDateParameterName).value(expectedDisbursementDate).notNull();
        }

        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        baseDataValidator.reset().parameter(MetricsApiResourceConstants.LOAN_ID).value(loanId).notBlank().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForLoanAssign(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, LOAN_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MetricsApiResourceConstants.RESOURCENAME);

        // final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        // baseDataValidator.reset().parameter(LoanApiConstants.noteParamName).value(note).notBlank();
        final Long loanId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.LOAN_ID, element);
        baseDataValidator.reset().parameter(MetricsApiResourceConstants.LOAN_ID).value(loanId).notBlank().integerGreaterThanZero();

        final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).notBlank().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForOverdraftApprovalUndoReject(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, OVERDRAFT_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MetricsApiResourceConstants.RESOURCENAME);

        final String note = this.fromApiJsonHelper.extractStringNamed(LoanApiConstants.noteParamName, element);
        baseDataValidator.reset().parameter(LoanApiConstants.noteParamName).value(note).notBlank();

        if (this.fromApiJsonHelper.parameterExists(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element)) {
            final Long undoToMetricsId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.UNDO_TO_METRICS_ID, element);
            baseDataValidator.reset().parameter(MetricsApiResourceConstants.UNDO_TO_METRICS_ID).value(undoToMetricsId).notBlank()
                    .integerGreaterThanZero();
        }

        final Long overdraftId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.OVERDRAFT_ID, element);
        baseDataValidator.reset().parameter(MetricsApiResourceConstants.OVERDRAFT_ID).value(overdraftId).notBlank().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    public void validateForOverdraftAssign(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {
        }.getType();

        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, OVERDRAFT_ACTION_DATA_PARAMETERS);
        final JsonElement element = this.fromApiJsonHelper.parse(json);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();

        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(MetricsApiResourceConstants.RESOURCENAME);

        final Long overdraftId = this.fromApiJsonHelper.extractLongNamed(MetricsApiResourceConstants.OVERDRAFT_ID, element);
        baseDataValidator.reset().parameter(MetricsApiResourceConstants.OVERDRAFT_ID).value(overdraftId).notBlank().integerGreaterThanZero();

        final Long staffId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.staffIdParamName, element);
        baseDataValidator.reset().parameter(ClientApiConstants.staffIdParamName).value(staffId).notBlank().integerGreaterThanZero();

        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
            //
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

}
