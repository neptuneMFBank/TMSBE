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
package org.apache.fineract.simplifytech.data;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.account.AccountDetailConstants;
import org.apache.fineract.portfolio.account.api.AccountTransfersApiConstants;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterest;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridData;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridJsonData;
import org.apache.fineract.portfolio.paymenttype.service.business.PaymentTypeGridReadPlatformService;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Olakunle.Thompson
 */
@Slf4j
public class GeneralConstants {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralConstants.class);
    public static String LOCALE_EN_DEFAULT = "en";
    public static String DATEFORMET_DEFAULT = "yyyy-MM-dd";
    public static String DATEFORMAT_MONTHDAY_DEFAULT = "dd MMM";

    public static String removeSpecialCharacters(final String value) {
        String newValue = value.trim().toLowerCase();
        return newValue.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static boolean fuzzyMatchingProcess(String firstValue, String secondValue, Integer comparisonValue) {
        String fistValueReal;
        String secondValueReal;
        if (StringUtils.length(firstValue) > StringUtils.length(secondValue)) {
            fistValueReal = firstValue;
            secondValueReal = secondValue;
        } else {
            fistValueReal = secondValue;
            secondValueReal = firstValue;
        }
        Integer similariryScore = new FuzzyScore(Locale.getDefault()).fuzzyScore(fistValueReal, secondValueReal);
        // we are using a threshold of 5 and above to approve similiarity
        return (similariryScore >= comparisonValue);
    }

    public static List<Integer> getNumbersUsingIntStreamRangeClosed(Integer start, Integer end) {
        if (start == null || end == null) {
            return null;
        }
        return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }

    public static Integer numberOfDays(LocalDate startDate, LocalDate endDate) {
        Period period = Period.between(startDate, endDate);
        return period.getDays();
    }

    public static Integer calculateYearsFromDate(LocalDate date) {
        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        Period period = Period.between(date, today);

        // Now access the values as below
        // System.out.println(period.getDays());
        // System.out.println(period.getMonths());
        return period.getYears();
    }

    public static BigDecimal calculateMonthFromDate(LocalDate date) {
        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        Period period = Period.between(date, today);

        // Now access the values as below
        // System.out.println(period.getDays());
        // System.out.println(period.getMonths());
        final BigDecimal months = new BigDecimal(period.getMonths());
        return months.divide(new BigDecimal(BigInteger.TEN));
    }

    /**
     * TODO: Need a better implementation with guaranteed uniqueness (but not a long UUID)...maybe something tied to
     * system clock..
     *
     * @param context
     * @return
     */
    public static String generateUniqueId() {
        final Long time = System.currentTimeMillis();
        final String uniqueVal = String.valueOf(time);
        final String transactionId = Long.toHexString(Long.parseLong(uniqueVal));
        return transactionId;
    }

    public static void main(String[] args) {
        String[][] inputStrings = new String[][] {
                // Matches abc at start of term
                { "Asiata Omodeleola Babalola", "Asiata Omodeleola Babalola" }, // {"Thompson Olakunle Rasak", "Rasak
                // Olakunle Thompson"},
                // // ABC in different case than term
                // {"cecilianwebonyi", "testname2"},
                // // Matches abc at end of term
                // {"qwreweqwqw", "testname3"},
                // // Matches abc in middle
                // {"dedede", "testname4"},
                // // Matches abc but not continuous.
                // {"abxycz", "abc"}, {"axbycz", "abc"},
                // // Reverse order of abc
                // {"cbaxyz", "abc"},
                // // Matches abc but different order.
                // {"cabxyz", "abc"}
        };
        for (String[] input : inputStrings) {
            String term = input[0];
            String query = input[1];
            // Fuzzy score of query against term
            double fuzzyScore = new FuzzyScore(Locale.getDefault()).fuzzyScore(term, query);
            System.out.println("FuzzyScore of query '" + query + "' against term '" + term + "' is " + fuzzyScore + "points");
        }
    }

    // Babs Development
    public static String phoneNumberValidator(String number) {
        LOG.info("Mobile number: " + number);
        PhoneNumberUtil numberUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phoneNumber;
        try {
            phoneNumber = numberUtil.parse(number, "NG");
            boolean isValid = numberUtil.isValidNumber(phoneNumber);
            String NationalNumber = String.valueOf(phoneNumber.getNationalNumber());
            LOG.info("Google Generated Mobile number: " + NationalNumber);
            boolean isonlyDigit = onlyDigit(NationalNumber);

            if (!isonlyDigit) {
                LOG.error("Phone Number contains characters");
                return null;
            } else if (!(NationalNumber.length() == 10)) {
                LOG.error("Phone Number with invalid length");
                return null;
            } else if (isValid) {
                return "0" + phoneNumber.getNationalNumber();
            } else {
                LOG.error("Phone Number not valid");
                return null;
            }
        } catch (NumberParseException e) {
            LOG.error("Phone Number Exception: {}", e.getMessage());
            return null;
        }

    }

    static boolean onlyDigit(String s) {
        boolean onlyDigit = false;

        if (s != null && !s.isEmpty()) {
            for (char c : s.toCharArray()) {
                if (!(onlyDigit = Character.isDigit(c))) {
                    break;
                }
            }
        }

        return onlyDigit;
    }

    // Generic function to convert List of
    // String to List of Integer
    public static <T, U> List<U> convertStringListToLongList(List<T> listOfString, Function<T, U> function) {
        return Lists.transform(listOfString, function);
    }

    public static boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    public static boolean isWithinRange(final BigDecimal value, final BigDecimal min, final BigDecimal max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    public static Long withdrawAmount(final BigDecimal amount, final Long savingsId, final String note, final String accountNumber,
            final Long paymentTypeId, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        final JsonObject withdrawAmountJson = new JsonObject();
        withdrawAmountJson.addProperty(SavingsApiConstants.transactionDateParamName, today.toString());
        withdrawAmountJson.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
        withdrawAmountJson.addProperty(SavingsApiConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);
        withdrawAmountJson.addProperty(SavingsApiConstants.transactionAmountParamName, amount);
        withdrawAmountJson.addProperty(SavingsApiConstants.noteParamName, note);
        withdrawAmountJson.addProperty(SavingsApiConstants.accountNumberParamName, accountNumber);
        withdrawAmountJson.addProperty(SavingsApiConstants.paymentTypeIdParamName, paymentTypeId);
        final String apiRequestBodyAsJson = withdrawAmountJson.toString();
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest = builder.savingsAccountWithdrawal(savingsId).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result.resourceId();
    }

    public static Long holdAmount(final BigDecimal amountToHold, final Long loanId, final Long savingsId, final String note,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        // lien/hold the upfront fee sum
        LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        final JsonObject holdAmountJson = new JsonObject();
        holdAmountJson.addProperty(SavingsApiConstants.transactionDateParamName, today.toString());
        holdAmountJson.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
        holdAmountJson.addProperty(SavingsApiConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);
        holdAmountJson.addProperty(SavingsApiConstants.transactionAmountParamName, amountToHold);
        holdAmountJson.addProperty(SavingsApiConstants.reasonForBlockParamName, note);
        final String apiRequestBodyAsJson = holdAmountJson.toString();
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
        final CommandWrapper commandRequest = builder.holdAmount(savingsId).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result.resourceId();
    }

    public static Long releaseAmount(final Long savingsId, final Long transactionId,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withNoJsonBody();
        final CommandWrapper commandRequest = builder.releaseAmount(savingsId, transactionId).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result.resourceId();
    }

    public static Long intrabankTransfer(final Long transferApprovalId, final BigDecimal amount, final Long fromOfficeId,
            final Long fromClientId, final Long fromAccountId, final Integer fromAccountType, final Long toOfficeId, final Long toClientId,
            final Long toAccountId, final Integer toAccountType, final String note,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        final LocalDate today = LocalDate.now(DateUtils.getDateTimeZoneOfTenant());
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(AccountDetailConstants.fromOfficeIdParamName, fromOfficeId);
        jsonObject.addProperty(AccountDetailConstants.fromClientIdParamName, fromClientId);
        jsonObject.addProperty(AccountDetailConstants.fromAccountTypeParamName, fromAccountType);
        jsonObject.addProperty(AccountDetailConstants.fromAccountIdParamName, fromAccountId);
        jsonObject.addProperty(AccountDetailConstants.toOfficeIdParamName, toOfficeId);
        jsonObject.addProperty(AccountDetailConstants.toClientIdParamName, toClientId);
        jsonObject.addProperty(AccountDetailConstants.toAccountIdParamName, toAccountId);
        jsonObject.addProperty(AccountDetailConstants.toAccountTypeParamName, toAccountType);
        jsonObject.addProperty(AccountDetailConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
        jsonObject.addProperty(AccountDetailConstants.dateFormatParamName, GeneralConstants.DATEFORMET_DEFAULT);
        jsonObject.addProperty(AccountTransfersApiConstants.transferDescriptionParamName, note + "-" + transferApprovalId);
        jsonObject.addProperty(AccountTransfersApiConstants.transferAmountParamName, amount);
        jsonObject.addProperty(AccountTransfersApiConstants.transferDateParamName, today.toString());
        LOG.info("intrabankTransfer createAccountTransfer details: {}", jsonObject.toString());

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(jsonObject.toString());
        final CommandWrapper commandRequest = builder.createAccountTransfer().build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return result.resourceId();
    }

    public static BigDecimal loanProductInterestGeneration(final LoanProductInterestRepositoryWrapper loanProductInterestRepositoryWrapper,
            final Long productId, Integer loanTermFrequency, BigDecimal interestRatePerPeriod) {
        // connect to Loan Product Interest to pick business interest rate if configured
        final LoanProductInterest loanProductInterest = loanProductInterestRepositoryWrapper.findByLoanProductIdAndActive(productId, true);
        if (loanProductInterest != null) {
            final Set<LoanProductInterestConfig> loanProductInterestConfig = loanProductInterest.getLoanProductInterestConfig();
            if (!CollectionUtils.isEmpty(loanProductInterestConfig)) {
                final BigDecimal interestRatePerPeriodCheck = loanProductInterestConfig.stream()
                        .filter(predicate -> GeneralConstants.isWithinRange(new BigDecimal(loanTermFrequency), predicate.getMinTenor(),
                                predicate.getMaxTenor()))
                        .map(LoanProductInterestConfig::getNominalInterestRatePerPeriod).findFirst().orElse(null);
                if (interestRatePerPeriodCheck != null) {
                    interestRatePerPeriod = interestRatePerPeriodCheck;
                }
            }
        }
        return interestRatePerPeriod;
    }

    public static boolean feeIntervalOnInterestCharge(final Charge charge,
            // final Integer periodInstallment,
            final String codeName, final LocalDate dueDate) {
        final Integer feeInterval = charge.getFeeInterval();
        if (feeInterval != null && feeInterval > 0) {
            // check feeFrequency and skip
            // final int isFeeIntervalModulo = periodInstallment % feeInterval;
            final int getMonth = dueDate.getMonthValue();
            final int isFeeIntervalModulo = getMonth % feeInterval;
            if (// periodInstallment != 1 &&
            isFeeIntervalModulo != 0) {
                // log.warn("cumulativeFeeChargesDueWithin feeInterval checks {}: periodInstallment:{} % feeInterval:{}
                // = {}", codeName, periodInstallment, feeInterval, isFeeIntervalModulo);
                log.warn("cumulativeFeeChargesDueWithin feeInterval checks {}: getMonth:{} % feeInterval:{} = {}", codeName, getMonth,
                        feeInterval, isFeeIntervalModulo);
                return true;
            }
        }
        return false;
    }

    public static String removeFirstCharacters(String originalString, int numberOfCharactersToRemove) {
        // Check if the original string is not null and its length is greater than the number of characters to remove
        if (originalString != null && originalString.length() > numberOfCharactersToRemove) {
            return StringUtils.substring(originalString, numberOfCharactersToRemove);
        } else {
            // Handle the case where the original string is null or its length is less than or equal to the number of
            // characters to remove
            return originalString;
        }
    }

    public static BigDecimal paymentExtensionGridCharge(PaymentTypeGridReadPlatformService paymentTypeGridReadPlatformService,
            // final PaymentDetail paymentDetail,
            final BigDecimal transactionAmount, Long paymentTypeId, Long chargeId) {
        BigDecimal amount = BigDecimal.ZERO;
        FromJsonHelper fromJsonHelper = new FromJsonHelper();
        try {
            // if (paymentDetail != null && paymentDetail.getPaymentType() != null) {
            // final PaymentType paymentType = paymentDetail.getPaymentType();
            // paymentTypeId = paymentType.getId();
            // }
            // Extending to paymentTypeGrid
            // final Collection<PaymentTypeGridData> paymentTypeGridData =
            // paymentTypeGridReadPlatformService.retrievePaymentTypeGrids(paymentTypeId);
            Collection<PaymentTypeGridData> paymentTypeGridData;// =
                                                                // paymentTypeGridReadPlatformService.retrievePaymentTypeGridsViaCharge(chargeId);
            if (paymentTypeId != null && chargeId != null) {
                paymentTypeGridData = paymentTypeGridReadPlatformService.retrievePaymentTypeGridsViaCharge(chargeId, paymentTypeId);
            } else {
                paymentTypeGridData = paymentTypeGridReadPlatformService.retrievePaymentTypeGridsViaCharge(chargeId);
            }
            if (!CollectionUtils.isEmpty(paymentTypeGridData)) {
                for (PaymentTypeGridData paymentTypeGridData1 : paymentTypeGridData) {
                    // final PaymentTypeGridData paymentTypeGridData1 =
                    // paymentTypeGridData.stream().findFirst().orElse(null);
                    // if (paymentTypeGridData1 != null) {
                    if (BooleanUtils.isFalse(paymentTypeGridData1.getIsCommission())
                            && BooleanUtils.isTrue(paymentTypeGridData1.getIsGrid()) && paymentTypeGridData1.getGridJsonObject() != null) {
                        final JsonElement je = paymentTypeGridData1.getGridJsonObject();
                        final Type listType = new TypeToken<List<PaymentTypeGridJsonData>>() {}.getType();
                        final String json = fromJsonHelper.toJson(je);
                        log.info("paymentExtensionGridCharge raw info: {}", json);
                        final List<PaymentTypeGridJsonData> paymentTypeGridJsonData = fromJsonHelper.fromJson(json, listType);
                        if (!CollectionUtils.isEmpty(paymentTypeGridJsonData)) {
                            log.info("paymentExtensionGridCharge json info: {}", Arrays.toString(paymentTypeGridJsonData.toArray()));
                            final BigDecimal secondAmount = paymentTypeGridJsonData.stream()
                                    .filter(predicate -> isWithinRange(transactionAmount, predicate.getMinAmount(),
                                            predicate.getMaxAmount()))
                                    .map(PaymentTypeGridJsonData::getAmount).findFirst().orElse(BigDecimal.ZERO);
                            amount = amount.add(secondAmount);
                            // if (amount != null) {
                            // return amount;
                            // }
                        }
                    }
                    // }
                }
            }
        } catch (Exception e) {
            log.error("paymentTypeGridData Error: {}", e);
        }
        return amount;
    }

    public static String getAuthUserCurrentRoleId(final AppUser appUser, final FromJsonHelper fromApiJsonHelper) {
        String roleIds = null;
        final JsonArray roleIdArray = new JsonArray();
        if (!CollectionUtils.isEmpty(appUser.getRoles())) {
            for (Role role : appUser.getRoles()) {
                final JsonObject roleIdObject = new JsonObject();
                roleIdObject.addProperty(String.valueOf(role.getId()), role.getName());
                roleIdArray.add(roleIdObject);
            }
            roleIds = fromApiJsonHelper.toJson(roleIdArray);
        }
        return roleIds;
    }

    public static BigDecimal setCustomDefaultInterateRateForInvestmentViewPurpose(final Set<InterestRateChart> charts,
            BigDecimal interestRate) {
        // set a default rate if InterestRateChart is not Empty
        if (!CollectionUtils.isEmpty(charts)) {
            final InterestRateChart interestRateChart = charts.stream()
                    .filter(predicate -> !CollectionUtils.isEmpty(predicate.setOfChartSlabs())).findFirst().orElse(null);
            if (interestRateChart != null) {
                final InterestRateChartSlab interestRateChartSlab = interestRateChart.setOfChartSlabs().stream()
                        .filter(predicate -> predicate.slabFields() != null && predicate.slabFields().annualInterestRate() != null)
                        .findFirst().orElse(null);
                if (interestRateChartSlab != null) {
                    interestRate = interestRateChartSlab.slabFields().annualInterestRate();
                }
            }
        }
        return interestRate;
    }
}
