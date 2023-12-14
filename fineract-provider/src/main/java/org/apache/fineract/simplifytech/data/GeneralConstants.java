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
import com.google.gson.JsonObject;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.FuzzyScore;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterest;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestConfig;
import org.apache.fineract.portfolio.loanproduct.business.domain.LoanProductInterestRepositoryWrapper;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Olakunle.Thompson
 */
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
     * TODO: Need a better implementation with guaranteed uniqueness (but not a
     * long UUID)...maybe something tied to system clock..
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
        String[][] inputStrings = new String[][]{
            // Matches abc at start of term
            {"Asiata Omodeleola Babalola", "Asiata Omodeleola Babalola"}, // {"Thompson Olakunle Rasak", "Rasak
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

    public static Long withdrawAmount(final BigDecimal amount, final Long savingsId,
            final String note, final String accountNumber, final Long paymentTypeId,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
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

    public static Long holdAmount(final BigDecimal amountToHold, final Long loanId, final Long savingsId,
            final String note,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        //lien/hold the upfront fee sum
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

    public static BigDecimal loanProductInterestGeneration(final LoanProductInterestRepositoryWrapper loanProductInterestRepositoryWrapper, final Long productId, Integer loanTermFrequency, BigDecimal interestRatePerPeriod) {
        //connect to Loan Product Interest to pick business interest rate if configured
        final LoanProductInterest loanProductInterest = loanProductInterestRepositoryWrapper.findByLoanProductIdAndActive(productId, true);
        if (loanProductInterest != null) {
            final Set<LoanProductInterestConfig> loanProductInterestConfig = loanProductInterest.getLoanProductInterestConfig();
            if (!CollectionUtils.isEmpty(loanProductInterestConfig)) {
                final BigDecimal interestRatePerPeriodCheck = loanProductInterestConfig.stream()
                        .filter(predicate -> GeneralConstants.isWithinRange(new BigDecimal(loanTermFrequency), predicate.getMinTenor(), predicate.getMaxTenor()))
                        .map(LoanProductInterestConfig::getNominalInterestRatePerPeriod)
                        .findFirst().orElse(null);
                if (interestRatePerPeriodCheck != null) {
                    interestRatePerPeriod = interestRatePerPeriodCheck;
                }
            }
        }
        return interestRatePerPeriod;
    }
}
