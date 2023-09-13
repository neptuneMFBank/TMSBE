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
package org.apache.fineract.portfolio.loanaccount.api.business;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoansApiResource;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;

public interface LoanBusinessApiConstants {

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

    /*
     * { "clientId":1, "clientAccountNo":"000000001", "clientName":"Test Test", "clientOfficeId":1, "loanProductId":1,
     * "loanProductName":"Test Loan", "loanProductDescription":"Test Loan", "isLoanProductLinkedToFloatingRate":false,
     * "currency":{ "code":"NGN", "name":"Nigerian Naira", "decimalPlaces":2, "inMultiplesOf":0,
     * "nameCode":"currency.NGN", "displayLabel":"Nigerian Naira [NGN]" }, "principal":5000, "approvedPrincipal":5000,
     * "proposedPrincipal":5000, "netDisbursalAmount":5000, "termFrequency":2, "termPeriodFrequencyType":{ "id":2,
     * "code":"repaymentFrequency.periodFrequencyType.months", "value":"Months" }, "numberOfRepayments":2,
     * "repaymentEvery":1, "repaymentFrequencyType":{ "id":2, "code":"repaymentFrequency.periodFrequencyType.months",
     * "value":"Months" }, "interestRatePerPeriod":2, "interestRateFrequencyType":{ "id":2,
     * "code":"interestRateFrequency.periodFrequencyType.months", "value":"Per month" }, "annualInterestRate":24,
     * "isFloatingInterestRate":false, "amortizationType":{ "id":1, "code":"amortizationType.equal.installments",
     * "value":"Equal installments" }, "interestType":{ "id":0, "code":"interestType.declining.balance",
     * "value":"Declining Balance" }, "interestCalculationPeriodType":{ "id":1,
     * "code":"interestCalculationPeriodType.same.as.repayment.period", "value":"Same as repayment period" },
     * "allowPartialPeriodInterestCalcualtion":false, "transactionProcessingStrategyId":1, "timeline":{
     * "expectedDisbursementDate":[ 2023, 8, 13 ] }, "charges":[
     *
     * ], "productOptions":[ { "id":1, "name":"Test Loan", "includeInBorrowerCycle":false, "useBorrowerCycle":false,
     * "isLinkedToFloatingInterestRates":false, "isFloatingInterestRateCalculationAllowed":false,
     * "allowVariableInstallments":false, "isInterestRecalculationEnabled":false, "canDefineInstallmentAmount":false,
     * "principalVariationsForBorrowerCycle":[
     *
     * ], "interestRateVariationsForBorrowerCycle":[
     *
     * ], "numberOfRepaymentVariationsForBorrowerCycle":[
     *
     * ], "canUseForTopup":false, "isRatesEnabled":false, "multiDisburseLoan":false,
     * "disallowExpectedDisbursements":false, "allowApprovedDisbursedAmountsOverApplied":false,
     * "holdGuaranteeFunds":false, "accountMovesOutOfNPAOnlyOnArrearsCompletion":false,
     * "syncExpectedWithDisbursementDate":false, "isEqualAmortization":false } ], "loanOfficerOptions":[ { "id":3,
     * "firstname":"Sylvernus", "lastname":"Akubo", "displayName":"Akubo, Sylvernus", "officeId":1,
     * "officeName":"Head Office", "isLoanOfficer":true, "isActive":true, "joiningDate":[ 2023, 8, 9 ] }, { "id":4,
     * "firstname":"Joe", "lastname":"Edache", "displayName":"Edache, Joe", "officeId":1, "officeName":"Head Office",
     * "isLoanOfficer":true, "isActive":true, "joiningDate":[ 2023, 8, 9 ] }, { "id":1, "firstname":"Daniel",
     * "lastname":"Michael", "displayName":"Michael, Daniel", "mobileNo":"07062902851", "officeId":1,
     * "officeName":"Head Office", "isLoanOfficer":true, "isActive":true, "joiningDate":[ 2023, 8, 7 ] }, { "id":5,
     * "firstname":"Tayo", "lastname":"Oladosu", "displayName":"Oladosu, Tayo", "officeId":1,
     * "officeName":"Head Office", "isLoanOfficer":true, "isActive":true, "joiningDate":[ 2023, 8, 10 ] }, { "id":2,
     * "firstname":"Chibuikem", "lastname":"Simplify", "displayName":"Simplify, Chibuikem", "mobileNo":"09020867386",
     * "officeId":1, "officeName":"Head Office", "isLoanOfficer":true, "isActive":true, "joiningDate":[ 2023, 8, 7 ] }
     * ], "loanPurposeOptions":[ { "id":32, "name":"Rent", "position":0, "description":"Rent", "active":true,
     * "mandatory":false }, { "id":33, "name":"Education", "position":1, "description":"Education", "active":true,
     * "mandatory":false }, { "id":34, "name":"Mortgage", "position":2, "description":"Mortgage", "active":true,
     * "mandatory":false } ], "fundOptions":[ { "id":1, "name":"Fund 1" } ], "termFrequencyTypeOptions":[ { "id":0,
     * "code":"loanTermFrequency.periodFrequencyType.days", "value":"Days" }, { "id":1,
     * "code":"loanTermFrequency.periodFrequencyType.weeks", "value":"Weeks" }, { "id":2,
     * "code":"loanTermFrequency.periodFrequencyType.months", "value":"Months" }, { "id":3,
     * "code":"loanTermFrequency.periodFrequencyType.years", "value":"Years" } ], "repaymentFrequencyTypeOptions":[ {
     * "id":0, "code":"repaymentFrequency.periodFrequencyType.days", "value":"Days" }, { "id":1,
     * "code":"repaymentFrequency.periodFrequencyType.weeks", "value":"Weeks" }, { "id":2,
     * "code":"repaymentFrequency.periodFrequencyType.months", "value":"Months" } ],
     * "repaymentFrequencyNthDayTypeOptions":[ { "id":1, "code":"repaymentFrequency.nthDayType.one", "value":"first" },
     * { "id":2, "code":"repaymentFrequency.nthDayType.two", "value":"second" }, { "id":3,
     * "code":"repaymentFrequency.nthDayType.three", "value":"third" }, { "id":4,
     * "code":"repaymentFrequency.nthDayType.four", "value":"fourth" }, { "id":-1,
     * "code":"repaymentFrequency.nthDayType.last", "value":"last" } ], "repaymentFrequencyDaysOfWeekTypeOptions":[ {
     * "id":7, "code":"repaymentFrequency.weekDayType.sunday", "value":"SUNDAY" }, { "id":1,
     * "code":"repaymentFrequency.weekDayType.monday", "value":"MONDAY" }, { "id":2,
     * "code":"repaymentFrequency.weekDayType.tuesday", "value":"TUESDAY" }, { "id":3,
     * "code":"repaymentFrequency.weekDayType.wednesday", "value":"WEDNESDAY" }, { "id":4,
     * "code":"repaymentFrequency.weekDayType.thursday", "value":"THURSDAY" }, { "id":5,
     * "code":"repaymentFrequency.weekDayType.friday", "value":"FRIDAY" }, { "id":6,
     * "code":"repaymentFrequency.weekDayType.saturday", "value":"SATURDAY" } ], "interestRateFrequencyTypeOptions":[ {
     * "id":2, "code":"interestRateFrequency.periodFrequencyType.months", "value":"Per month" }, { "id":3,
     * "code":"interestRateFrequency.periodFrequencyType.years", "value":"Per year" }, { "id":4,
     * "code":"interestRateFrequency.periodFrequencyType.whole_term", "value":"Whole term" } ],
     * "amortizationTypeOptions":[ { "id":1, "code":"amortizationType.equal.installments", "value":"Equal installments"
     * }, { "id":0, "code":"amortizationType.equal.principal", "value":"Equal principal payments" } ],
     * "interestTypeOptions":[ { "id":1, "code":"interestType.flat", "value":"Flat" }, { "id":0,
     * "code":"interestType.declining.balance", "value":"Declining Balance" } ],
     * "interestCalculationPeriodTypeOptions":[ { "id":0, "code":"interestCalculationPeriodType.daily", "value":"Daily"
     * }, { "id":1, "code":"interestCalculationPeriodType.same.as.repayment.period", "value":"Same as repayment period"
     * } ], "transactionProcessingStrategyOptions":[ { "id":1, "code":"mifos-standard-strategy",
     * "name":"Penalties, Fees, Interest, Principal order" }, { "id":4, "code":"rbi-india-strategy",
     * "name":"Overdue/Due Fee/Int,Principal" }, { "id":5, "code":"principal-interest-penalties-fees-order-strategy",
     * "name":"Principal, Interest, Penalties, Fees Order" }, { "id":6,
     * "code":"interest-principal-penalties-fees-order-strategy", "name":"Interest, Principal, Penalties, Fees Order" },
     * { "id":7, "code":"early-repayment-strategy", "name":"Early Repayment Strategy" }, { "id":2,
     * "code":"heavensfamily-strategy", "name":"HeavensFamily Unique" }, { "id":3, "code":"creocore-strategy",
     * "name":"Creocore Unique" } ], "chargeOptions":[ { "id":1, "name":"Loan Admin Charge", "active":true,
     * "penalty":false, "freeWithdrawal":false, "freeWithdrawalChargeFrequency":0, "restartFrequency":0,
     * "restartFrequencyEnum":0, "isPaymentType":false, "currency":{ "code":"NGN", "name":"Nigerian Naira",
     * "decimalPlaces":2, "nameCode":"currency.NGN", "displayLabel":"Nigerian Naira [NGN]" }, "amount":1,
     * "chargeTimeType":{ "id":1, "code":"chargeTimeType.disbursement", "value":"Disbursement" }, "chargeAppliesTo":{
     * "id":1, "code":"chargeAppliesTo.loan", "value":"Loan" }, "chargeCalculationType":{ "id":2,
     * "code":"chargeCalculationType.percent.of.amount", "value":"% Amount" }, "chargePaymentMode":{ "id":0,
     * "code":"chargepaymentmode.regular", "value":"Regular" } } ], "accountLinkingOptions":[ { "id":1,
     * "accountNo":"000000001", "clientId":1, "clientName":"Test Test", "productId":1, "productName":"Eso-E Savings",
     * "fieldOfficerId":3, "fieldOfficerName":"Akubo, Sylvernus", "currency":{ "code":"NGN", "name":"Nigerian Naira",
     * "decimalPlaces":2, "inMultiplesOf":100, "nameCode":"currency.NGN", "displayLabel":"Nigerian Naira [NGN]" } } ],
     * "multiDisburseLoan":false, "canDefineInstallmentAmount":false, "canDisburse":false, "clientActiveLoanOptions":[ {
     * "id":1, "accountNo":"000000001", "externalId":"111", "productId":1, "productName":"Test Loan",
     * "shortProductName":"TSL", "status":{ "id":300, "code":"loanStatusType.active", "value":"Active",
     * "pendingApproval":false, "waitingForDisbursal":false, "active":true, "closedObligationsMet":false,
     * "closedWrittenOff":false, "closedRescheduled":false, "closed":false, "overpaid":false }, "loanType":{ "id":1,
     * "code":"accountType.individual", "value":"Individual" }, "loanCycle":1, "timeline":{ "submittedOnDate":[ 2023, 8,
     * 13 ], "submittedByUsername":"mifos", "submittedByFirstname":"App", "submittedByLastname":"Administrator",
     * "approvedOnDate":[ 2023, 8, 13 ], "approvedByUsername":"mifos", "approvedByFirstname":"App",
     * "approvedByLastname":"Administrator", "expectedDisbursementDate":[ 2023, 8, 13 ], "actualDisbursementDate":[
     * 2023, 8, 13 ], "disbursedByUsername":"mifos", "disbursedByFirstname":"App",
     * "disbursedByLastname":"Administrator", "expectedMaturityDate":[ 2023, 10, 1 ] }, "inArrears":false,
     * "originalLoan":5000, "loanBalance":5100, "amountPaid":50 } ], "canUseForTopup":true, "isTopup":false, "product":{
     * "id":1, "name":"Test Loan", "shortName":"TSL", "description":"Test Loan", "includeInBorrowerCycle":true,
     * "useBorrowerCycle":false, "startDate":[ 1999, 1, 1 ], "closeDate":[ 2026, 12, 31 ],
     * "status":"loanProduct.active", "currency":{ "code":"NGN", "name":"Nigerian Naira", "decimalPlaces":2,
     * "inMultiplesOf":0, "nameCode":"currency.NGN", "displayLabel":"Nigerian Naira [NGN]" }, "principal":5000,
     * "minPrincipal":1000, "maxPrincipal":10000000, "numberOfRepayments":2, "minNumberOfRepayments":1,
     * "maxNumberOfRepayments":36, "repaymentEvery":1, "repaymentFrequencyType":{ "id":2,
     * "code":"repaymentFrequency.periodFrequencyType.months", "value":"Months" }, "interestRatePerPeriod":2,
     * "minInterestRatePerPeriod":1, "maxInterestRatePerPeriod":3.85, "interestRateFrequencyType":{ "id":2,
     * "code":"interestRateFrequency.periodFrequencyType.months", "value":"Per month" }, "annualInterestRate":24,
     * "isLinkedToFloatingInterestRates":false, "isFloatingInterestRateCalculationAllowed":false,
     * "allowVariableInstallments":false, "minimumGap":0, "maximumGap":0, "amortizationType":{ "id":1,
     * "code":"amortizationType.equal.installments", "value":"Equal installments" }, "interestType":{ "id":0,
     * "code":"interestType.declining.balance", "value":"Declining Balance" }, "interestCalculationPeriodType":{ "id":1,
     * "code":"interestCalculationPeriodType.same.as.repayment.period", "value":"Same as repayment period" },
     * "allowPartialPeriodInterestCalcualtion":false, "transactionProcessingStrategyId":1,
     * "transactionProcessingStrategyName":"Penalties, Fees, Interest, Principal order", "daysInMonthType":{ "id":1,
     * "code":"DaysInMonthType.actual", "value":"Actual" }, "daysInYearType":{ "id":1, "code":"DaysInYearType.actual",
     * "value":"Actual" }, "isInterestRecalculationEnabled":false, "minimumDaysBetweenDisbursalAndFirstRepayment":25,
     * "canDefineInstallmentAmount":false, "installmentAmountInMultiplesOf":100, "charges":[
     *
     * ], "principalVariationsForBorrowerCycle":[
     *
     * ], "interestRateVariationsForBorrowerCycle":[
     *
     * ], "numberOfRepaymentVariationsForBorrowerCycle":[
     *
     * ], "accountingRule":{ "id":1, "code":"accountingRuleType.none", "value":"NONE" }, "canUseForTopup":true,
     * "isRatesEnabled":false, "rates":[
     *
     * ], "multiDisburseLoan":false, "maxTrancheCount":0, "disallowExpectedDisbursements":false,
     * "allowApprovedDisbursedAmountsOverApplied":false, "overAppliedNumber":0,
     * "principalThresholdForLastInstallment":0, "holdGuaranteeFunds":false,
     * "accountMovesOutOfNPAOnlyOnArrearsCompletion":false, "allowAttributeOverrides":{ "amortizationType":true,
     * "interestType":true, "transactionProcessingStrategyId":true, "interestCalculationPeriodType":true,
     * "inArrearsTolerance":true, "repaymentEvery":true, "graceOnPrincipalAndInterestPayment":true,
     * "graceOnArrearsAgeing":true, "isNew":true }, "syncExpectedWithDisbursementDate":false,
     * "isEqualAmortization":false }, "overdueCharges":[
     *
     * ], "daysInMonthType":{ "id":1, "code":"DaysInMonthType.actual", "value":"Actual" }, "daysInYearType":{ "id":1,
     * "code":"DaysInYearType.actual", "value":"Actual" }, "isInterestRecalculationEnabled":false,
     * "interestRecalculationData":{
     *
     * }, "isVariableInstallmentsAllowed":false, "minimumGap":0, "maximumGap":0, "isEqualAmortization":false,
     * "isRatesEnabled":false, "delinquent":{ "availableDisbursementAmount":0, "pastDueDays":0, "delinquentDays":0,
     * "delinquentAmount":0, "lastPaymentAmount":0 } }
     */
    public static String loanTemplateConfig(final LoansApiResource loansApiResource, final String apiRequestBodyAsJson,
            final FromJsonHelper fromApiJsonHelper, final Long clientDefaultId, final boolean staffInSelectedOfficeOnly,
            @Context final UriInfo uriInfo, final Long loanId) {

        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());

