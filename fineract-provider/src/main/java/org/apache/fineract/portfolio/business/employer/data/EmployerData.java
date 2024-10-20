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
package org.apache.fineract.portfolio.business.employer.data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.address.data.business.AddressBusinessData;
import org.apache.fineract.portfolio.client.data.ClientData;

public class EmployerData implements Serializable {

    private final Long id;

    private final String externalId;

    private final String mobileNo;

    private final String contactPerson;

    private final String emailAddress;

    private final String emailExtension;

    private final String name;

    private final String slug;

    private final String rcNumber;

    private final CodeValueData state;

    private final CodeValueData country;

    private final CodeValueData clientClassification;

    private final CodeValueData industry;

    private final CodeValueData lga;

    private final String officeAddress;

    private final String nearestLandMark;

    private final Boolean active;

    private final ClientData business;

    private final AddressBusinessData addressOptions;

    private final Collection<CodeValueData> clientClassificationOptions;
    private final Collection<CodeValueData> industryOptions;

    private final StaffData staffData;
    private final StaffData supervisorStaffData;
    private final LocalDate createdOn;

    public static EmployerData template(AddressBusinessData addressOptions, Collection<CodeValueData> clientClassificationOptions,
            Collection<CodeValueData> industryOptions) {
        final Long id = null;
        final String externalId = null;
        final String mobileNo = null;
        final String contactPerson = null;
        final String emailAddress = null;
        final String emailExtension = null;
        final String name = null;
        final String slug = null;
        final String rcNumber = null;
        final CodeValueData state = null;
        final CodeValueData country = null;
        final CodeValueData clientClassification = null;
        final CodeValueData industry = null;
        final CodeValueData lga = null;
        final String officeAddress = null;
        final String nearestLandMark = null;
        final Boolean active = null;
        final ClientData business = null;
        final LocalDate createdOn = null;
        final StaffData staffData = null;
        final StaffData supervisorStaffData = null;
        return new EmployerData(id, externalId, mobileNo, contactPerson, emailAddress, emailExtension, name, slug, rcNumber, state, country,
                clientClassification, industry, lga, officeAddress, nearestLandMark, active, business, addressOptions,
                clientClassificationOptions, industryOptions, staffData, supervisorStaffData, createdOn);
    }

    public static EmployerData instance(Long id, String externalId, String mobileNo, String contactPerson, String emailAddress,
            String emailExtension, String name, String slug, String rcNumber, CodeValueData state, CodeValueData country,
            CodeValueData clientClassification, CodeValueData industry, CodeValueData lga, String officeAddress, String nearestLandMark,
            Boolean active, ClientData business, final StaffData staffData, final StaffData supervisorStaffData, LocalDate createdOn) {
        final AddressBusinessData addressOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<CodeValueData> industryOptions = null;
        return new EmployerData(id, externalId, mobileNo, contactPerson, emailAddress, emailExtension, name, slug, rcNumber, state, country,
                clientClassification, industry, lga, officeAddress, nearestLandMark, active, business, addressOptions,
                clientClassificationOptions, industryOptions, staffData, supervisorStaffData, createdOn);
    }

    public EmployerData(Long id, String externalId, String mobileNo, String contactPerson, String emailAddress, String emailExtension,
            String name, String slug, String rcNumber, CodeValueData state, CodeValueData country, CodeValueData clientClassification,
            CodeValueData industry, CodeValueData lga, String officeAddress, String nearestLandMark, Boolean active, ClientData business,
            AddressBusinessData addressOptions, Collection<CodeValueData> clientClassificationOptions,
            Collection<CodeValueData> industryOptions, StaffData staffData, StaffData supervisorStaffData, LocalDate createdOn) {
        this.id = id;
        this.externalId = externalId;
        this.mobileNo = mobileNo;
        this.contactPerson = contactPerson;
        this.emailAddress = emailAddress;
        this.emailExtension = emailExtension;
        this.name = name;
        this.slug = slug;
        this.rcNumber = rcNumber;
        this.state = state;
        this.country = country;
        this.clientClassification = clientClassification;
        this.industry = industry;
        this.lga = lga;
        this.officeAddress = officeAddress;
        this.nearestLandMark = nearestLandMark;
        this.active = active;
        this.business = business;
        this.addressOptions = addressOptions;
        this.clientClassificationOptions = clientClassificationOptions;
        this.industryOptions = industryOptions;
        this.staffData = staffData;
        this.supervisorStaffData = supervisorStaffData;
        this.createdOn = createdOn;
    }

    public Long getId() {
        return id;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailExtension() {
        return emailExtension;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getRcNumber() {
        return rcNumber;
    }

    public CodeValueData getState() {
        return state;
    }

    public CodeValueData getCountry() {
        return country;
    }

    public CodeValueData getClientClassification() {
        return clientClassification;
    }

    public CodeValueData getIndustry() {
        return industry;
    }

    public CodeValueData getLga() {
        return lga;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public String getNearestLandMark() {
        return nearestLandMark;
    }

    public Boolean getActive() {
        return active;
    }

    public ClientData getBusiness() {
        return business;
    }

    public AddressBusinessData getAddressOptions() {
        return addressOptions;
    }

    public Collection<CodeValueData> getClientClassificationOptions() {
        return clientClassificationOptions;
    }

    public Collection<CodeValueData> getIndustryOptions() {
        return industryOptions;
    }

    public StaffData getStaffData() {
        return staffData;
    }

    public StaffData getSupervisorStaffData() {
        return supervisorStaffData;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

}
