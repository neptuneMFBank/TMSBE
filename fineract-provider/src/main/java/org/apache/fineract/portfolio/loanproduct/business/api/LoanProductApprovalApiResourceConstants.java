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
package org.apache.fineract.portfolio.loanproduct.business.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class LoanProductApprovalApiResourceConstants {

    private LoanProductApprovalApiResourceConstants() {

    }

    public static final String RESOURCENAME = "LOANPRODUCT_APPROVAL";
    public static final String SAVINGSPRODUCTOPTIONS = "savingsProductOptions";
    public static final String LOANPRODUCTOPTIONS = "loanProductOptions";
    public static final String ROLEOPTIONS = "roleOptions";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String LOANPRODUCTDATA = "loanProductData";
    public static final String SAVINGSPRODUCTDATA = "savingsProductData";
    public static final String SAVINGSPRODUCTID = "savingsProductId";
    public static final String LOANPRODUCTID = "loanProductId";
    public static final String LOANPRODUCTAPPROVALCONFIGDATA = "loanProductApprovalConfigData";

    public static final String ROLEID = "roleId";
    public static final String RANK = "rank";
    public static final String MINAPPROVALAMOUNT = "minApprovalAmount";
    public static final String MAXAPPROVALAMOUNT = "maxApprovalAmount";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(ID, NAME, LOANPRODUCTDATA, LOANPRODUCTAPPROVALCONFIGDATA, SAVINGSPRODUCTDATA));

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, LOANPRODUCTID, LOANPRODUCTAPPROVALCONFIGDATA, SAVINGSPRODUCTID));

    public static final Set<String> REQUEST_UPDATE_DATA_PARAMETERS = REQUEST_DATA_PARAMETERS;

    public static final Set<String> RESPONSE_TEMPLATE_PARAMETERS = new HashSet<>(
            Arrays.asList(LOANPRODUCTOPTIONS, ROLEOPTIONS, SAVINGSPRODUCTOPTIONS));

}
