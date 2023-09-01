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
package org.apache.fineract.organisation.staff.data.business;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;

/**
 * Immutable data object representing staff data.
 */
public final class StaffBusinessData implements Serializable {

    private final Long id;
    private final String externalId;
    private final String firstname;
    private final String lastname;
    private final String displayName;
    private final String mobileNo;
    private final Long officeId;
    private final String officeName;
    private final Boolean isLoanOfficer;
    private final Boolean isActive;
    private final LocalDate joiningDate;

    private final CodeValueData organisationalRoleType;
    private final StaffData organisationalRoleParentStaff;

    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;

    public static StaffBusinessData templateData(final StaffBusinessData staff, final Collection<OfficeData> allowedOffices) {
        return new StaffBusinessData(staff.id, staff.firstname, staff.lastname, staff.displayName, staff.officeId, staff.officeName,
                staff.isLoanOfficer, staff.externalId, staff.mobileNo, allowedOffices, staff.isActive, staff.joiningDate, staff.organisationalRoleType, staff.organisationalRoleParentStaff);
    }

    public static StaffBusinessData instance(final Long id, final String firstname, final String lastname, final String displayName,
            final Long officeId, final String officeName, final Boolean isLoanOfficer, final String externalId, final String mobileNo,
            final boolean isActive, final LocalDate joiningDate, final CodeValueData organisationalRoleType, final StaffData organisationalRoleParentStaff) {
        return new StaffBusinessData(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId, mobileNo, null,
                isActive, joiningDate, organisationalRoleType, organisationalRoleParentStaff);
    }

    private StaffBusinessData(final Long id, final String firstname, final String lastname, final String displayName, final Long officeId,
            final String officeName, final Boolean isLoanOfficer, final String externalId, final String mobileNo,
            final Collection<OfficeData> allowedOffices, final Boolean isActive, final LocalDate joiningDate,
            final CodeValueData organisationalRoleType, final StaffData organisationalRoleParentStaff) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.displayName = displayName;
        this.officeName = officeName;
        this.isLoanOfficer = isLoanOfficer;
        this.externalId = externalId;
        this.officeId = officeId;
        this.mobileNo = mobileNo;
        this.allowedOffices = allowedOffices;
        this.isActive = isActive;
        this.joiningDate = joiningDate;
        this.organisationalRoleType = organisationalRoleType;
        this.organisationalRoleParentStaff = organisationalRoleParentStaff;
    }

    public Long getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public LocalDate getJoiningDate() {
        return this.joiningDate;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public Boolean getIsLoanOfficer() {
        return isLoanOfficer;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public CodeValueData getOrganisationalRoleType() {
        return organisationalRoleType;
    }

    public StaffData getOrganisationalRoleParentStaff() {
        return organisationalRoleParentStaff;
    }

    public Collection<OfficeData> getAllowedOffices() {
        return allowedOffices;
    }

}
