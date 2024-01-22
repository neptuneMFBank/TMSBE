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
package org.apache.fineract.portfolio.business.overdraft.data;

import java.io.Serializable;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.client.data.ClientData;

@SuppressWarnings("unused")
public class OverdraftData implements Serializable {

    private final Long id;
    private final Long loanId;
    private final Long savingsId;
    private final EnumOptionData status;
    private final ClientData clientData;
    private final StaffData loanOfficerData;
    private final StaffData staffData;
    private final StaffData supervisorStaffData;
    private final LocalDate createdOn;
    private final LocalDate modifiedOn;

    public static OverdraftData instance(Long id, Long loanId, final Long savingsId, EnumOptionData status, StaffData staffData,
            StaffData supervisorStaffData, LocalDate createdOn, LocalDate modifiedOn, final ClientData clientData,
            final StaffData loanOfficerData) {
        return new OverdraftData(id, loanId, savingsId, status, staffData, supervisorStaffData, createdOn, modifiedOn, clientData,
                loanOfficerData);
    }

    public OverdraftData(Long id, Long loanId, Long savingsId, EnumOptionData status, StaffData staffData, StaffData supervisorStaffData,
            LocalDate createdOn, LocalDate modifiedOn, ClientData clientData, StaffData loanOfficerData) {
        this.id = id;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.status = status;
        this.staffData = staffData;
        this.supervisorStaffData = supervisorStaffData;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.clientData = clientData;
        this.loanOfficerData = loanOfficerData;
    }

    public Long getId() {
        return id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public Long getSavingsId() {
        return savingsId;
    }

    public EnumOptionData getStatus() {
        return status;
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

    public LocalDate getModifiedOn() {
        return modifiedOn;
    }

    public ClientData getClientData() {
        return clientData;
    }

    public StaffData getLoanOfficerData() {
        return loanOfficerData;
    }

}
