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
package org.apache.fineract.portfolio.business.employer.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class EmployerApiResourceConstants {

    private EmployerApiResourceConstants() {

    }

    public static final String resourceName = "EMPLOYER";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SLUG = "slug";
    public static final String clientType = "clientType";
    public static final String clientTypeIdParamName = "clientTypeId";
    public static final String RCNUMBER = "rcNumber";
    public static final String STATE = "state";
    public static final String STATEID = "stateId";
    public static final String COUNTRY = "country";
    public static final String COUNTRYID = "countryId";
    public static final String PARENTID = "parentId";
    public static final String externalId = "externalId";
    public static final String SOURCENAME = "sourceName";
    public static final String BUSINESSID = "businessId";
    public static final String WALLETID = "walletId";
    public static final String SECTOR = "sector";
    public static final String SECTORID = "sectorId";
    public static final String INDUSTRY = "industry";
    public static final String INDUSTRYID = "industryId";
    public static final String LGA = "lga";
    public static final String LGAID = "lgaId";
    public static final String OFFICEADDRESS = "officeAddress";
    public static final String NEARESTLANDMARK = "nearestLandMark";
    public static final String ACTIVE = "active";

    public static final String MOBILE_NO = "mobileNo";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String EMAIL_EXTENSION = "emailExtension";
    public static final String CONTACT_PERSON = "contactPerson";

    // Employer Loan Product
    public static final String EMPLOYER_ID = "employerId";
    public static final String EMPLOYER_LOAN_PRODUCT_ID = "loanProductId";
    public static final String EMPLOYER_LOAN_PRODUCT_NAME = "loanProductName";
    public static final String EMPLOYER_LOAN_PRODUCT_INTEREST = "interestRate";
    public static final String EMPLOYER_LOAN_PRODUCT_PRINCIPAL = "principal";
    public static final String EMPLOYER_LOAN_PRODUCT_TERM_FREQUENCY = "termFrequency";
    public static final String EMPLOYER_LOAN_PRODUCT_OPTIONS = "loanProductOptions";
    public static final String EMPLOYER_LOAN_PRODUCT_DSR = "dsr";
    public static final String localeParamName = "locale";
    public static final String teamLeadDisburseParamName = "teamLeadDisburse";
    public static final String teamLeadMaxDisburseParamName = "teamLeadMaxDisburse";
    public static final String SHOW_SIGNATURE = "showSignature";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, NAME, SLUG, RCNUMBER, STATE, COUNTRY, SECTOR,
            INDUSTRY, LGA, OFFICEADDRESS, NEARESTLANDMARK, ACTIVE, PARENTID, MOBILE_NO, EMAIL_ADDRESS, EMAIL_EXTENSION, CONTACT_PERSON,
            clientType, SOURCENAME, externalId, WALLETID, "paypoint", SHOW_SIGNATURE));

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(NAME, SLUG, RCNUMBER, COUNTRYID, STATEID,
            SECTORID, INDUSTRYID, LGAID, OFFICEADDRESS, NEARESTLANDMARK, ACTIVE, PARENTID, MOBILE_NO, EMAIL_ADDRESS, EMAIL_EXTENSION,
            CONTACT_PERSON, clientTypeIdParamName, BUSINESSID, SOURCENAME, externalId, SHOW_SIGNATURE));

    public static final Set<String> REQUEST_EMPLOYER_LOAN_PRODUCTS_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(teamLeadMaxDisburseParamName, teamLeadDisburseParamName, "noCharge", "acceptDisbursementServiceFeeFromExternal",
                    "downPaymentPaidFull", EMPLOYER_LOAN_PRODUCT_ID, EMPLOYER_LOAN_PRODUCT_INTEREST, localeParamName,
                    EMPLOYER_LOAN_PRODUCT_PRINCIPAL, EMPLOYER_LOAN_PRODUCT_TERM_FREQUENCY, EMPLOYER_LOAN_PRODUCT_DSR,
                    "minNominalInterestRatePerPeriod", "maxNominalInterestRatePerPeriod", "minNumberOfRepayments", "maxNumberOfRepayments",
                    "minPrincipal", "maxPrincipal", "downPaymentLimit", "charges", "maxAge", "minServiceYear", "maxServiceYear"));

    public static final Set<String> RESPONSE_EMPLOYER_LOAN_PRODUCTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("timestampCreatedDate",
            teamLeadMaxDisburseParamName, teamLeadDisburseParamName, "noCharge", "acceptDisbursementServiceFeeFromExternal",
            "downPaymentPaidFull", ID, EMPLOYER_ID, EMPLOYER_LOAN_PRODUCT_ID, EMPLOYER_LOAN_PRODUCT_NAME, EMPLOYER_LOAN_PRODUCT_INTEREST,
            EMPLOYER_LOAN_PRODUCT_OPTIONS, EMPLOYER_LOAN_PRODUCT_PRINCIPAL, EMPLOYER_LOAN_PRODUCT_TERM_FREQUENCY, EMPLOYER_LOAN_PRODUCT_DSR,
            "minPrincipal", "maxPrincipal", "minNominalInterestRatePerPeriod", "maxNominalInterestRatePerPeriod", "minNumberOfRepayments",
            "maxNumberOfRepayments", "downPaymentLimit", "charges", "totalOutstanding", "transactions", "status", "nextRepaymentDate",
            "loanPurposeName", "loanAmount", "employerLoanProductTopUps", "maxAge", "minServiceYear", "maxServiceYear"));

}
