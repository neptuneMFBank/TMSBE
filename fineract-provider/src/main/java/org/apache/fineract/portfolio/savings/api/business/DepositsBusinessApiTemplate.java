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
package org.apache.fineract.portfolio.savings.api.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.api.FixedDepositAccountsApiResource;
import org.apache.fineract.portfolio.savings.api.RecurringDepositAccountsApiResource;
import org.apache.fineract.portfolio.savings.api.SavingsAccountsApiResource;
import org.apache.fineract.simplifytech.data.GeneralConstants;

public interface DepositsBusinessApiTemplate {

    final String accountChartParamName = "accountChart";
    final String isActiveChartParamName = "isActiveChart";
    final String preClosurePenalInterestOnTypeParamName = "preClosurePenalInterestOnType";

    String termFrequencyParameterName = "termFrequency";
    String termPeriodFrequencyTypeParameterName = "termPeriodFrequencyType";
    String allowPartialPeriodInterestCalcualtionParameterName = "allowPartialPeriodInterestCalcualtion";
    String graceOnArrearsAgeingParameterName = "graceOnArrearsAgeing";
    String isRatesEnabledParameterName = "isRatesEnabled";
    String expectedDisbursementDateParameterName = "expectedDisbursementDate";
    String minimumDaysBetweenDisbursalAndFirstRepaymentParameterName = "minimumDaysBetweenDisbursalAndFirstRepayment";
    String startPeriodParameterName = "startPeriod";
    String endPeriodParameterName = "endPeriod";

    String activationChannelIdParam = "activationChannelId";
    String activationChannelNameParam = "activationChannelName";
    String metricsDataParam = "metricsData";

