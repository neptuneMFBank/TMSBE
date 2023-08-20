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
package org.apache.fineract.portfolio.client.data.business;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.data.business.CodeValueBusinessData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
import org.apache.fineract.portfolio.client.data.ClientTimelineData;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;

/**
 * Immutable data object representing client data.
 */
@SuppressWarnings("unused")
public final class ClientBusinessData implements Comparable<ClientBusinessData>, Serializable {

    private final Long id;
    private final String accountNo;
    private final String externalId;

    private final EnumOptionData status;
    private final CodeValueData subStatus;

    private final Boolean active;
    private final LocalDate activationDate;

    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private final String displayName;
    private final String mobileNo;
    private final String emailAddress;
    private final LocalDate dateOfBirth;
    private final CodeValueData gender;
    private final CodeValueData clientType;
    private final CodeValueData clientClassification;
    private final Boolean isStaff;

    private final Long officeId;
    private final String officeName;
    private final Long transferToOfficeId;
    private final String transferToOfficeName;

    private final Long imageId;
    private final Boolean imagePresent;
    private final Long staffId;
    private final String staffName;
    private final ClientTimelineData timeline;

    private final Long savingsProductId;
    private final String savingsProductName;

    private final Long savingsAccountId;
    private final EnumOptionData legalForm;
    private final Set<ClientCollateralManagementData> clientCollateralManagements;

    // associations
    private final Collection<GroupGeneralData> groups;

    // template
    // private final Collection<CodeValueBusinessData> countryValues;
    // private final Collection<CodeValueBusinessData> stateValues;
    // private final Collection<CodeValueBusinessData> lgaValues; salaryRange, employmentType
    private final Collection<CodeValueBusinessData> salaryRangeOptions;
    private final Collection<CodeValueBusinessData> employmentTypeOptions;
    private final Collection<CodeValueBusinessData> bankAccountTypeOptions;
    private final Collection<CodeValueBusinessData> bankOptions;
    private final Collection<CodeValueBusinessData> activationChannelOptions;
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    private final Collection<CodeValueData> narrations;
    private final Collection<SavingsProductData> savingProductOptions;
    private final Collection<SavingsAccountData> savingAccountOptions;
    private final Collection<CodeValueData> genderOptions;
    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> clientClassificationOptions;
    private final Collection<CodeValueData> clientNonPersonConstitutionOptions;
    private final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions;
    private final List<EnumOptionData> clientLegalFormOptions;
    private final ClientFamilyMembersData familyMemberOptions;

    private final ClientNonPersonData clientNonPersonDetails;

    private final Collection<AddressData> address;

    private final Boolean isAddressEnabled;

    private final List<DatatableData> datatables;

    // import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private Long clientTypeId;
    private Long genderId;
    private Long clientClassificationId;
    private Long legalFormId;
    private LocalDate submittedOnDate;

    public Integer getRowIndex() {
        return rowIndex;
    }

    public Long getSavingsAccountId() {
        return savingsAccountId;
    }

    public Long getId() {
        return id;
    }

    public String getOfficeName() {
        return officeName;
    }

