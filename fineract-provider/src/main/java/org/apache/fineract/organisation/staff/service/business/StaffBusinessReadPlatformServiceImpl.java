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
package org.apache.fineract.organisation.staff.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.data.business.StaffBusinessData;
import org.apache.fineract.organisation.staff.exception.StaffNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class StaffBusinessReadPlatformServiceImpl implements StaffBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ColumnValidator columnValidator;

    @Autowired
    public StaffBusinessReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final ColumnValidator columnValidator) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.columnValidator = columnValidator;
    }

    @Override
    public StaffBusinessData retrieveStaff(final Long staffId) {

        // adding the Authorization criteria so that a user cannot see an
        // employee who does not belong to his office or a sub office for his
        // office.
        final String hierarchy = this.context.authenticatedUser().getOffice().getHierarchy() + "%";

        try {
            final StaffMapper rm = new StaffMapper();
            final String sql = "select " + rm.schema() + " where s.id = ? and o.hierarchy like ? ";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[]{staffId, hierarchy}); // NOSONAR
        } catch (final EmptyResultDataAccessException e) {
            throw new StaffNotFoundException(staffId, e);
        }
    }

    private static final class StaffMapper implements RowMapper<StaffBusinessData> {

        public String schema() {
            return " s.id as id,s.office_id as officeId, o.name as officeName, s.firstname as firstname, s.lastname as lastname,"
                    + " s.display_name as displayName, s.is_loan_officer as isLoanOfficer, s.external_id as externalId, s.mobile_no as mobileNo,"
                    + " s.is_active as isActive, s.joining_date as joiningDate "
                    + " s.organisational_role_parent_staff_id organisationalRoleParentStaff, ms.display_name organisationalRoleParentStaffName, s.organisational_role_enum organisationalRoleType, cv.code_value organisationalRoleTypeName "
                    + " from m_staff s "
                    + " join m_office o on o.id = s.office_id "
                    + " left join m_staff ms on ms.id=s.organisational_role_parent_staff_id "
                    + " left join m_code_value mc on mc.id=s.organisational_role_enum ";
        }

        @Override
        public StaffBusinessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final boolean isLoanOfficer = rs.getBoolean("isLoanOfficer");
            final String officeName = rs.getString("officeName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isActive = rs.getBoolean("isActive");
            final LocalDate joiningDate = JdbcSupport.getLocalDate(rs, "joiningDate");

            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaff = JdbcSupport.getLongDefaultToNullIfZero(rs, "organisationalRoleParentStaff");
            if (organisationalRoleParentStaff != null) {
                final String organisationalRoleParentStaffName = rs.getString("organisationalRoleParentStaffName");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaff, organisationalRoleParentStaffName);
            }

            CodeValueData organisationalRoleTypeData = null;
            final Long organisationalRoleType = JdbcSupport.getLongDefaultToNullIfZero(rs, "organisationalRoleType");
            if (organisationalRoleType != null) {
                final String organisationalRoleTypeName = rs.getString("organisationalRoleTypeName");
                organisationalRoleTypeData = CodeValueData.instance(organisationalRoleType, organisationalRoleTypeName);
            }

            return StaffBusinessData.instance(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, externalId, mobileNo,
                    isActive, joiningDate, organisationalRoleTypeData, supervisorStaffData);
        }
    }

}