    public static String recurringTemplateConfig(final RecurringDepositAccountsApiResource recurringDepositAccountsApiResource,
            final String apiRequestBodyAsJson, final FromJsonHelper fromApiJsonHelper, final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo, final Long recurringDepositId) {

        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        // recurring savings to create json
        final JsonElement apiRequestBodyAsJsonElement = fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonObject jsonObjectLoan = apiRequestBodyAsJsonElement.getAsJsonObject();

        String locale = GeneralConstants.LOCALE_EN_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement)) {
            locale = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.localeParamName, locale);

        String dateFormat = GeneralConstants.DATEFORMET_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement)) {
            dateFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.dateFormatParamName, dateFormat);

        String monthDayFormat = GeneralConstants.DATEFORMAT_MONTHDAY_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement)) {
            monthDayFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.monthDayFormatParamName, monthDayFormat);

        final Locale localeFormat = new Locale(locale);
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat).withLocale(localeFormat);

        final Long productId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.productIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.productIdParamName, productId);

        Long clientId = null;
        Long groupId = null;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement)) {
            clientId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.clientIdParamName, clientId);
        } else if (fromApiJsonHelper.parameterExists(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement)) {
            groupId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.groupIdParamName, groupId);
        }
        String externalId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.externalIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.externalIdParamName, externalId);

        String fieldOfficerId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.fieldOfficerIdParamName,
                apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.fieldOfficerIdParamName, fieldOfficerId);

        if (recurringDepositId == null) {
            String submittedOnDate;
            if (fromApiJsonHelper.parameterExists(SavingsApiConstants.submittedOnDateParamName, apiRequestBodyAsJsonElement)) {
                submittedOnDate = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.submittedOnDateParamName,
                        apiRequestBodyAsJsonElement);
            } else {
                submittedOnDate = today.format(fmt);
            }
            jsonObjectLoan.addProperty(SavingsApiConstants.submittedOnDateParamName, submittedOnDate);
        }

        // loanTemplate config
        JsonElement loanTemplateElement;
        String loanTemplate;
        if (recurringDepositId == null) {
            loanTemplate = recurringDepositAccountsApiResource.template(clientId, groupId, productId, staffInSelectedOfficeOnly, uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        } else {
            loanTemplate = recurringDepositAccountsApiResource.retrieveOne(recurringDepositId, staffInSelectedOfficeOnly, "all", uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        }

        String expectedFirstDepositOnDate;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.expectedFirstDepositOnDateParamName, apiRequestBodyAsJsonElement)) {
            expectedFirstDepositOnDate = fromApiJsonHelper.extractStringNamed(DepositsApiConstants.expectedFirstDepositOnDateParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            expectedFirstDepositOnDate = today.format(fmt);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.expectedFirstDepositOnDateParamName, expectedFirstDepositOnDate);

        BigDecimal maxDepositTerm;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maxDepositTermParamName, apiRequestBodyAsJsonElement)) {
            maxDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.maxDepositTermParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            maxDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.maxDepositTermParamName, loanTemplateElement,
                    localeFormat);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.maxDepositTermParamName, maxDepositTerm);

        Integer maxDepositTermTypeId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maxDepositTermTypeIdParamName, apiRequestBodyAsJsonElement)) {
            maxDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maxDepositTermTypeIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement maxDepositTermType = fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.maxDepositTermType,
                    loanTemplateElement);
            maxDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName, maxDepositTermType);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.maxDepositTermTypeIdParamName, maxDepositTermTypeId);

        BigDecimal mandatoryRecommendedDepositAmount;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.mandatoryRecommendedDepositAmountParamName,
                apiRequestBodyAsJsonElement)) {
            mandatoryRecommendedDepositAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    DepositsApiConstants.mandatoryRecommendedDepositAmountParamName, apiRequestBodyAsJsonElement);
        } else {
            mandatoryRecommendedDepositAmount = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(DepositsApiConstants.mandatoryRecommendedDepositAmountParamName, loanTemplateElement);
        }
        if (mandatoryRecommendedDepositAmount != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.mandatoryRecommendedDepositAmountParamName, mandatoryRecommendedDepositAmount);
        }

        Integer depositPeriodFrequencyId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.depositPeriodFrequencyIdParamName, apiRequestBodyAsJsonElement)) {
            depositPeriodFrequencyId = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodFrequencyIdParamName, apiRequestBodyAsJsonElement);
        } else {
            depositPeriodFrequencyId = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodFrequencyIdParamName, loanTemplateElement);
        }
        if (depositPeriodFrequencyId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.depositPeriodFrequencyIdParamName, depositPeriodFrequencyId);
        }
        Integer depositPeriod;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.depositPeriodParamName, apiRequestBodyAsJsonElement)) {
            depositPeriod = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            depositPeriod = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodParamName,
                    loanTemplateElement);
        }
        if (depositPeriod != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.depositPeriodParamName, depositPeriod);
        }
        Integer recurringFrequency = null;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.recurringFrequencyParamName, apiRequestBodyAsJsonElement)) {
            recurringFrequency = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.recurringFrequencyParamName,
                    apiRequestBodyAsJsonElement);
        }
        // else {
        // recurringFrequency =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.recurringFrequencyParamName,
        // loanTemplateElement);
        // }
        if (recurringFrequency != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.recurringFrequencyParamName, recurringFrequency);
        }
        Integer recurringFrequencyType = null;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.recurringFrequencyTypeParamName, apiRequestBodyAsJsonElement)) {
            recurringFrequencyType = fromApiJsonHelper.extractIntegerNamed(DepositsApiConstants.recurringFrequencyTypeParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        }
        // else {
        // final JsonElement recurringFrequencyTypeId =
        // fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.recurringFrequencyTypeParamName,
        // loanTemplateElement);
        // recurringFrequencyType = fromApiJsonHelper.extractIntegerNamed(DepositsApiConstants.idParamName,
        // recurringFrequencyTypeId, localeFormat);
        // }
        if (recurringFrequencyType != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.recurringFrequencyTypeParamName, recurringFrequencyType);
        }

        Integer minDepositTermTypeId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.minDepositTermTypeIdParamName, apiRequestBodyAsJsonElement)) {
            minDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.minDepositTermTypeIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement minDepositTermType = fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.minDepositTermType,
                    loanTemplateElement);
            minDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName, minDepositTermType);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.minDepositTermTypeIdParamName, minDepositTermTypeId);

        BigDecimal minDepositTerm;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.minDepositTermParamName, apiRequestBodyAsJsonElement)) {
            minDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.minDepositTermParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            minDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.minDepositTermParamName, loanTemplateElement,
                    localeFormat);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.minDepositTermParamName, minDepositTerm);

        BigDecimal nominalAnnualInterestRate;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateParamName, apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    loanTemplateElement, localeFormat);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateParamName, nominalAnnualInterestRate);

        BigDecimal minOverdraftForInterestCalculation;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minOverdraftForInterestCalculationParamName,
                apiRequestBodyAsJsonElement)) {
            minOverdraftForInterestCalculation = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    SavingsApiConstants.minOverdraftForInterestCalculationParamName, apiRequestBodyAsJsonElement);
        } else {
            minOverdraftForInterestCalculation = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minOverdraftForInterestCalculationParamName, loanTemplateElement);
        }
        if (minOverdraftForInterestCalculation != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minOverdraftForInterestCalculationParamName, minOverdraftForInterestCalculation);
        }
        BigDecimal nominalAnnualInterestRateOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName,
                apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, loanTemplateElement, localeFormat);
        }
        if (nominalAnnualInterestRateOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, nominalAnnualInterestRateOverdraft);
        }
        BigDecimal minRequiredOpeningBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement)) {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement);
        } else {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, loanTemplateElement);
        }
        if (minRequiredOpeningBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minRequiredOpeningBalanceParamName, minRequiredOpeningBalance);
        }

        Integer maturityInstructionId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maturityInstructionIdParamName, apiRequestBodyAsJsonElement)) {
            maturityInstructionId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maturityInstructionIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            maturityInstructionId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maturityInstructionIdParamName,
                    loanTemplateElement);
        }
        if (maturityInstructionId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.maturityInstructionIdParamName, maturityInstructionId);
        }
        Integer interestCalculationDaysInYearType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationDaysInYearTypeParamName,
                apiRequestBodyAsJsonElement)) {
            interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    SavingsApiConstants.interestCalculationDaysInYearTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCalculationDaysInYearTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, loanTemplateElement);
            interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestCalculationDaysInYearTypeElement);
        }
        if (interestCalculationDaysInYearType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, interestCalculationDaysInYearType);
        }
        Integer interestCalculationType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.interestCalculationTypeParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCalculationTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestCalculationTypeParamName, loanTemplateElement);
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestCalculationTypeElement);
        }
        if (interestCalculationType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationTypeParamName, interestCalculationType);
        }

        Integer interestPostingPeriodType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestPostingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestPostingPeriodTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, loanTemplateElement);
            interestPostingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestPostingPeriodTypeElement);
        }
        if (interestPostingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestPostingPeriodTypeParamName, interestPostingPeriodType);
        }
        Integer interestCompoundingPeriodType = null;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCompoundingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCompoundingPeriodTypeParamName, loanTemplateElement)) {
                final JsonElement interestCompoundingPeriodTypeElement = fromApiJsonHelper
                        .extractJsonObjectNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, loanTemplateElement);
                interestCompoundingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                        interestCompoundingPeriodTypeElement);
            }
        }
        if (interestCompoundingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCompoundingPeriodTypeParamName, interestCompoundingPeriodType);
        }

        lockInPeriodFrequencyConfig(fromApiJsonHelper, apiRequestBodyAsJsonElement, loanTemplateElement, jsonObjectLoan);

        Boolean isCalendarInherited;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.isCalendarInheritedParamName, apiRequestBodyAsJsonElement)) {
            isCalendarInherited = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isCalendarInheritedParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            isCalendarInherited = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isCalendarInheritedParamName,
                    loanTemplateElement);
        }
        if (isCalendarInherited != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.isCalendarInheritedParamName, isCalendarInherited);
        }
        // Boolean withdrawalFeeForTransfers;
        // if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // apiRequestBodyAsJsonElement)) {
        // withdrawalFeeForTransfers =
        // fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // withdrawalFeeForTransfers =
        // fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // loanTemplateElement);
        // }
        // if (withdrawalFeeForTransfers != null) {
        // jsonObjectLoan.addProperty(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // withdrawalFeeForTransfers);
        // }
        // Long linkAccountId;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.linkedAccountParamName,
        // apiRequestBodyAsJsonElement)) {
        // linkAccountId = fromApiJsonHelper.extractLongNamed(DepositsApiConstants.linkedAccountParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // linkAccountId = fromApiJsonHelper.extractLongNamed(DepositsApiConstants.linkedAccountParamName,
        // loanTemplateElement);
        // }
        // if (linkAccountId != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.linkedAccountParamName, linkAccountId);
        // }
        // Boolean transferInterestToSavings;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.transferInterestToSavingsParamName,
        // apiRequestBodyAsJsonElement)) {
        // transferInterestToSavings =
        // fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.transferInterestToSavingsParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // transferInterestToSavings =
        // fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.transferInterestToSavingsParamName,
        // loanTemplateElement);
        // }
        // if (transferInterestToSavings != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.transferInterestToSavingsParamName,
        // transferInterestToSavings);
        // }

        Integer preClosurePenalInterestOnTypeId = null;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, apiRequestBodyAsJsonElement)) {
            preClosurePenalInterestOnTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, apiRequestBodyAsJsonElement);
        } else {
            if (fromApiJsonHelper.parameterExists(preClosurePenalInterestOnTypeParamName, loanTemplateElement)) {
                final JsonElement preClosurePenalInterestOnType = fromApiJsonHelper
                        .extractJsonObjectNamed(preClosurePenalInterestOnTypeParamName, loanTemplateElement);
                preClosurePenalInterestOnTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName,
                        preClosurePenalInterestOnType);
            }
        }
        if (preClosurePenalInterestOnTypeId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, preClosurePenalInterestOnTypeId);
        }
        BigDecimal preClosurePenalInterest;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalInterestParamName, apiRequestBodyAsJsonElement)) {
            preClosurePenalInterest = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.preClosurePenalInterestParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            preClosurePenalInterest = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.preClosurePenalInterestParamName,
                    loanTemplateElement, localeFormat);
        }
        if (preClosurePenalInterest != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalInterestParamName, preClosurePenalInterest);
        }

        Boolean preClosurePenalApplicable;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalApplicableParamName, apiRequestBodyAsJsonElement)) {
            preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.preClosurePenalApplicableParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.preClosurePenalApplicableParamName,
                    loanTemplateElement);
        }
        if (preClosurePenalApplicable != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalApplicableParamName, preClosurePenalApplicable);
        }
        BigDecimal overdraftLimit;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.overdraftLimitParamName, apiRequestBodyAsJsonElement)) {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName, loanTemplateElement,
                    localeFormat);
        }
        if (overdraftLimit != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.overdraftLimitParamName, overdraftLimit);
        }
        Boolean withHoldTax;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement)) {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement);
        } else {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, loanTemplateElement);
        }
        if (withHoldTax != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.withHoldTaxParamName, withHoldTax);
        }
        Boolean enforceMinRequiredBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.enforceMinRequiredBalanceParamName, apiRequestBodyAsJsonElement)) {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    loanTemplateElement);
        }
        if (enforceMinRequiredBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.enforceMinRequiredBalanceParamName, enforceMinRequiredBalance);
        }
        Boolean allowOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.allowOverdraftParamName, apiRequestBodyAsJsonElement)) {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName, loanTemplateElement);
        }
        if (allowOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.allowOverdraftParamName, allowOverdraft);
        }
        Boolean allowWithdrawal;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.allowWithdrawalParamName, apiRequestBodyAsJsonElement)) {
            allowWithdrawal = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.allowWithdrawalParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            allowWithdrawal = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.allowWithdrawalParamName, loanTemplateElement);
        }
        if (allowWithdrawal != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.allowWithdrawalParamName, allowWithdrawal);
        }
        Boolean isMandatoryDeposit;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.isMandatoryDepositParamName, apiRequestBodyAsJsonElement)) {
            isMandatoryDeposit = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isMandatoryDepositParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            isMandatoryDeposit = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isMandatoryDepositParamName,
                    loanTemplateElement);
        }
        if (isMandatoryDeposit != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.isMandatoryDepositParamName, isMandatoryDeposit);
        }
        Boolean adjustAdvanceTowardsFuturePayments;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
                apiRequestBodyAsJsonElement)) {
            adjustAdvanceTowardsFuturePayments = fromApiJsonHelper
                    .extractBooleanNamed(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName, apiRequestBodyAsJsonElement);
        } else {
            adjustAdvanceTowardsFuturePayments = fromApiJsonHelper
                    .extractBooleanNamed(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName, loanTemplateElement);
        }
        if (adjustAdvanceTowardsFuturePayments != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
                    adjustAdvanceTowardsFuturePayments);
        }

        Integer inMultiplesOfDepositTermTypeId = null;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName, apiRequestBodyAsJsonElement)) {
            inMultiplesOfDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName, apiRequestBodyAsJsonElement);
        } else {
            if (fromApiJsonHelper.parameterExists(DepositsApiConstants.inMultiplesOfDepositTermType, loanTemplateElement)) {
                final JsonElement inMultiplesOfDepositTermType = fromApiJsonHelper
                        .extractJsonObjectNamed(DepositsApiConstants.inMultiplesOfDepositTermType, loanTemplateElement);
                inMultiplesOfDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName,
                        inMultiplesOfDepositTermType);
            }
        }
        if (inMultiplesOfDepositTermTypeId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName, inMultiplesOfDepositTermTypeId);
        }

        JsonArray charts = new JsonArray();

        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.chartsParamName, apiRequestBodyAsJsonElement)) {
            charts = fromApiJsonHelper.extractJsonArrayNamed(DepositsApiConstants.chartsParamName, apiRequestBodyAsJsonElement);
        } else {
            // waiting to see if chart works without passing it

            // final JsonElement accountChart = fromApiJsonHelper.extractJsonObjectNamed(accountChartParamName,
            // loanTemplateElement);
            // String fromDate = today.toString();
            // if (fromApiJsonHelper.parameterExists(InterestRateChartApiConstants.fromDateParamName, accountChart)) {
            // final JsonArray fromDateArray =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartApiConstants.fromDateParamName, accountChart);
            // for (JsonElement x : fromDateArray) {
            // fromDate += x + "-";
            // }
            // fromDate = fromDate.substring(0, fromDate.length() - 1);
            // }
            //
            // final JsonArray chartSlabs =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartApiConstants.chartSlabs, accountChart);
            // final JsonArray chartSlabArray = new JsonArray();
            // for (JsonElement chartSlab : chartSlabs) {
            // final JsonObject chartSlabObject = new JsonObject();
            //
            // final Long fromPeriod =
            // fromApiJsonHelper.extractLongNamed(InterestRateChartSlabApiConstants.fromPeriodParamName, chartSlab);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.fromPeriodParamName, fromPeriod);
            // final BigDecimal annualInterestRate =
            // fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InterestRateChartSlabApiConstants.annualInterestRateParamName,
            // chartSlab);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.annualInterestRateParamName,
            // annualInterestRate);
            //
            // final JsonArray incentives =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartSlabApiConstants.incentivesParamName,
            // chartSlab);
            // chartSlabObject.add(InterestRateChartSlabApiConstants.incentivesParamName, incentives);
            // final JsonElement periodType =
            // fromApiJsonHelper.extractJsonObjectNamed(InterestRateChartSlabApiConstants.periodTypeParamName,
            // chartSlab);
            // final Long periodTypeId =
            // fromApiJsonHelper.extractLongNamed(InterestRateChartSlabApiConstants.idParamName, periodType);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.periodTypeParamName, periodTypeId);
            //
            // if (fromApiJsonHelper.parameterExists(InterestRateChartSlabApiConstants.localeParamName, chartSlab)) {
            // locale = fromApiJsonHelper.extractStringNamed(InterestRateChartSlabApiConstants.localeParamName,
            // chartSlab);
            // }
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.localeParamName, locale);
            // chartSlabArray.add(chartSlabObject);
            // }
            //
            // final JsonObject chartObject = new JsonObject();
            // chartObject.addProperty(InterestRateChartApiConstants.dateFormatParamName, dateFormat);
            // chartObject.addProperty(InterestRateChartApiConstants.fromDateParamName, fromDate);
            // chartObject.addProperty(isActiveChartParamName, true);
            // chartObject.addProperty("locale", locale);
            //
            // chartObject.add(InterestRateChartApiConstants.chartSlabs, chartSlabArray);
            // charts.add(chartObject);
        }
        jsonObjectLoan.add(DepositsApiConstants.chartsParamName, charts);

        JsonArray charges = new JsonArray();
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement)) {
            charges = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonArray chargesCheck = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName,
                    loanTemplateElement);
            if (chargesCheck != null && chargesCheck.isJsonArray()) {
                for (JsonElement charge : chargesCheck) {
                    final JsonObject chargesValue = new JsonObject();
                    chargesValue.addProperty(SavingsApiConstants.chargeIdParamName,
                            fromApiJsonHelper.extractLongNamed(SavingsApiConstants.chargeIdParamName, charge));
                    chargesValue.addProperty(SavingsApiConstants.amountParamName,
                            fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.amountParamName, charge, Locale.ENGLISH));
                    charges.add(chargesValue);
                }
            }
        }
        jsonObjectLoan.add(SavingsApiConstants.chargesParamName, charges);

        return jsonObjectLoan.toString();
    }

    static void lockInPeriodFrequencyConfig(final FromJsonHelper fromApiJsonHelper, final JsonElement apiRequestBodyAsJsonElement, JsonElement loanTemplateElement, final JsonObject jsonObjectLoan) {
        Integer lockinPeriodFrequency;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.lockinPeriodFrequencyParamName, apiRequestBodyAsJsonElement)) {
            lockinPeriodFrequency = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.lockinPeriodFrequencyParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            lockinPeriodFrequency = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.lockinPeriodFrequencyParamName,
                    loanTemplateElement);
        }

        Integer lockinPeriodFrequencyType = null;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, apiRequestBodyAsJsonElement)) {
            lockinPeriodFrequencyType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            //lockinPeriodFrequencyType = null;
            if (fromApiJsonHelper.parameterExists(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, loanTemplateElement)) {
                final JsonElement lockinPeriodFrequencyTypeElement
                        = fromApiJsonHelper.extractJsonObjectNamed(SavingsApiConstants.lockinPeriodFrequencyTypeParamName,
                                loanTemplateElement);

                lockinPeriodFrequencyType
                        = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                                lockinPeriodFrequencyTypeElement);
            }
        }
        if (lockinPeriodFrequency != null && lockinPeriodFrequencyType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.lockinPeriodFrequencyParamName, lockinPeriodFrequency);
            jsonObjectLoan.addProperty(SavingsApiConstants.lockinPeriodFrequencyTypeParamName, lockinPeriodFrequencyType);
        }
    }

    public static String fixedTemplateConfig(final FixedDepositAccountsApiResource fixedDepositAccountsApiResource,
            final String apiRequestBodyAsJson, final FromJsonHelper fromApiJsonHelper, final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo, final Long fixedDepositId) {

        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        // fixed savings to create json
        final JsonElement apiRequestBodyAsJsonElement = fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonObject jsonObjectLoan = apiRequestBodyAsJsonElement.getAsJsonObject();

        String locale = GeneralConstants.LOCALE_EN_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement)) {
            locale = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.localeParamName, locale);

        String dateFormat = GeneralConstants.DATEFORMET_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement)) {
            dateFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.dateFormatParamName, dateFormat);

        String monthDayFormat = GeneralConstants.DATEFORMAT_MONTHDAY_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement)) {
            monthDayFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.monthDayFormatParamName, monthDayFormat);

        final Locale localeFormat = new Locale(locale);
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat).withLocale(localeFormat);

        final Long productId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.productIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.productIdParamName, productId);

        Long clientId = null;
        Long groupId = null;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement)) {
            clientId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.clientIdParamName, clientId);
        } else if (fromApiJsonHelper.parameterExists(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement)) {
            groupId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.groupIdParamName, groupId);
        }
        String externalId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.externalIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.externalIdParamName, externalId);

        String fieldOfficerId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.fieldOfficerIdParamName,
                apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.fieldOfficerIdParamName, fieldOfficerId);

        if (fixedDepositId == null) {
            String submittedOnDate;
            if (fromApiJsonHelper.parameterExists(SavingsApiConstants.submittedOnDateParamName, apiRequestBodyAsJsonElement)) {
                submittedOnDate = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.submittedOnDateParamName,
                        apiRequestBodyAsJsonElement);
            } else {
                submittedOnDate = today.format(fmt);
            }
            jsonObjectLoan.addProperty(SavingsApiConstants.submittedOnDateParamName, submittedOnDate);
        }

        // loanTemplate config
        JsonElement loanTemplateElement;
        String loanTemplate;
        if (fixedDepositId == null) {
            loanTemplate = fixedDepositAccountsApiResource.template(clientId, groupId, productId, staffInSelectedOfficeOnly, uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        } else {
            loanTemplate = fixedDepositAccountsApiResource.retrieveOne(fixedDepositId, staffInSelectedOfficeOnly, "all", uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        }

        BigDecimal depositAmount;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.depositAmountParamName, apiRequestBodyAsJsonElement)) {
            depositAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(DepositsApiConstants.depositAmountParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            depositAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(DepositsApiConstants.depositAmountParamName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.depositAmountParamName, depositAmount);

        BigDecimal maxDepositTerm;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maxDepositTermParamName, apiRequestBodyAsJsonElement)) {
            maxDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.maxDepositTermParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            maxDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.maxDepositTermParamName, loanTemplateElement,
                    localeFormat);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.maxDepositTermParamName, maxDepositTerm);

        Integer maxDepositTermTypeId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maxDepositTermTypeIdParamName, apiRequestBodyAsJsonElement)) {
            maxDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maxDepositTermTypeIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement maxDepositTermType = fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.maxDepositTermType,
                    loanTemplateElement);
            maxDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName, maxDepositTermType);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.maxDepositTermTypeIdParamName, maxDepositTermTypeId);

        Integer minDepositTermTypeId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.minDepositTermTypeIdParamName, apiRequestBodyAsJsonElement)) {
            minDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.minDepositTermTypeIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement minDepositTermType = fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.minDepositTermType,
                    loanTemplateElement);
            minDepositTermTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName, minDepositTermType);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.minDepositTermTypeIdParamName, minDepositTermTypeId);

        BigDecimal minDepositTerm;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.minDepositTermParamName, apiRequestBodyAsJsonElement)) {
            minDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.minDepositTermParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            minDepositTerm = fromApiJsonHelper.extractBigDecimalNamed(DepositsApiConstants.minDepositTermParamName, loanTemplateElement,
                    localeFormat);
        }
        jsonObjectLoan.addProperty(DepositsApiConstants.minDepositTermParamName, minDepositTerm);

        BigDecimal nominalAnnualInterestRate;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateParamName, apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    loanTemplateElement, localeFormat);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateParamName, nominalAnnualInterestRate);

        BigDecimal minOverdraftForInterestCalculation;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minOverdraftForInterestCalculationParamName,
                apiRequestBodyAsJsonElement)) {
            minOverdraftForInterestCalculation = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    SavingsApiConstants.minOverdraftForInterestCalculationParamName, apiRequestBodyAsJsonElement);
        } else {
            minOverdraftForInterestCalculation = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minOverdraftForInterestCalculationParamName, loanTemplateElement);
        }
        if (minOverdraftForInterestCalculation != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minOverdraftForInterestCalculationParamName, minOverdraftForInterestCalculation);
        }
        BigDecimal nominalAnnualInterestRateOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName,
                apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, loanTemplateElement, localeFormat);
        }
        if (nominalAnnualInterestRateOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, nominalAnnualInterestRateOverdraft);
        }
        BigDecimal minRequiredOpeningBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement)) {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement);
        } else {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, loanTemplateElement);
        }
        if (minRequiredOpeningBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minRequiredOpeningBalanceParamName, minRequiredOpeningBalance);
        }

        Integer maturityInstructionId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.maturityInstructionIdParamName, apiRequestBodyAsJsonElement)) {
            maturityInstructionId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maturityInstructionIdParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            maturityInstructionId = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.maturityInstructionIdParamName,
                    loanTemplateElement);
        }
        if (maturityInstructionId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.maturityInstructionIdParamName, maturityInstructionId);
        }
        Integer interestCalculationDaysInYearType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationDaysInYearTypeParamName,
                apiRequestBodyAsJsonElement)) {
            interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    SavingsApiConstants.interestCalculationDaysInYearTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCalculationDaysInYearTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, loanTemplateElement);
            interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestCalculationDaysInYearTypeElement);
        }
        if (interestCalculationDaysInYearType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, interestCalculationDaysInYearType);
        }
        Integer interestCalculationType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.interestCalculationTypeParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCalculationTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestCalculationTypeParamName, loanTemplateElement);
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestCalculationTypeElement);
        }
        if (interestCalculationType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationTypeParamName, interestCalculationType);
        }

        Integer interestPostingPeriodType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestPostingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestPostingPeriodTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, loanTemplateElement);
            interestPostingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestPostingPeriodTypeElement);
        }
        if (interestPostingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestPostingPeriodTypeParamName, interestPostingPeriodType);
        }
        Integer interestCompoundingPeriodType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCompoundingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCompoundingPeriodTypeElement = fromApiJsonHelper
                    .extractJsonObjectNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, loanTemplateElement);
            interestCompoundingPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.idParamName,
                    interestCompoundingPeriodTypeElement);
        }
        if (interestCompoundingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCompoundingPeriodTypeParamName, interestCompoundingPeriodType);
        }

        Integer depositPeriodFrequencyId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.depositPeriodFrequencyIdParamName, apiRequestBodyAsJsonElement)) {
            depositPeriodFrequencyId = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodFrequencyIdParamName, apiRequestBodyAsJsonElement);
        } else {
            depositPeriodFrequencyId = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodFrequencyIdParamName, loanTemplateElement);
        }
        if (depositPeriodFrequencyId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.depositPeriodFrequencyIdParamName, depositPeriodFrequencyId);
        }
        Integer depositPeriod;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.depositPeriodParamName, apiRequestBodyAsJsonElement)) {
            depositPeriod = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            depositPeriod = fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.depositPeriodParamName,
                    loanTemplateElement);
        }
        if (depositPeriod != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.depositPeriodParamName, depositPeriod);
        }

        lockInPeriodFrequencyConfig(fromApiJsonHelper, apiRequestBodyAsJsonElement, loanTemplateElement, jsonObjectLoan); //lockinPeriodFrequencyType = null;

        // Boolean withdrawalFeeForTransfers;
        // if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // apiRequestBodyAsJsonElement)) {
        // withdrawalFeeForTransfers =
        // fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // withdrawalFeeForTransfers =
        // fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // loanTemplateElement);
        // }
        // if (withdrawalFeeForTransfers != null) {
        // jsonObjectLoan.addProperty(SavingsApiConstants.withdrawalFeeForTransfersParamName,
        // withdrawalFeeForTransfers);
        // }
        Long linkAccountId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.linkedAccountParamName, apiRequestBodyAsJsonElement)) {
            linkAccountId = fromApiJsonHelper.extractLongNamed(DepositsApiConstants.linkedAccountParamName, apiRequestBodyAsJsonElement);
        } else {
            linkAccountId = fromApiJsonHelper.extractLongNamed(DepositsApiConstants.linkedAccountParamName, loanTemplateElement);
        }
        if (linkAccountId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.linkedAccountParamName, linkAccountId);
        }
        // Boolean adjustAdvanceTowardsFuturePayments;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
        // apiRequestBodyAsJsonElement)) {
        // adjustAdvanceTowardsFuturePayments =
        // fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // adjustAdvanceTowardsFuturePayments =
        // fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
        // loanTemplateElement);
        // }
        // if (adjustAdvanceTowardsFuturePayments != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.adjustAdvanceTowardsFuturePaymentsParamName,
        // adjustAdvanceTowardsFuturePayments);
        // }
        // Boolean allowWithdrawal;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.allowWithdrawalParamName,
        // apiRequestBodyAsJsonElement)) {
        // allowWithdrawal = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.allowWithdrawalParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // allowWithdrawal = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.allowWithdrawalParamName,
        // loanTemplateElement);
        // }
        // if (allowWithdrawal != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.allowWithdrawalParamName, allowWithdrawal);
        // }
        // Boolean isMandatoryDeposit;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.isMandatoryDepositParamName,
        // apiRequestBodyAsJsonElement)) {
        // isMandatoryDeposit = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isMandatoryDepositParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // isMandatoryDeposit = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.isMandatoryDepositParamName,
        // loanTemplateElement);
        // }
        // if (isMandatoryDeposit != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.isMandatoryDepositParamName, isMandatoryDeposit);
        // }
        Boolean transferInterestToSavings;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.transferInterestToSavingsParamName, apiRequestBodyAsJsonElement)) {
            transferInterestToSavings = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.transferInterestToSavingsParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            transferInterestToSavings = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.transferInterestToSavingsParamName,
                    loanTemplateElement);
        }
        if (transferInterestToSavings != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.transferInterestToSavingsParamName, transferInterestToSavings);
        }
        // Integer inMultiplesOfDepositTermTypeId;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName,
        // apiRequestBodyAsJsonElement)) {
        // inMultiplesOfDepositTermTypeId =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // final JsonElement inMultiplesOfDepositTermType =
        // fromApiJsonHelper.extractJsonObjectNamed(DepositsApiConstants.inMultiplesOfDepositTermType,
        // loanTemplateElement);
        // inMultiplesOfDepositTermTypeId =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName,
        // inMultiplesOfDepositTermType);
        // }
        // if (inMultiplesOfDepositTermTypeId != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.inMultiplesOfDepositTermTypeIdParamName,
        // inMultiplesOfDepositTermTypeId);
        // }
        // Integer inMultiplesOfDepositTerm;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.inMultiplesOfDepositTermParamName,
        // apiRequestBodyAsJsonElement)) {
        // inMultiplesOfDepositTerm =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.inMultiplesOfDepositTermParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // inMultiplesOfDepositTerm =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.inMultiplesOfDepositTermParamName,
        // loanTemplateElement);
        // }
        // if (inMultiplesOfDepositTerm != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.inMultiplesOfDepositTermParamName, inMultiplesOfDepositTerm);
        // }
        Integer preClosurePenalInterestOnTypeId;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, apiRequestBodyAsJsonElement)) {
            preClosurePenalInterestOnTypeId = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, apiRequestBodyAsJsonElement);
        } else {
            preClosurePenalInterestOnTypeId = null;
            // final JsonElement preClosurePenalInterestOnType =
            // fromApiJsonHelper.extractJsonObjectNamed(preClosurePenalInterestOnTypeParamName, loanTemplateElement);
            // preClosurePenalInterestOnTypeId =
            // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.idParamName,
            // preClosurePenalInterestOnType);
        }
        if (preClosurePenalInterestOnTypeId != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalInterestOnTypeIdParamName, preClosurePenalInterestOnTypeId);
        }
        // Integer preClosurePenalInterest;
        // if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalInterestParamName,
        // apiRequestBodyAsJsonElement)) {
        // preClosurePenalInterest =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.preClosurePenalInterestParamName,
        // apiRequestBodyAsJsonElement);
        // } else {
        // preClosurePenalInterest =
        // fromApiJsonHelper.extractIntegerSansLocaleNamed(DepositsApiConstants.preClosurePenalInterestParamName,
        // loanTemplateElement);
        // }
        // if (preClosurePenalInterest != null) {
        // jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalInterestParamName, preClosurePenalInterest);
        // }
        Boolean preClosurePenalApplicable;
        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.preClosurePenalApplicableParamName, apiRequestBodyAsJsonElement)) {
            preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.preClosurePenalApplicableParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            preClosurePenalApplicable = fromApiJsonHelper.extractBooleanNamed(DepositsApiConstants.preClosurePenalApplicableParamName,
                    loanTemplateElement);
        }
        if (preClosurePenalApplicable != null) {
            jsonObjectLoan.addProperty(DepositsApiConstants.preClosurePenalApplicableParamName, preClosurePenalApplicable);
        }
        BigDecimal overdraftLimit;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.overdraftLimitParamName, apiRequestBodyAsJsonElement)) {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName, loanTemplateElement,
                    localeFormat);
        }
        if (overdraftLimit != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.overdraftLimitParamName, overdraftLimit);
        }
        Boolean withHoldTax;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement)) {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement);
        } else {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, loanTemplateElement);
        }
        if (withHoldTax != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.withHoldTaxParamName, withHoldTax);
        }
        Boolean enforceMinRequiredBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.enforceMinRequiredBalanceParamName, apiRequestBodyAsJsonElement)) {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    loanTemplateElement);
        }
        if (enforceMinRequiredBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.enforceMinRequiredBalanceParamName, enforceMinRequiredBalance);
        }
        Boolean allowOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.allowOverdraftParamName, apiRequestBodyAsJsonElement)) {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName, loanTemplateElement);
        }
        if (allowOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.allowOverdraftParamName, allowOverdraft);
        }

        JsonArray charts = new JsonArray();

        if (fromApiJsonHelper.parameterExists(DepositsApiConstants.chartsParamName, apiRequestBodyAsJsonElement)) {
            charts = fromApiJsonHelper.extractJsonArrayNamed(DepositsApiConstants.chartsParamName, apiRequestBodyAsJsonElement);
        } else {
            // waiting to see if chart works without passing it

            // final JsonElement accountChart = fromApiJsonHelper.extractJsonObjectNamed(accountChartParamName,
            // loanTemplateElement);
            // String fromDate = today.toString();
            // if (fromApiJsonHelper.parameterExists(InterestRateChartApiConstants.fromDateParamName, accountChart)) {
            // final JsonArray fromDateArray =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartApiConstants.fromDateParamName, accountChart);
            // for (JsonElement x : fromDateArray) {
            // fromDate += x + "-";
            // }
            // fromDate = fromDate.substring(0, fromDate.length() - 1);
            // }
            //
            // final JsonArray chartSlabs =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartApiConstants.chartSlabs, accountChart);
            // final JsonArray chartSlabArray = new JsonArray();
            // for (JsonElement chartSlab : chartSlabs) {
            // final JsonObject chartSlabObject = new JsonObject();
            //
            // final Long fromPeriod =
            // fromApiJsonHelper.extractLongNamed(InterestRateChartSlabApiConstants.fromPeriodParamName, chartSlab);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.fromPeriodParamName, fromPeriod);
            // final BigDecimal annualInterestRate =
            // fromApiJsonHelper.extractBigDecimalWithLocaleNamed(InterestRateChartSlabApiConstants.annualInterestRateParamName,
            // chartSlab);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.annualInterestRateParamName,
            // annualInterestRate);
            //
            // final JsonArray incentives =
            // fromApiJsonHelper.extractJsonArrayNamed(InterestRateChartSlabApiConstants.incentivesParamName,
            // chartSlab);
            // chartSlabObject.add(InterestRateChartSlabApiConstants.incentivesParamName, incentives);
            // final JsonElement periodType =
            // fromApiJsonHelper.extractJsonObjectNamed(InterestRateChartSlabApiConstants.periodTypeParamName,
            // chartSlab);
            // final Long periodTypeId =
            // fromApiJsonHelper.extractLongNamed(InterestRateChartSlabApiConstants.idParamName, periodType);
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.periodTypeParamName, periodTypeId);
            //
            // if (fromApiJsonHelper.parameterExists(InterestRateChartSlabApiConstants.localeParamName, chartSlab)) {
            // locale = fromApiJsonHelper.extractStringNamed(InterestRateChartSlabApiConstants.localeParamName,
            // chartSlab);
            // }
            // chartSlabObject.addProperty(InterestRateChartSlabApiConstants.localeParamName, locale);
            // chartSlabArray.add(chartSlabObject);
            // }
            //
            // final JsonObject chartObject = new JsonObject();
            // chartObject.addProperty(InterestRateChartApiConstants.dateFormatParamName, dateFormat);
            // chartObject.addProperty(InterestRateChartApiConstants.fromDateParamName, fromDate);
            // chartObject.addProperty(isActiveChartParamName, true);
            // chartObject.addProperty("locale", locale);
            //
            // chartObject.add(InterestRateChartApiConstants.chartSlabs, chartSlabArray);
            // charts.add(chartObject);
        }
        jsonObjectLoan.add(DepositsApiConstants.chartsParamName, charts);

        JsonArray charges = new JsonArray();
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement)) {
            charges = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonArray chargesCheck = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName,
                    loanTemplateElement);
            if (chargesCheck != null && chargesCheck.isJsonArray()) {
                for (JsonElement charge : chargesCheck) {
                    final JsonObject chargesValue = new JsonObject();
                    chargesValue.addProperty(SavingsApiConstants.chargeIdParamName,
                            fromApiJsonHelper.extractLongNamed(SavingsApiConstants.chargeIdParamName, charge));
                    chargesValue.addProperty(SavingsApiConstants.amountParamName,
                            fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.amountParamName, charge, Locale.ENGLISH));
                    charges.add(chargesValue);
                }
            }
        }
        jsonObjectLoan.add(SavingsApiConstants.chargesParamName, charges);

        return jsonObjectLoan.toString();
    }

    public static String savingsTemplateConfig(final SavingsAccountsApiResource savingsAccountsApiResource,
            final String apiRequestBodyAsJson, final FromJsonHelper fromApiJsonHelper, final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo, final Long savingsId) {

        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        // savings to create json
        final JsonElement apiRequestBodyAsJsonElement = fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonObject jsonObjectLoan = apiRequestBodyAsJsonElement.getAsJsonObject();

        String locale = GeneralConstants.LOCALE_EN_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement)) {
            locale = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.localeParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.localeParamName, locale);

        String dateFormat = GeneralConstants.DATEFORMET_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement)) {
            dateFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.dateFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.dateFormatParamName, dateFormat);

        String monthDayFormat = GeneralConstants.DATEFORMAT_MONTHDAY_DEFAULT;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement)) {
            monthDayFormat = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.monthDayFormatParamName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.monthDayFormatParamName, monthDayFormat);

        final Locale localeFormat = new Locale(locale);
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat).withLocale(localeFormat);

        final Long productId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.productIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.productIdParamName, productId);

        Long clientId = null;
        Long groupId = null;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement)) {
            clientId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.clientIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.clientIdParamName, clientId);
        } else if (fromApiJsonHelper.parameterExists(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement)) {
            groupId = fromApiJsonHelper.extractLongNamed(SavingsApiConstants.groupIdParamName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(SavingsApiConstants.groupIdParamName, groupId);
        }
        String externalId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.externalIdParamName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.externalIdParamName, externalId);

        String fieldOfficerId = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.fieldOfficerIdParamName,
                apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(SavingsApiConstants.fieldOfficerIdParamName, fieldOfficerId);

        if (savingsId == null) {
            String submittedOnDate;
            if (fromApiJsonHelper.parameterExists(SavingsApiConstants.submittedOnDateParamName, apiRequestBodyAsJsonElement)) {
                submittedOnDate = fromApiJsonHelper.extractStringNamed(SavingsApiConstants.submittedOnDateParamName,
                        apiRequestBodyAsJsonElement);
            } else {
                submittedOnDate = today.format(fmt);
            }
            jsonObjectLoan.addProperty(SavingsApiConstants.submittedOnDateParamName, submittedOnDate);
        }

        // loanTemplate config
        JsonElement loanTemplateElement;
        String loanTemplate;
        if (savingsId == null) {
            loanTemplate = savingsAccountsApiResource.template(clientId, groupId, productId, staffInSelectedOfficeOnly, uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        } else {
            loanTemplate = savingsAccountsApiResource.retrieveOne(savingsId, staffInSelectedOfficeOnly, "all", uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        }

        BigDecimal nominalAnnualInterestRate;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateParamName, apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRate = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.nominalAnnualInterestRateParamName,
                    loanTemplateElement, localeFormat);
        }
        jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateParamName, nominalAnnualInterestRate);

        BigDecimal minOverdraftForInterestCalculation;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minOverdraftForInterestCalculationParamName,
                apiRequestBodyAsJsonElement)) {
            minOverdraftForInterestCalculation = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(
                    SavingsApiConstants.minOverdraftForInterestCalculationParamName, apiRequestBodyAsJsonElement);
        } else {
            minOverdraftForInterestCalculation = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minOverdraftForInterestCalculationParamName, loanTemplateElement);
        }
        if (minOverdraftForInterestCalculation != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minOverdraftForInterestCalculationParamName, minOverdraftForInterestCalculation);
        }
        BigDecimal nominalAnnualInterestRateOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName,
                apiRequestBodyAsJsonElement)) {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, apiRequestBodyAsJsonElement, localeFormat);
        } else {
            nominalAnnualInterestRateOverdraft = fromApiJsonHelper.extractBigDecimalNamed(
                    SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, loanTemplateElement, localeFormat);
        }
        if (nominalAnnualInterestRateOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.nominalAnnualInterestRateOverdraftParamName, nominalAnnualInterestRateOverdraft);
        }
        BigDecimal minRequiredOpeningBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement)) {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, apiRequestBodyAsJsonElement);
        } else {
            minRequiredOpeningBalance = fromApiJsonHelper
                    .extractBigDecimalWithLocaleNamed(SavingsApiConstants.minRequiredOpeningBalanceParamName, loanTemplateElement);
        }
        if (minRequiredOpeningBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minRequiredOpeningBalanceParamName, minRequiredOpeningBalance);
        }

        Integer interestCalculationDaysInYearType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationDaysInYearTypeParamName,
                apiRequestBodyAsJsonElement)) {
            interestCalculationDaysInYearType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    SavingsApiConstants.interestCalculationDaysInYearTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            interestCalculationDaysInYearType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, loanTemplateElement);
        }
        if (interestCalculationDaysInYearType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationDaysInYearTypeParamName, interestCalculationDaysInYearType);
        }
        Integer interestCalculationType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCalculationTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.interestCalculationTypeParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            interestCalculationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(SavingsApiConstants.interestCalculationTypeParamName,
                    loanTemplateElement);
        }
        if (interestCalculationType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCalculationTypeParamName, interestCalculationType);
        }

        Integer interestPostingPeriodType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestPostingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            interestPostingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestPostingPeriodTypeParamName, loanTemplateElement);
        }
        if (interestPostingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestPostingPeriodTypeParamName, interestPostingPeriodType);
        }
        Integer interestCompoundingPeriodType;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement)) {
            interestCompoundingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, apiRequestBodyAsJsonElement);
        } else {
            interestCompoundingPeriodType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(SavingsApiConstants.interestCompoundingPeriodTypeParamName, loanTemplateElement);
        }
        if (interestCompoundingPeriodType != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.interestCompoundingPeriodTypeParamName, interestCompoundingPeriodType);
        }

        lockInPeriodFrequencyConfig(fromApiJsonHelper, apiRequestBodyAsJsonElement, loanTemplateElement, jsonObjectLoan);

        Boolean withdrawalFeeForTransfers;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withdrawalFeeForTransfersParamName, apiRequestBodyAsJsonElement)) {
            withdrawalFeeForTransfers = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            withdrawalFeeForTransfers = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withdrawalFeeForTransfersParamName,
                    loanTemplateElement);
        }
        if (withdrawalFeeForTransfers != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.withdrawalFeeForTransfersParamName, withdrawalFeeForTransfers);
        }
        BigDecimal overdraftLimit;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.overdraftLimitParamName, apiRequestBodyAsJsonElement)) {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            overdraftLimit = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.overdraftLimitParamName, loanTemplateElement,
                    localeFormat);
        }
        if (overdraftLimit != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.overdraftLimitParamName, overdraftLimit);
        }
        Boolean withHoldTax;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement)) {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, apiRequestBodyAsJsonElement);
        } else {
            withHoldTax = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.withHoldTaxParamName, loanTemplateElement);
        }
        if (withHoldTax != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.withHoldTaxParamName, withHoldTax);
        }
        Boolean enforceMinRequiredBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.enforceMinRequiredBalanceParamName, apiRequestBodyAsJsonElement)) {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            enforceMinRequiredBalance = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.enforceMinRequiredBalanceParamName,
                    loanTemplateElement);
        }
        if (enforceMinRequiredBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.enforceMinRequiredBalanceParamName, enforceMinRequiredBalance);
        }

        BigDecimal minRequiredBalance;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minRequiredBalanceParamName, apiRequestBodyAsJsonElement)) {
            minRequiredBalance = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.minRequiredBalanceParamName,
                    apiRequestBodyAsJsonElement, localeFormat);
        } else {
            minRequiredBalance = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.minRequiredBalanceParamName,
                    loanTemplateElement, localeFormat);
        }
        if (minRequiredBalance != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.minRequiredBalanceParamName, minRequiredBalance);
        }

