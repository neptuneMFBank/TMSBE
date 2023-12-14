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
package org.apache.fineract.portfolio.loanproduct.business.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import lombok.Data;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;

@SuppressWarnings("unused")
@Data
public class LoanProductInterestData implements Serializable {

    private final Long id;
    private final String name;
    private final LoanProductData loanProductData;
    private final Collection<LoanProductInterestConfigData> loanProductInterestConfigData;
    private final Collection<LoanProductData> loanProductOptions;
    private final String description;
    private final Boolean active;

    public static LoanProductInterestData template(Collection<LoanProductData> loanProductOptions) {
        Long id = null;
        String name = null;
        String description = null;
        Boolean active = null;
        LoanProductData loanProductData = null;
        Collection<LoanProductInterestConfigData> loanProductInterestConfigData = null;
        return new LoanProductInterestData(id, name, loanProductData, loanProductInterestConfigData, loanProductOptions, description, active);
    }

    public static LoanProductInterestData lookUp(Long id, String name, LoanProductData loanProductData, String description, final Boolean active) {
        Collection<LoanProductInterestConfigData> loanProductInterestConfigData = null;
        Collection<LoanProductData> loanProductOptions = null;
        return new LoanProductInterestData(id, name, loanProductData, loanProductInterestConfigData, loanProductOptions, description, active);
    }

    public static LoanProductInterestData lookUpFinal(Collection<LoanProductInterestConfigData> loanProductInterestConfigData,
            LoanProductInterestData loanProductApprovalData) {
        final Long id = loanProductApprovalData.getId();
        final String name = loanProductApprovalData.getName();
        final String description = loanProductApprovalData.getDescription();
        final Boolean active = loanProductApprovalData.getActive();
        final LoanProductData loanProductData = loanProductApprovalData.getLoanProductData();
        Collection<LoanProductData> loanProductOptions = null;
        return new LoanProductInterestData(id, name, loanProductData, loanProductInterestConfigData, loanProductOptions, description, active);
    }

    public static LoanProductInterestData instance(Long id, String name, LoanProductData loanProductData,
            Collection<LoanProductInterestConfigData> loanProductInterestConfigData, Collection<LoanProductData> loanProductOptions,
            LocalDate createdOn, LocalDate modifiedOn, String description, Boolean active) {
        return new LoanProductInterestData(id, name, loanProductData, loanProductInterestConfigData, loanProductOptions, description, active);
    }

}
