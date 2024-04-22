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
package org.apache.fineract.portfolio.products.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class ProductVisibilityApiResourceConstants {

    private ProductVisibilityApiResourceConstants() {

    }

    public static final String LOAN_VISIBILITY_RESOURCENAME = "LOANPRODUCT_VISIBILITY";
    public static final String SAVINGS_VISIBILITY_RESOURCENAME = "SAVINGSPRODUCT_VISIBILITY";

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";

    public static final String LOANPRODUCT = "loanProduct";
    public static final String SAVINGSPRODUCT = "savingsProduct";
    public static final String CLIENTCLASSIFICATION = "clientClassification";
    public static final String CLIENTTYPE = "clientType";
    public static final String LEGALENUM = "legalEnum";
    public static final String LOANPRODUCTOPTIONS = "loanProductOptions";
    public static final String CLIENTTYPEOPTIONS = "clientTypeOptions";
    public static final String CLIENTCLASSIFICATIONOPTIONS = "clientClassificationOptions";
    public static final String CLIENTLEGALOPTIONS = "clientLegalFormOptions";

    public static final Set<String> REQUEST_DATA_LOAN_VISIBILITY_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, DESCRIPTION, LOANPRODUCT, CLIENTCLASSIFICATION, LEGALENUM, CLIENTTYPE));

    public static final Set<String> REQUEST_DATA_SAVINGS_VISIBILITY_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, DESCRIPTION, SAVINGSPRODUCT, CLIENTCLASSIFICATION, LEGALENUM, CLIENTTYPE));

    public static final Set<String> RESPONSE_TEMPLATE_PARAMETERS = new HashSet<>(
            Arrays.asList(LOANPRODUCTOPTIONS, CLIENTTYPEOPTIONS, CLIENTCLASSIFICATIONOPTIONS, CLIENTLEGALOPTIONS));

    public static final Set<String> RESPONSE_DATA_LOAN_VISIBILITY_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, DESCRIPTION, LOANPRODUCT, CLIENTCLASSIFICATION, LEGALENUM, CLIENTTYPE));

    public static final Set<String> RESPONSE_DATA_SAVINGS_VISIBILITY_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, DESCRIPTION, SAVINGSPRODUCT, CLIENTCLASSIFICATION, LEGALENUM, CLIENTTYPE));

}