//        BigDecimal minBalanceForInterestCalculation;
//        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.minBalanceForInterestCalculationParamName, apiRequestBodyAsJsonElement)) {
//            minBalanceForInterestCalculation = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.minBalanceForInterestCalculationParamName,
//                    apiRequestBodyAsJsonElement, localeFormat);
//        } else {
//            minBalanceForInterestCalculation = fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.minBalanceForInterestCalculationParamName,
//                    loanTemplateElement, localeFormat);
//        }
//        if (minBalanceForInterestCalculation != null) {
//            jsonObjectLoan.addProperty(SavingsApiConstants.minBalanceForInterestCalculationParamName, minBalanceForInterestCalculation);
//        }
        Boolean allowOverdraft;
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.allowOverdraftParamName, apiRequestBodyAsJsonElement)) {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            allowOverdraft = fromApiJsonHelper.extractBooleanNamed(SavingsApiConstants.allowOverdraftParamName, loanTemplateElement);
        }
        if (allowOverdraft != null) {
            jsonObjectLoan.addProperty(SavingsApiConstants.allowOverdraftParamName, allowOverdraft);
        }

        JsonArray charges = new JsonArray();
        if (fromApiJsonHelper.parameterExists(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement)) {
            charges = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName, apiRequestBodyAsJsonElement);
        } else {
            final JsonArray chargesCheck = fromApiJsonHelper.extractJsonArrayNamed(SavingsApiConstants.chargesParamName,
                    loanTemplateElement);
            if (chargesCheck != null && chargesCheck.isJsonArray()) {
                for (JsonElement charge : chargesCheck) {
                    final JsonObject chargesValue = new JsonObject();
                    chargesValue.addProperty(SavingsApiConstants.chargeIdParamName,
                            fromApiJsonHelper.extractLongNamed(SavingsApiConstants.chargeIdParamName, charge));
                    chargesValue.addProperty(SavingsApiConstants.amountParamName,
                            fromApiJsonHelper.extractBigDecimalNamed(SavingsApiConstants.amountParamName, charge, Locale.ENGLISH));
                    charges.add(chargesValue);
                }
            }
        }
        jsonObjectLoan.add(SavingsApiConstants.chargesParamName, charges);

        return jsonObjectLoan.toString();
    }

}
