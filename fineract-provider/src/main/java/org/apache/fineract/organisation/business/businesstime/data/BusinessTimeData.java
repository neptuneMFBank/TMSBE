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
package org.apache.fineract.organisation.business.businesstime.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.useradministration.data.RoleData;

public class BusinessTimeData {

    private final Long id;
    private final Integer weekDayId;
    private final Long roleId;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private final String createdByUsername;
    private final Long createdById;
    private final LocalDate createdOn;

    private final String lastModifiedByUsername;
    private final Long lastModifiedById;
    private final LocalDate lastModifiedOnUtc;

    private final Collection<EnumOptionData> weeksList;
    private final Collection<RoleData> roles;

    private BusinessTimeData(final Long id, final Integer weekDayId, final Long roleId, final LocalTime startTime, final LocalTime endTime,
            final String createdByUsername, final Long createdById, final LocalDate createdOn, final String lastModifiedByUsername,
            final Long lastModifiedById, final LocalDate lastModifiedOnUtc, final Collection<EnumOptionData> weeksList,
            final Collection<RoleData> roles) {
        this.id = id;
        this.weekDayId = weekDayId;
        this.roleId = roleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdByUsername = createdByUsername;
        this.createdById = createdById;
        this.createdOn = createdOn;
        this.lastModifiedByUsername = lastModifiedByUsername;
        this.lastModifiedById = lastModifiedById;
        this.lastModifiedOnUtc = lastModifiedOnUtc;
        this.weeksList = weeksList;
        this.roles = roles;
    }

    public static BusinessTimeData template(Collection<EnumOptionData> weeksList, final Collection<RoleData> roles) {
        final Long id = null;
        final Integer weekDayId = null;
        final Long roleId = null;
        final LocalTime startTime = null;
        final LocalTime endTime = null;

        final String createdByUsername = null;
        final Long createdById = null;
        final LocalDate createdOn = null;

        final String lastModifiedByUsername = null;
        final Long lastModifiedById = null;
        final LocalDate lastModifiedOnUtc = null;
        return new BusinessTimeData(id, weekDayId, roleId, startTime, endTime, createdByUsername, createdById, createdOn,
                lastModifiedByUsername, lastModifiedById, lastModifiedOnUtc, weeksList, roles);
    }

    public static BusinessTimeData instance(final Long id, final Integer weekDayId, final Long roleId, final LocalTime startTime,
            final LocalTime endTime, final String createdByUsername, final Long createdById, final LocalDate createdOn,
            final String lastModifiedByUsername, final Long lastModifiedById, final LocalDate lastModifiedOnUtc) {
        final Collection<EnumOptionData> weeksList = null;
        final Collection<RoleData> roles = null;
        return new BusinessTimeData(id, weekDayId, roleId, startTime, endTime, createdByUsername, createdById, createdOn,
                lastModifiedByUsername, lastModifiedById, lastModifiedOnUtc, weeksList, roles);
    }
}
