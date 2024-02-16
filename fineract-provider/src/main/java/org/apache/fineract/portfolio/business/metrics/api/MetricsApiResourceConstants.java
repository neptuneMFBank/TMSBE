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
package org.apache.fineract.portfolio.business.metrics.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;

public final class MetricsApiResourceConstants {

    private MetricsApiResourceConstants() {

    }

    public static final String RESOURCENAME = "METRICS";
    public static final String ID = "id";
    public static final String RANK = "rank";
    public static final String UNDO_TO_METRICS_ID = "undoToMetricsId";
    public static final String LOAN_ID = "loanId";
    public static final String OVERDRAFT_ID = "overdraftId";
    public static final String SAVINGS_ID = "savingsId";
    public static final String STATUS = "status";
    public static final String STAFF_DATA = "staffData";
    public static final String SUPERVISOR_STAFF_DATA = "supervisorStaffData";
    public static final String CREATED_ON = "createdOn";
    public static final String MODIFIED_ON = "modifiedOn";
    public static final String CLIENT_DATA = "clientData";
    public static final String LOAN_OFFICER_DATA = "loanOfficerData";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, LOAN_ID, SAVINGS_ID, STATUS, STAFF_DATA,
            SUPERVISOR_STAFF_DATA, CREATED_ON, MODIFIED_ON, CLIENT_DATA, LOAN_OFFICER_DATA, OVERDRAFT_ID, RANK));

    public static final Set<String> LOAN_ACTION_DATA_PARAMETERS = new HashSet<>(Arrays.asList(LoanApiConstants.noteParamName, LOAN_ID, LoanApiConstants.localeParameterName,
            ClientApiConstants.staffIdParamName, SavingsApiConstants.paymentTypeIdParamName, LoanBusinessApiConstants.expectedDisbursementDateParameterName, LoanApiConstants.dateFormatParameterName, UNDO_TO_METRICS_ID));

    public static final Set<String> OVERDRAFT_ACTION_DATA_PARAMETERS = new HashSet<>(Arrays.asList(LoanApiConstants.noteParamName, OVERDRAFT_ID, LoanApiConstants.localeParameterName,
            ClientApiConstants.staffIdParamName, SavingsApiConstants.paymentTypeIdParamName, LoanBusinessApiConstants.expectedDisbursementDateParameterName, LoanApiConstants.dateFormatParameterName, UNDO_TO_METRICS_ID));

}
