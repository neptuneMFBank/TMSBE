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
package org.apache.fineract.portfolio.business.employer.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.portfolio.client.domain.Client;

@Entity
@Table(name = "m_employer", uniqueConstraints = { @UniqueConstraint(columnNames = { "rc_number" }, name = "employer_rc_number_UNIQUE"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "employer_external_id_UNIQUE"),
        @UniqueConstraint(columnNames = { "name" }, name = "employer_name_UNIQUE"), // @UniqueConstraint(columnNames =
                                                                                    // {"mobile_no"}, name =
                                                                                    // "employer_mobile_no_UNIQUE"),
// @UniqueConstraint(columnNames = {"email_address"}, name = "employer_email_address_UNIQUE")
})
public class Employer extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "client_classification_cv_id", nullable = false)
    private CodeValue clientClassification;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug")
    private String slug;

    @Column(name = "mobile_no")
    private String mobileNo;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "email_extension")
    private String emailExtension;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "rc_number")
    private String rcNumber;

    @ManyToOne
    @JoinColumn(name = "industry_id")
    private CodeValue industry;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private Client business;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private CodeValue state;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private CodeValue country;

    @ManyToOne
    @JoinColumn(name = "lga_id")
    private CodeValue lga;

    @ManyToOne
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @Column(name = "office_address")
    private String officeAddress;

    @Column(name = "nearest_land_mark")
    private String nearestLandMark;

    @Column(name = "active")
    private Boolean active;

    protected Employer() {}

    private Employer(CodeValue clientClassification, String externalId, String name, String slug, String mobileNo, String emailAddress,
            String emailExtension, String contactPerson, String rcNumber, CodeValue industry, Client business, CodeValue state,
            CodeValue country, CodeValue lga, String officeAddress, String nearestLandMark, Boolean active, Staff staff) {
        this.clientClassification = clientClassification;
        this.externalId = externalId;
        this.name = name;
        this.slug = slug;
        this.mobileNo = mobileNo;
        this.emailAddress = emailAddress;
        this.emailExtension = emailExtension;
        this.contactPerson = contactPerson;
        this.rcNumber = rcNumber;
        this.industry = industry;
        this.business = business;
        this.state = state;
        this.country = country;
        this.lga = lga;
        this.officeAddress = officeAddress;
        this.nearestLandMark = nearestLandMark;
        this.active = active;
        this.staff = staff;
    }

    public static Employer create(CodeValue clientClassification, String externalId, String name, String slug, String mobileNo,
            String emailAddress, String emailExtension, String contactPerson, String rcNumber, CodeValue industry, Client business,
            CodeValue state, CodeValue country, CodeValue lga, String officeAddress, String nearestLandMark, Boolean active, Staff staff) {
        return new Employer(clientClassification, externalId, name, slug, mobileNo, emailAddress, emailExtension, contactPerson, rcNumber,
                industry, business, state, country, lga, officeAddress, nearestLandMark, active, staff);
    }

    public CodeValue getClientClassification() {
        return clientClassification;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getEmailExtension() {
        return emailExtension;
    }

    public String getRcNumber() {
        return rcNumber;
    }

    public CodeValue getIndustry() {
        return industry;
    }

    public Client getBusiness() {
        return business;
    }

    public CodeValue getState() {
        return state;
    }

    public CodeValue getCountry() {
        return country;
    }

    public CodeValue getLga() {
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

    public String getContactPerson() {
        return contactPerson;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setClientClassification(CodeValue clientClassification) {
        this.clientClassification = clientClassification;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setEmailExtension(String emailExtension) {
        this.emailExtension = emailExtension;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public void setRcNumber(String rcNumber) {
        this.rcNumber = rcNumber;
    }

    public void setIndustry(CodeValue industry) {
        this.industry = industry;
    }

    public void setBusiness(Client business) {
        this.business = business;
    }

    public void setState(CodeValue state) {
        this.state = state;
    }

    public void setCountry(CodeValue country) {
        this.country = country;
    }

    public void setLga(CodeValue lga) {
        this.lga = lga;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public void setNearestLandMark(String nearestLandMark) {
        this.nearestLandMark = nearestLandMark;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}
