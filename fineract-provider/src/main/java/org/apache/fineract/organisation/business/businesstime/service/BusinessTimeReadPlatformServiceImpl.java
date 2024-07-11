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
package org.apache.fineract.organisation.business.businesstime.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.business.businesstime.data.BusinessTimeData;
import org.apache.fineract.organisation.business.businesstime.exception.BusinessTimeNotFoundException;
import org.apache.fineract.portfolio.calendar.domain.CalendarWeekDaysType;
import org.apache.fineract.portfolio.calendar.service.CalendarEnumerations;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BusinessTimeReadPlatformServiceImpl implements BusinessTimeReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final BusinessTimeMapper businessTimeMapper = new BusinessTimeMapper();
    private final RoleReadPlatformService roleReadPlatformService;

    @Override
    public BusinessTimeData retrieveTemplate() {
        this.context.authenticatedUser();

        Collection<EnumOptionData> weeksList = CalendarEnumerations.calendarWeekDaysType(CalendarWeekDaysType.values());
        Collection<RoleData> roles = this.roleReadPlatformService.retrieveAllActiveRoles();
        final BusinessTimeData businessTimeData = BusinessTimeData.template(weeksList, roles);
        return businessTimeData;
    }

    @Override
    public Collection<BusinessTimeData> retrieveAll(final Long roleId) {

        this.context.authenticatedUser();

        String sql = "select " + this.businessTimeMapper.schema() + "where mbt.role_id = ?";

        return this.jdbcTemplate.query(sql, this.businessTimeMapper, new Object[] { roleId });
    }

    @Override
    public BusinessTimeData retrieveOne(final Long businessTimeId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + this.businessTimeMapper.schema() + " where  mbt.id = ?";
            final BusinessTimeData businessTimeData = this.jdbcTemplate.queryForObject(sql, this.businessTimeMapper, businessTimeId);

            return businessTimeData;
        } catch (final EmptyResultDataAccessException e) {
            throw new BusinessTimeNotFoundException(businessTimeId);
        }
    }

    private static final class BusinessTimeMapper implements RowMapper<BusinessTimeData> {

        private final String schema;

        BusinessTimeMapper() {

            final StringBuilder builder = new StringBuilder(400);

            builder.append("mbt.id as id, ");
            builder.append(" mbt.week_day_id as weekDayId, ");
            builder.append(" mbt.role_id as roleId, ");
            builder.append(" mbt.start_time as startTime, ");
            builder.append(" mbt.end_time as endTime, ");

            builder.append(" sbu.username as createdByUsername, ");
            builder.append(" mbt.created_by as createdById, ");
            builder.append(" mbt.created_on_utc as createdOnUtc, ");

            builder.append(" msbu.username as lastModifiedByUsername, ");
            builder.append(" mbt.last_modified_by as lastModifiedById, ");
            builder.append(" mbt.last_modified_on_utc as lastModifiedOnUtc ");

            builder.append(" from m_business_time mbt ");
            builder.append(" left join m_appuser sbu on sbu.id = mbt.created_by ");
            builder.append(" left join m_appuser msbu on msbu.id = mbt.last_modified_by ");

            this.schema = builder.toString();

        }

        public String schema() {
            return this.schema;
        }

        @Override
        public BusinessTimeData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = JdbcSupport.getLong(rs, "id");
            final Integer weekDayId = JdbcSupport.getInteger(rs, "weekDayId");
            final Long roleId = JdbcSupport.getLong(rs, "roleId");
            final LocalTime startTime = JdbcSupport.getLocalTime(rs, "startTime");
            final LocalTime endTime = JdbcSupport.getLocalTime(rs, "endTime");

            final String createdByUsername = rs.getString("createdByUsername");
            final Long createdById = JdbcSupport.getLong(rs, "createdById");
            final LocalDate createdOn = JdbcSupport.getLocalDate(rs, "createdOnUtc");

            final String lastModifiedByUsername = rs.getString("lastModifiedByUsername");
            final Long lastModifiedById = JdbcSupport.getLong(rs, "lastModifiedById");
            final LocalDate lastModifiedOnUtc = JdbcSupport.getLocalDate(rs, "lastModifiedOnUtc");

            return BusinessTimeData.instance(id, weekDayId, roleId, startTime, endTime, createdByUsername, createdById, createdOn,
                    lastModifiedByUsername, lastModifiedById, lastModifiedOnUtc);

        }
    }
}
