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
package org.apache.fineract.portfolio.address.domain.business;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.address.domain.Address;
import org.springframework.stereotype.Component;

@Entity
@Component
@Table(name = "m_address_other")
public class AddressOther extends AbstractPersistableCustom {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lga_id")
    private CodeValue lga;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "residence_status_id")
    private CodeValue resisdenceStatus;

    @Column(name = "date_moved_in")
    private LocalDate dateMovedIn;

    @OneToOne(optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    protected AddressOther() {}

    public static AddressOther instance(CodeValue lga, CodeValue resisdenceStatus, LocalDate dateMovedIn, Address address) {
        return new AddressOther(lga, resisdenceStatus, dateMovedIn, address);
    }

    public AddressOther(CodeValue lga, CodeValue resisdenceStatus, LocalDate dateMovedIn, Address address) {
        this.lga = lga;
        this.resisdenceStatus = resisdenceStatus;
        this.dateMovedIn = dateMovedIn;
        this.address = address;
    }

    public CodeValue getResisdenceStatus() {
        return resisdenceStatus;
    }

    public LocalDate getDateMovedIn() {
        return dateMovedIn;
    }

    public Address getAddress() {
        return address;
    }

    public void setResisdenceStatus(CodeValue resisdenceStatus) {
        this.resisdenceStatus = resisdenceStatus;
    }

    public void setDateMovedIn(LocalDate dateMovedIn) {
        this.dateMovedIn = dateMovedIn;
    }

    public void setLga(CodeValue lga) {
        this.lga = lga;
    }

    public CodeValue getLga() {
        return lga;
    }

}