    public static ClientBusinessData template(final Long officeId, final LocalDate joinedDate, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> narrations,
            final Collection<CodeValueData> genderOptions, final Collection<SavingsProductData> savingProductOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions,
            final Collection<CodeValueData> clientNonPersonConstitutionOptions,
            final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions, final List<EnumOptionData> clientLegalFormOptions,
            final ClientFamilyMembersData familyMemberOptions, final Collection<AddressData> address, final Boolean isAddressEnabled,
            final List<DatatableData> datatables, final Collection<CodeValueBusinessData> activationChannelOptions,
            final Collection<CodeValueBusinessData> bankAccountTypeOptions, final Collection<CodeValueBusinessData> bankOptions,
            final Collection<CodeValueBusinessData> salaryRangeOptions, final Collection<CodeValueBusinessData> employmentTypeOptions
    // ,final Collection<CodeValueBusinessData> countryValues,
    // final Collection<CodeValueBusinessData> stateValues,
    // final Collection<CodeValueBusinessData> lgaValues
    ) {
        final String accountNo = null;
        final EnumOptionData status = null;
        final CodeValueData subStatus = null;
        final String officeName = null;
        final Long transferToOfficeId = null;
        final String transferToOfficeName = null;
        final Long id = null;
        final String firstname = null;
        final String middlename = null;
        final String lastname = null;
        final String fullname = null;
        final String displayName = null;
        final String externalId = null;
        final String mobileNo = null;
        final String emailAddress = null;
        final LocalDate dateOfBirth = null;
        final CodeValueData gender = null;
        final Long imageId = null;
        final Long staffId = null;
        final String staffName = null;
        final Collection<GroupGeneralData> groups = null;
        final ClientTimelineData timeline = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Long savingsAccountId = null;
        final Collection<SavingsAccountData> savingAccountOptions = null;
        final CodeValueData clientType = null;
        final CodeValueData clientClassification = null;
        final EnumOptionData legalForm = null;
        final Boolean isStaff = false;
        final ClientNonPersonData clientNonPersonDetails = null;
        final Set<ClientCollateralManagementData> clientCollateralManagements = null;
        return new ClientBusinessData(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName, id,
                firstname, middlename, lastname, fullname, displayName, externalId, mobileNo, emailAddress, dateOfBirth, gender, joinedDate,
                imageId, staffId, staffName, officeOptions, groups, staffOptions, narrations, genderOptions, timeline, savingProductOptions,
                savingsProductId, savingsProductName, savingsAccountId, savingAccountOptions, clientType, clientClassification,
                clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions, clientNonPersonMainBusinessLineOptions,
                clientNonPersonDetails, clientLegalFormOptions, familyMemberOptions, legalForm, address, isAddressEnabled, datatables,
                isStaff, clientCollateralManagements // , countryValues, stateValues
                // , lgaValues
                , activationChannelOptions, bankAccountTypeOptions, bankOptions, salaryRangeOptions, employmentTypeOptions);

    }

