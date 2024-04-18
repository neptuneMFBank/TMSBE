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
package org.apache.fineract.portfolio.business.overdraft.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;

public final class OverdraftApiResourceConstants {

    private OverdraftApiResourceConstants() {

    }

    public static final String RESOURCENAME = "OVERDRAFT";
    public static final String ID = "id";
    public static final String AMOUNT = "amount";
    public static final String NOMINALINTEREST = "nominalAnnualInterestRateOverdraft";
    public static final String STARTDATE = "startDate";
    public static final String STATUS = "status";
    public static final String EXPIRYDATE = "expiryDate";
    public static final String CREATEDBYUSER = "createdByUser";
    public static final String CREATED_ON = "createdOn";
    public static final String MODIFIED_ON = "modifiedOn";
    public static final String MODIFIEDBYUSER = "modifiedByUser";
    public static final String SAVINGS_ID = "savingsId";
    public static final String NUMBER_OF_DAYS = "numberOfDays";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, AMOUNT, SAVINGS_ID, STATUS, NOMINALINTEREST,
            STARTDATE, CREATED_ON, MODIFIED_ON, EXPIRYDATE, CREATEDBYUSER, MODIFIEDBYUSER, NUMBER_OF_DAYS, LoanBusinessApiConstants.metricsDataParam));

    public static final Set<String> REQUEST_ACTION_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(SAVINGS_ID, AMOUNT, LoanApiConstants.localeParameterName, NOMINALINTEREST, STARTDATE, NUMBER_OF_DAYS,
                    SavingsApiConstants.noteParamName, SavingsApiConstants.dateFormatParamName));

}
