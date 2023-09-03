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
package org.apache.fineract.infrastructure.documentmanagement.data.business;

import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.data.business.CodeBusinessData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object representing a user document being managed on the
 * platform.
 */
public class DocumentConfigData {

    private final boolean active;
    private final Long id;
    private Integer legalFormId;
    private final String name;
    private final String description;
    private Collection<CodeBusinessData> settings;
    private Collection<CodeData> settingsCode;
    //private Collection<LoanProductData> loanProductDatas;
    private List<EnumOptionData> clientLegalFormOptions;
    //private Collection<SavingsProductData> savingProductOptions;
    private EnumOptionData globalEntityType;
    private List<EnumOptionData> globalEntityTypes;

    public static DocumentConfigData template(final Collection<CodeData> settingsCode, final List<EnumOptionData> clientLegalFormOptions,
            final List<EnumOptionData> globalEntityTypes) {

        final DocumentConfigData documentConfigData = new DocumentConfigData(null, null, null, false);
        documentConfigData.setClientLegalFormOptions(clientLegalFormOptions);
        documentConfigData.setGlobalEntityTypes(globalEntityTypes);
        //documentConfigData.setLoanProductDatas(loanProductDatas);
        //documentConfigData.setSavingProductOptions(savingProductOptions);
        documentConfigData.setSettingsCode(settingsCode);
        return documentConfigData;
    }

    public static DocumentConfigData lookup(final Long id, final String name) {
        return new DocumentConfigData(id, name, null, false);
    }

    public DocumentConfigData(final Long id, final String name, final String description, final boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Collection<CodeBusinessData> getSettings() {
        return settings;
    }

//    public Collection<LoanProductData> getLoanProductDatas() {
//        return loanProductDatas;
//    }
//
//    public void setLoanProductDatas(Collection<LoanProductData> loanProductDatas) {
//        this.loanProductDatas = loanProductDatas;
//    }
    public boolean isActive() {
        return active;
    }

    public void setSettings(Collection<CodeBusinessData> settings) {
        this.settings = settings;
    }

    public Integer getLegalFormId() {
        return legalFormId;
    }

    public void setLegalFormId(Integer legalFormId) {
        this.legalFormId = legalFormId;
    }

    public List<EnumOptionData> getClientLegalFormOptions() {
        return clientLegalFormOptions;
    }

    public void setClientLegalFormOptions(List<EnumOptionData> clientLegalFormOptions) {
        this.clientLegalFormOptions = clientLegalFormOptions;
    }

//    public Collection<SavingsProductData> getSavingProductOptions() {
//        return savingProductOptions;
//    }
//
//    public void setSavingProductOptions(Collection<SavingsProductData> savingProductOptions) {
//        this.savingProductOptions = savingProductOptions;
//    }
    public void setSettingsCode(Collection<CodeData> settingsCode) {
        this.settingsCode = settingsCode;
    }

    public Collection<CodeData> getSettingsCode() {
        return settingsCode;
    }

    public EnumOptionData getGlobalEntityType() {
        return globalEntityType;
    }

    public void setGlobalEntityType(EnumOptionData globalEntityType) {
        this.globalEntityType = globalEntityType;
    }

    public List<EnumOptionData> getGlobalEntityTypes() {
        return globalEntityTypes;
    }

    public void setGlobalEntityTypes(List<EnumOptionData> globalEntityTypes) {
        this.globalEntityTypes = globalEntityTypes;
    }

}
