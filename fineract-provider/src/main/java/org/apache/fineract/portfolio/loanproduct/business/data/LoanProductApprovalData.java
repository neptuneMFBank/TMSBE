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
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.useradministration.data.RoleData;

@SuppressWarnings("unused")
//@Data
public class LoanProductApprovalData implements Serializable {

    private Long id;
    private String name;
    private LoanProductData loanProductData;
    private Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData;
    private Collection<LoanProductData> loanProductOptions;
    private Collection<RoleData> roleOptions;
    private SavingsProductData savingsProductData;

    public LoanProductApprovalData(Long id, String name, LoanProductData loanProductData, Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData, Collection<LoanProductData> loanProductOptions, Collection<RoleData> roleOptions) {
        this.id = id;
        this.name = name;
        this.loanProductData = loanProductData;
        this.loanProductApprovalConfigData = loanProductApprovalConfigData;
        this.loanProductOptions = loanProductOptions;
        this.roleOptions = roleOptions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LoanProductData getLoanProductData() {
        return loanProductData;
    }

    public void setLoanProductData(LoanProductData loanProductData) {
        this.loanProductData = loanProductData;
    }

    public Collection<LoanProductApprovalConfigData> getLoanProductApprovalConfigData() {
        return loanProductApprovalConfigData;
    }

    public void setLoanProductApprovalConfigData(Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData) {
        this.loanProductApprovalConfigData = loanProductApprovalConfigData;
    }

    public Collection<LoanProductData> getLoanProductOptions() {
        return loanProductOptions;
    }

    public void setLoanProductOptions(Collection<LoanProductData> loanProductOptions) {
        this.loanProductOptions = loanProductOptions;
    }

    public Collection<RoleData> getRoleOptions() {
        return roleOptions;
    }

    public void setRoleOptions(Collection<RoleData> roleOptions) {
        this.roleOptions = roleOptions;
    }

    public SavingsProductData getSavingsProductData() {
        return savingsProductData;
    }

    public void setSavingsProductData(SavingsProductData savingsProductData) {
        this.savingsProductData = savingsProductData;
    }

    public static LoanProductApprovalData template(Collection<LoanProductData> loanProductOptions, Collection<RoleData> roleOptions) {
        Long id = null;
        String name = null;
        LoanProductData loanProductData = null;
        Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData = null;
        return new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
    }

    public static LoanProductApprovalData lookUp(Long id, String name, LoanProductData loanProductData, SavingsProductData savingsProductData) {
        Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData = null;
        Collection<LoanProductData> loanProductOptions = null;
        Collection<RoleData> roleOptions = null;
        final LoanProductApprovalData loanProductApprovalData1 = new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
        loanProductApprovalData1.setSavingsProductData(savingsProductData);
        return loanProductApprovalData1;

    }

    public static LoanProductApprovalData lookUpFinal(Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData,
            LoanProductApprovalData loanProductApprovalData) {
        final Long id = loanProductApprovalData.getId();
        final String name = loanProductApprovalData.getName();
        final LoanProductData loanProductData = loanProductApprovalData.getLoanProductData();
        final SavingsProductData savingsProductData = loanProductApprovalData.getSavingsProductData();
        Collection<LoanProductData> loanProductOptions = null;
        Collection<RoleData> roleOptions = null;
        final LoanProductApprovalData loanProductApprovalData1 = new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
        loanProductApprovalData1.setSavingsProductData(savingsProductData);
        return loanProductApprovalData1;
    }

    public static LoanProductApprovalData instance(Long id, String name, LoanProductData loanProductData,
            Collection<LoanProductApprovalConfigData> loanProductApprovalConfigData, Collection<LoanProductData> loanProductOptions,
            Collection<RoleData> roleOptions, LocalDate createdOn, LocalDate modifiedOn, SavingsProductData savingsProductData) {
        final LoanProductApprovalData loanProductApprovalData = new LoanProductApprovalData(id, name, loanProductData, loanProductApprovalConfigData, loanProductOptions, roleOptions);
        loanProductApprovalData.setSavingsProductData(savingsProductData);
        return loanProductApprovalData;
    }

}
