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
import org.apache.fineract.useradministration.data.RoleData;

@SuppressWarnings("unused")
@Data
public class LoanProductApprovalData implements Serializable {

    private final Long id;
    private final String name;
    private final LoanProductData loanProductData;
    private final Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData;
    private final Collection<LoanProductData> loanProductOptions;
    private final Collection<RoleData> roleOptions;

    public static LoanProductApprovalData template(Collection<LoanProductData> loanProductOptions,
            Collection<RoleData> roleOptions) {
        Long id = null;
        String name = null;
        LoanProductData loanProductData = null;
        Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData = null;
        return new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
    }

    public static LoanProductApprovalData lookUp(Long id, String name, LoanProductData loanProductData) {
        Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData = null;
        Collection<LoanProductData> loanProductOptions = null;
        Collection<RoleData> roleOptions = null;
        return new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
    }

    public static LoanProductApprovalData lookUpFinal(Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData, LoanProductApprovalData loanProductApprovalData) {
        final Long id = loanProductApprovalData.getId();
        final String name = loanProductApprovalData.getName();
        final LoanProductData loanProductData = loanProductApprovalData.getLoanProductData();
        Collection<LoanProductData> loanProductOptions = null;
        Collection<RoleData> roleOptions = null;
        return new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
    }

    public static LoanProductApprovalData instance(Long id, String name, LoanProductData loanProductData, Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData, Collection<LoanProductData> loanProductOptions, Collection<RoleData> roleOptions, LocalDate createdOn, LocalDate modifiedOn) {
        return new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
    }

}