    private ClientBusinessData(final String accountNo, final EnumOptionData status, final CodeValueData subStatus, final Long officeId,
            final String officeName, final Long transferToOfficeId, final String transferToOfficeName, final Long id,
            final String firstname, final String middlename, final String lastname, final String fullname, final String displayName,
            final String externalId, final String mobileNo, final String emailAddress, final LocalDate dateOfBirth,
            final CodeValueData gender, final LocalDate activationDate, final Long imageId, final Long staffId, final String staffName,
            final Collection<OfficeData> allowedOffices, final Collection<GroupGeneralData> groups,
            final Collection<StaffData> staffOptions, final Collection<CodeValueData> narrations,
            final Collection<CodeValueData> genderOptions, final ClientTimelineData timeline,
            final Collection<SavingsProductData> savingProductOptions, final Long savingsProductId, final String savingsProductName,
            final Long savingsAccountId, final Collection<SavingsAccountData> savingAccountOptions, final CodeValueData clientType,
            final CodeValueData clientClassification, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions, final Collection<CodeValueData> clientNonPersonConstitutionOptions,
            final Collection<CodeValueData> clientNonPersonMainBusinessLineOptions, final ClientNonPersonData clientNonPerson,
            final List<EnumOptionData> clientLegalFormOptions, final ClientFamilyMembersData familyMemberOptions,
            final EnumOptionData legalForm, final Collection<AddressData> address, final Boolean isAddressEnabled,
            final List<DatatableData> datatables, final Boolean isStaff,
            final Set<ClientCollateralManagementData> clientCollateralManagements,
            final Collection<CodeValueBusinessData> activationChannelOptions,
            final Collection<CodeValueBusinessData> bankAccountTypeOptions, final Collection<CodeValueBusinessData> bankOptions,
            final Collection<CodeValueBusinessData> salaryRangeOptions, final Collection<CodeValueBusinessData> employmentTypeOptions
    // , final Collection<CodeValueBusinessData> countryValues,
    // final Collection<CodeValueBusinessData> stateValues,
    // final Collection<CodeValueBusinessData> lgaValues
    ) {
        this.accountNo = accountNo;
        this.status = status;
        if (status != null) {
            this.active = status.getId().equals(300L);
        } else {
            this.active = null;
        }
        this.subStatus = subStatus;
        this.officeId = officeId;
        this.officeName = officeName;
        this.transferToOfficeId = transferToOfficeId;
        this.transferToOfficeName = transferToOfficeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.mobileNo = StringUtils.defaultIfEmpty(mobileNo, null);
        this.emailAddress = StringUtils.defaultIfEmpty(emailAddress, null);
        this.activationDate = activationDate;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.clientClassification = clientClassification;
        this.clientType = clientType;
        this.imageId = imageId;
        if (imageId != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.staffId = staffId;
        this.staffName = staffName;

        // associations
        this.groups = groups;

        // template
        this.officeOptions = allowedOffices;
        this.staffOptions = staffOptions;
        this.narrations = narrations;

        this.genderOptions = genderOptions;
        this.clientClassificationOptions = clientClassificationOptions;
        this.clientTypeOptions = clientTypeOptions;

        this.clientNonPersonConstitutionOptions = clientNonPersonConstitutionOptions;
        this.clientNonPersonMainBusinessLineOptions = clientNonPersonMainBusinessLineOptions;
        this.clientLegalFormOptions = clientLegalFormOptions;
        this.familyMemberOptions = familyMemberOptions;

        this.timeline = timeline;
        this.savingProductOptions = savingProductOptions;
        this.savingsProductId = savingsProductId;
        this.savingsProductName = savingsProductName;
        this.savingsAccountId = savingsAccountId;
        this.savingAccountOptions = savingAccountOptions;
        this.legalForm = legalForm;
        this.isStaff = isStaff;
        this.clientNonPersonDetails = clientNonPerson;

        this.address = address;
        this.isAddressEnabled = isAddressEnabled;
        this.datatables = datatables;
        this.clientCollateralManagements = clientCollateralManagements;
        // this.countryValues = countryValues;
        // this.stateValues = stateValues;
        // this.lgaValues = lgaValues;
        this.activationChannelOptions = activationChannelOptions;
        this.bankAccountTypeOptions = bankAccountTypeOptions;
        this.bankOptions = bankOptions;
        this.salaryRangeOptions = salaryRangeOptions;
        this.employmentTypeOptions = employmentTypeOptions;
    }

    public Long id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public String accountNo() {
        return this.accountNo;
    }

    public Long officeId() {
        return this.officeId;
    }

    public String officeName() {
        return this.officeName;
    }

    public Long getImageId() {
        return this.imageId;
    }

    public Boolean getImagePresent() {
        return this.imagePresent;
    }

    public ClientTimelineData getTimeline() {
        return this.timeline;
    }

    @Override
    public int compareTo(final ClientBusinessData obj) {
        if (obj == null) {
            return -1;
        }
        return new CompareToBuilder() //
                .append(this.id, obj.id) //
                .append(this.displayName, obj.displayName) //
                .append(this.mobileNo, obj.mobileNo) //
                .append(this.emailAddress, obj.emailAddress) //
                .toComparison();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final ClientBusinessData rhs = (ClientBusinessData) obj;
        return new EqualsBuilder() //
                .append(this.id, rhs.id) //
                .append(this.displayName, rhs.displayName) //
                .append(this.mobileNo, rhs.mobileNo) //
                .append(this.emailAddress, rhs.emailAddress) //
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37) //
                .append(this.id) //
                .append(this.displayName) //
                .toHashCode();
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public LocalDate getActivationDate() {
        return this.activationDate;
    }

    public Boolean getIsAddressEnabled() {
        return this.isAddressEnabled;
    }

}