        // loan to create json
        final JsonElement apiRequestBodyAsJsonElement = fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonObject jsonObjectLoan = apiRequestBodyAsJsonElement.getAsJsonObject();

        final Long productId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.productIdParameterName, apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(LoanApiConstants.productIdParameterName, productId);

        String loanType = "individual";
        Long clientId = null;
        Long groupId = null;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.clientIdParameterName, apiRequestBodyAsJsonElement)) {
            clientId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.clientIdParameterName, apiRequestBodyAsJsonElement);
            loanType = "individual";
            jsonObjectLoan.addProperty(LoanApiConstants.clientIdParameterName, clientId);
        } else if (fromApiJsonHelper.parameterExists(LoanApiConstants.groupIdParameterName, apiRequestBodyAsJsonElement)) {
            groupId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.groupIdParameterName, apiRequestBodyAsJsonElement);
            loanType = "group";
            jsonObjectLoan.addProperty(LoanApiConstants.groupIdParameterName, groupId);
        }
        if (clientId == null && groupId == null && clientDefaultId != null) {
            // only needed when calculate loan repayment schedule
            clientId = clientDefaultId;
            jsonObjectLoan.addProperty(LoanApiConstants.clientIdParameterName, clientId);
        }

        // loanTemplate config
        JsonElement loanTemplateElement;
        String loanTemplate;
        if (loanId == null) {
            loanTemplate = loansApiResource.template(clientId, groupId, productId, loanType, staffInSelectedOfficeOnly, true, uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        } else {
            loanTemplate = loansApiResource.retrieveLoan(loanId, staffInSelectedOfficeOnly, null, null, null, uriInfo);
            loanTemplateElement = fromApiJsonHelper.parse(loanTemplate);
        }

        String dateFormat = "yyyy-MM-dd";
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.dateFormatParameterName, apiRequestBodyAsJsonElement)) {
            dateFormat = fromApiJsonHelper.extractStringNamed(LoanApiConstants.dateFormatParameterName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.dateFormatParameterName, dateFormat);

        String locale = "en";
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.localeParameterName, apiRequestBodyAsJsonElement)) {
            locale = fromApiJsonHelper.extractStringNamed(LoanApiConstants.localeParameterName, apiRequestBodyAsJsonElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.localeParameterName, locale);

        final Locale localeFormat = new Locale(locale);
        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(dateFormat).withLocale(localeFormat);

        // JsonElement clientActiveLoanOptions = null;
        // if (fromApiJsonHelper.parameterExists(LoanApiConstants.clientActiveLoanOptions, loanTemplateElement)) {
        // clientActiveLoanOptions = fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.clientActiveLoanOptions,
        // loanTemplateElement);
        // }
        String expectedDisbursementDate;
        if (fromApiJsonHelper.parameterExists(expectedDisbursementDateParameterName, apiRequestBodyAsJsonElement)) {
            expectedDisbursementDate = fromApiJsonHelper.extractStringNamed(expectedDisbursementDateParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            expectedDisbursementDate = today.format(fmt);
        }
        jsonObjectLoan.addProperty(expectedDisbursementDateParameterName, expectedDisbursementDate);

        String submittedOnDate;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.submittedOnDateParameterName, apiRequestBodyAsJsonElement)) {
            submittedOnDate = fromApiJsonHelper.extractStringNamed(LoanApiConstants.submittedOnDateParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            submittedOnDate = today.format(fmt);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.submittedOnDateParameterName, submittedOnDate);

        if (fromApiJsonHelper.parameterExists(LoanApiConstants.loanTypeParameterName, apiRequestBodyAsJsonElement)) {
            loanType = fromApiJsonHelper.extractStringNamed(LoanApiConstants.loanTypeParameterName, apiRequestBodyAsJsonElement);
        }
        if (StringUtils.isNotBlank(loanType)) {
            jsonObjectLoan.addProperty(LoanApiConstants.loanTypeParameterName, loanType);
        }
        String repaymentsStartingFromDate = null;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentsStartingFromDateParameterName, apiRequestBodyAsJsonElement)) {
            repaymentsStartingFromDate = fromApiJsonHelper.extractStringNamed(LoanApiConstants.repaymentsStartingFromDateParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            if (fromApiJsonHelper.parameterExists(minimumDaysBetweenDisbursalAndFirstRepaymentParameterName, loanTemplateElement)) {
                Integer minimumDaysBetweenDisbursalAndFirstRepayment = fromApiJsonHelper
                        .extractIntegerSansLocaleNamed(minimumDaysBetweenDisbursalAndFirstRepaymentParameterName, loanTemplateElement);
                repaymentsStartingFromDate = today.plusDays(minimumDaysBetweenDisbursalAndFirstRepayment).format(fmt);
            }
        }
        if (StringUtils.isNotBlank(repaymentsStartingFromDate)) {
            jsonObjectLoan.addProperty(LoanApiConstants.repaymentsStartingFromDateParameterName, repaymentsStartingFromDate);
        }

        final Integer fundId = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.fundIdParameterName,
                apiRequestBodyAsJsonElement);
        jsonObjectLoan.addProperty(LoanApiConstants.fundIdParameterName, fundId);
        BigDecimal principal;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.principalParamName, apiRequestBodyAsJsonElement)) {
            principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.principalParamName,
                    apiRequestBodyAsJsonElement);
        } else {
            principal = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.principalParamName, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.principalParamName, principal);

        Integer loanTermFrequency;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.loanTermFrequencyParameterName, apiRequestBodyAsJsonElement)) {
            loanTermFrequency = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.loanTermFrequencyParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            loanTermFrequency = fromApiJsonHelper.extractIntegerSansLocaleNamed(termFrequencyParameterName, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.loanTermFrequencyParameterName, loanTermFrequency);

        Integer loanTermFrequencyType;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.loanTermFrequencyTypeParameterName, apiRequestBodyAsJsonElement)) {
            loanTermFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.loanTermFrequencyTypeParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement termPeriodFrequencyType = fromApiJsonHelper.extractJsonObjectNamed(termPeriodFrequencyTypeParameterName,
                    loanTemplateElement);
            loanTermFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.idParameterName,
                    termPeriodFrequencyType);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.loanTermFrequencyTypeParameterName, loanTermFrequencyType);

        Integer numberOfRepayments;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.numberOfRepaymentsParameterName, apiRequestBodyAsJsonElement)) {
            numberOfRepayments = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.numberOfRepaymentsParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            numberOfRepayments = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.numberOfRepaymentsParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.numberOfRepaymentsParameterName, numberOfRepayments);

        Integer repaymentEvery;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentEveryParameterName, apiRequestBodyAsJsonElement)) {
            repaymentEvery = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.repaymentEveryParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            repaymentEvery = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.repaymentEveryParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.repaymentEveryParameterName, repaymentEvery);

        Integer repaymentFrequencyType;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentFrequencyTypeParameterName, apiRequestBodyAsJsonElement)) {
            repaymentFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.repaymentFrequencyTypeParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement repaymentFrequencyElement = fromApiJsonHelper
                    .extractJsonObjectNamed(LoanApiConstants.repaymentFrequencyTypeParameterName, loanTemplateElement);
            repaymentFrequencyType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.idParameterName,
                    repaymentFrequencyElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.repaymentFrequencyTypeParameterName, repaymentFrequencyType);

        BigDecimal interestRatePerPeriod;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.interestRatePerPeriodParameterName, apiRequestBodyAsJsonElement)) {
            interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRatePerPeriodParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            interestRatePerPeriod = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.interestRatePerPeriodParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.interestRatePerPeriodParameterName, interestRatePerPeriod);

        Integer amortizationType;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.amortizationTypeParameterName, apiRequestBodyAsJsonElement)) {
            amortizationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.amortizationTypeParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement amortization = fromApiJsonHelper.extractJsonObjectNamed(LoanApiConstants.amortizationTypeParameterName,
                    loanTemplateElement);
            amortizationType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.idParameterName, amortization);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.amortizationTypeParameterName, amortizationType);

        Boolean isEqualAmortization;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.isEqualAmortizationParam, apiRequestBodyAsJsonElement)) {
            isEqualAmortization = fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam,
                    apiRequestBodyAsJsonElement);
        } else {
            isEqualAmortization = fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isEqualAmortizationParam, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.isEqualAmortizationParam, isEqualAmortization);

        Integer interestType;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.interestTypeParameterName, apiRequestBodyAsJsonElement)) {
            interestType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.interestTypeParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestTypeDefault = fromApiJsonHelper.extractJsonObjectNamed(LoanApiConstants.interestTypeParameterName,
                    loanTemplateElement);
            interestType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.idParameterName, interestTypeDefault);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.interestTypeParameterName, interestType);

        String interestChargedFromDate = null;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.interestChargedFromDateParameterName, apiRequestBodyAsJsonElement)) {
            interestChargedFromDate = fromApiJsonHelper.extractStringNamed(LoanApiConstants.interestChargedFromDateParameterName,
                    apiRequestBodyAsJsonElement);
        }

        Integer interestCalculationPeriodType;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.interestCalculationPeriodTypeParameterName, apiRequestBodyAsJsonElement)) {
            interestCalculationPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    LoanApiConstants.interestCalculationPeriodTypeParameterName, apiRequestBodyAsJsonElement);
        } else {
            final JsonElement interestCalculationPeriod = fromApiJsonHelper
                    .extractJsonObjectNamed(LoanApiConstants.interestCalculationPeriodTypeParameterName, loanTemplateElement);
            interestCalculationPeriodType = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.idParameterName,
                    interestCalculationPeriod);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.interestCalculationPeriodTypeParameterName, interestCalculationPeriodType);
        if (StringUtils.isBlank(interestChargedFromDate) && interestCalculationPeriodType != null && interestCalculationPeriodType == 0) {
            // for daily add interestChargedFromDate to today's date
            interestChargedFromDate = submittedOnDate;
        }
        jsonObjectLoan.addProperty(LoanApiConstants.interestChargedFromDateParameterName, interestChargedFromDate);

        Boolean allowPartialPeriodInterestCalcualtion;
        if (fromApiJsonHelper.parameterExists(allowPartialPeriodInterestCalcualtionParameterName, apiRequestBodyAsJsonElement)) {
            allowPartialPeriodInterestCalcualtion = fromApiJsonHelper
                    .extractBooleanNamed(allowPartialPeriodInterestCalcualtionParameterName, apiRequestBodyAsJsonElement);
        } else {
            allowPartialPeriodInterestCalcualtion = fromApiJsonHelper
                    .extractBooleanNamed(allowPartialPeriodInterestCalcualtionParameterName, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(allowPartialPeriodInterestCalcualtionParameterName, allowPartialPeriodInterestCalcualtion);

        BigDecimal inArrearsTolerance;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.inArrearsToleranceParameterName, apiRequestBodyAsJsonElement)) {
            inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.inArrearsToleranceParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            inArrearsTolerance = fromApiJsonHelper.extractBigDecimalWithLocaleNamed(LoanApiConstants.inArrearsToleranceParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.inArrearsToleranceParameterName, inArrearsTolerance);

        Integer transactionProcessingStrategyId;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.transactionProcessingStrategyIdParameterName, apiRequestBodyAsJsonElement)) {
            transactionProcessingStrategyId = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    LoanApiConstants.transactionProcessingStrategyIdParameterName, apiRequestBodyAsJsonElement);
        } else {
            transactionProcessingStrategyId = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.transactionProcessingStrategyIdParameterName, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.transactionProcessingStrategyIdParameterName, transactionProcessingStrategyId);

        Integer graceOnArrearsAgeing;
        if (fromApiJsonHelper.parameterExists(graceOnArrearsAgeingParameterName, apiRequestBodyAsJsonElement)) {
            graceOnArrearsAgeing = fromApiJsonHelper.extractIntegerSansLocaleNamed(graceOnArrearsAgeingParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            graceOnArrearsAgeing = fromApiJsonHelper.extractIntegerSansLocaleNamed(graceOnArrearsAgeingParameterName, loanTemplateElement);
        }
        jsonObjectLoan.addProperty(graceOnArrearsAgeingParameterName, graceOnArrearsAgeing);

        Integer graceOnInterestCharged;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestChargedParameterName, apiRequestBodyAsJsonElement)) {
            graceOnInterestCharged = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnInterestChargedParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            graceOnInterestCharged = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnInterestChargedParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.graceOnInterestChargedParameterName, graceOnInterestCharged);

        Integer graceOnPrincipalPayment;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnPrincipalPaymentParameterName, apiRequestBodyAsJsonElement)) {
            graceOnPrincipalPayment = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnPrincipalPaymentParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            graceOnPrincipalPayment = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnPrincipalPaymentParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.graceOnPrincipalPaymentParameterName, graceOnPrincipalPayment);

        Integer graceOnInterestPayment;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.graceOnInterestPaymentParameterName, apiRequestBodyAsJsonElement)) {
            graceOnInterestPayment = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnInterestPaymentParameterName,
                    apiRequestBodyAsJsonElement);
        } else {
            graceOnInterestPayment = fromApiJsonHelper.extractIntegerSansLocaleNamed(LoanApiConstants.graceOnInterestPaymentParameterName,
                    loanTemplateElement);
        }
        jsonObjectLoan.addProperty(LoanApiConstants.graceOnInterestPaymentParameterName, graceOnInterestPayment);

        final Boolean isRatesEnabled = fromApiJsonHelper.extractBooleanNamed(isRatesEnabledParameterName, loanTemplateElement);
        if (BooleanUtils.isTrue(isRatesEnabled)) {
            JsonArray rates;
            if (fromApiJsonHelper.parameterExists(LoanProductConstants.RATES_PARAM_NAME, apiRequestBodyAsJsonElement)) {
                rates = fromApiJsonHelper.extractJsonArrayNamed(LoanProductConstants.RATES_PARAM_NAME, apiRequestBodyAsJsonElement);
            } else {
                rates = fromApiJsonHelper.extractJsonArrayNamed(LoanProductConstants.RATES_PARAM_NAME, loanTemplateElement);
            }
            jsonObjectLoan.add(LoanProductConstants.RATES_PARAM_NAME, rates);
        }
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.loanOfficerIdParameterName, apiRequestBodyAsJsonElement)) {
            final Long loanOfficerId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanOfficerIdParameterName,
                    apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(LoanApiConstants.loanOfficerIdParameterName, loanOfficerId);
        }
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.loanPurposeIdParameterName, apiRequestBodyAsJsonElement)) {
            final Long loanPurposeId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanPurposeIdParameterName,
                    apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(LoanApiConstants.loanPurposeIdParameterName, loanPurposeId);
        }
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.externalIdParameterName, apiRequestBodyAsJsonElement)) {
            final String externalId = fromApiJsonHelper.extractStringNamed(LoanApiConstants.externalIdParameterName,
                    apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(LoanApiConstants.externalIdParameterName, externalId);
        }

        Boolean createStandingInstructionAtDisbursement;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.createStandingInstructionAtDisbursementParameterName,
                apiRequestBodyAsJsonElement)) {
            createStandingInstructionAtDisbursement = fromApiJsonHelper.extractBooleanNamed(
                    LoanApiConstants.createStandingInstructionAtDisbursementParameterName, apiRequestBodyAsJsonElement);
            if (BooleanUtils.isTrue(createStandingInstructionAtDisbursement)) {
                jsonObjectLoan.addProperty(LoanApiConstants.createStandingInstructionAtDisbursementParameterName,
                        createStandingInstructionAtDisbursement);
                final Long linkAccountId = fromApiJsonHelper.extractLongNamed(LoanApiConstants.linkAccountIdParameterName,
                        apiRequestBodyAsJsonElement);
                jsonObjectLoan.addProperty(LoanApiConstants.linkAccountIdParameterName, linkAccountId);
            }
        }
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, apiRequestBodyAsJsonElement)) {
            final Integer repaymentFrequencyDayOfWeekType = fromApiJsonHelper.extractIntegerSansLocaleNamed(
                    LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(LoanApiConstants.repaymentFrequencyDayOfWeekTypeParameterName, repaymentFrequencyDayOfWeekType);
        }
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.repaymentFrequencyNthDayTypeParameterName, apiRequestBodyAsJsonElement)) {
            final Integer repaymentFrequencyNthDayType = fromApiJsonHelper
                    .extractIntegerSansLocaleNamed(LoanApiConstants.repaymentFrequencyNthDayTypeParameterName, apiRequestBodyAsJsonElement);
            jsonObjectLoan.addProperty(LoanApiConstants.repaymentFrequencyNthDayTypeParameterName, repaymentFrequencyNthDayType);
        }

        JsonArray charges = new JsonArray();
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.chargesParameterName, apiRequestBodyAsJsonElement)) {
            charges = fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.chargesParameterName, apiRequestBodyAsJsonElement);
        } else {
            final JsonArray chargesCheck = fromApiJsonHelper.extractJsonArrayNamed(LoanApiConstants.chargesParameterName,
                    loanTemplateElement);
            if (chargesCheck != null && chargesCheck.isJsonArray()) {
                for (JsonElement charge : chargesCheck) {
                    final JsonObject chargesValue = new JsonObject();
                    chargesValue.addProperty(ClientApiConstants.chargeIdParamName,
                            fromApiJsonHelper.extractLongNamed(ClientApiConstants.chargeIdParamName, charge));
                    chargesValue.addProperty(ClientApiConstants.amountParamName,
                            fromApiJsonHelper.extractBigDecimalNamed(ClientApiConstants.amountParamName, charge, Locale.ENGLISH));
                    charges.add(chargesValue);
                }
            }
        }
        jsonObjectLoan.add(LoanApiConstants.chargesParameterName, charges);

        Boolean isTopup;
        if (fromApiJsonHelper.parameterExists(LoanApiConstants.isTopup, apiRequestBodyAsJsonElement)) {
            isTopup = fromApiJsonHelper.extractBooleanNamed(LoanApiConstants.isTopup, apiRequestBodyAsJsonElement);
            if (BooleanUtils.isTrue(isTopup)) {
                Long loanIdToClose = fromApiJsonHelper.extractLongNamed(LoanApiConstants.loanIdToClose, apiRequestBodyAsJsonElement);
                jsonObjectLoan.addProperty(LoanApiConstants.isTopup, isTopup);
                jsonObjectLoan.addProperty(LoanApiConstants.loanIdToClose, loanIdToClose);
            }
        }
        return jsonObjectLoan.toString();
    }

}
