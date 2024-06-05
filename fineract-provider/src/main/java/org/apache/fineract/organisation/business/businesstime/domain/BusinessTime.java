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
package org.apache.fineract.organisation.business.businesstime.domain;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.apache.fineract.organisation.business.businesstime.api.BusinessTimeApiResourceConstants;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;

@Entity
@Table(name = "m_business_time")
public class BusinessTime extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "week_day_id", nullable = false)
    private Integer weekDayId;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    private BusinessTime(Long roleId, Integer weekDayId, LocalTime startTime, LocalTime endTime) {
        this.roleId = roleId;
        this.weekDayId = weekDayId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    protected BusinessTime() {
    }

    public static BusinessTime instance(Long roleId, Integer weekDayId, LocalTime startTime, LocalTime endTime) {
        return new BusinessTime(roleId, weekDayId, startTime, endTime);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(10);

        if (command.isChangeInLongParameterNamed(BusinessTimeApiResourceConstants.ROLE_ID, this.roleId)) {
            final Long newValue = command.longValueOfParameterNamed(BusinessTimeApiResourceConstants.ROLE_ID);
            actualChanges.put(BusinessTimeApiResourceConstants.ROLE_ID, newValue);
            this.roleId = newValue;
        }
        if (command.isChangeInIntegerParameterNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID, this.weekDayId)) {
            final Integer newValue = command.integerValueOfParameterNamed(BusinessTimeApiResourceConstants.WEEK_DAY_ID);
            actualChanges.put(BusinessTimeApiResourceConstants.WEEK_DAY_ID, newValue);
            this.weekDayId = newValue;
        }
        String timeFormat = command.stringValueOfParameterNamed(BusinessTimeApiResourceConstants.TIME_FORMAT);
        if (command.isChangeInTimeParameterNamed(BusinessTimeApiResourceConstants.START_TIME, this.startTime, timeFormat)) {
            final LocalTime newValue = command.localTimeValueOfParameterNamed(BusinessTimeApiResourceConstants.START_TIME);
            actualChanges.put(BusinessTimeApiResourceConstants.START_TIME, newValue);
            this.startTime = newValue;
        }
        if (command.isChangeInTimeParameterNamed(BusinessTimeApiResourceConstants.END_TIME, this.endTime, timeFormat)) {
            final LocalTime newValue = command.localTimeValueOfParameterNamed(BusinessTimeApiResourceConstants.END_TIME);
            actualChanges.put(BusinessTimeApiResourceConstants.END_TIME, newValue);
            this.endTime = newValue;
        }

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors)
                .resource(BusinessTimeApiResourceConstants.RESOURCE);

        if (this.startTime != null && this.endTime != null && !this.startTime.isBefore(this.endTime)) {

            baseDataValidator.reset().parameter(BusinessTimeApiResourceConstants.START_TIME).value(startTime)
                    .failWithCodeNoParameterAddedToErrorCode("starttime.cannot.be.after.endtime");
            if (!dataValidationErrors.isEmpty()) {
                throw new PlatformApiDataValidationException(dataValidationErrors);
            }
        }
        return actualChanges;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

}
