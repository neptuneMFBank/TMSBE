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
package org.apache.fineract.useradministration.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AppUserBusinessReadPlatformServiceImpl implements AppUserBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final RoleReadPlatformService roleReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ColumnValidator columnValidator;

    @Autowired
    public AppUserBusinessReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final RoleReadPlatformService roleReadPlatformService, final StaffReadPlatformService staffReadPlatformService,
            final ColumnValidator columnValidator, DatabaseSpecificSQLGenerator sqlGenerator, final PaginationHelper paginationHelper) {
        this.context = context;
        this.roleReadPlatformService = roleReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.staffReadPlatformService = staffReadPlatformService;
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
        this.columnValidator = columnValidator;
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.LOCK_INACTIVITY_STAFF_USER)
    public void lockInactivityStaffUser() {
        final String sqlFinder = "select mulv.id from m_users_lock_view mulv ";
        List<Long> staffUserIds = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        log.info("lockInactivityStaffUser start");
        for (Long staffUserId : staffUserIds) {
            String staffUserIdUpdateSql = "UPDATE m_appuser SET nonlocked=? WHERE id=?";
            jdbcTemplate.update(staffUserIdUpdateSql, 0, staffUserId);
        }
        log.info("{}: Records affected by lockInactivityStaffUser: {}", ThreadLocalContextUtil.getTenant().getName(),
                staffUserIds.size());
    }

    @Override
    public Page<AppUserData> retrieveAllUsers(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        final AppUserMapper mapper = new AppUserMapper(this.roleReadPlatformService, this.staffReadPlatformService);
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(mapper.schema());

        sqlBuilder.append(" where u.is_deleted=false ");

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromUserCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" and (").append(extraCriteria).append(")");
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" limit ").append(searchParameters.getLimit());
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(" offset ").append(searchParameters.getOffset());
                }
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), mapper);

    }

    private String buildSqlStringFromUserCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        // u.username,u.is_self_service_user,u.office_id
        final Long officeId = searchParameters.getOfficeId();
        final String username = searchParameters.getUsername();
        final Boolean active = searchParameters.isActive();
        final Boolean isSelfUser = searchParameters.isSelfUser();
        final Boolean locked = searchParameters.getLocked();

        String extraCriteria = "";

        // if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
        // final LocalDate startPeriod = searchParameters.getFromDate();
        // final LocalDate endPeriod = searchParameters.getToDate();
        //
        // final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
        // if (startPeriod != null && endPeriod != null) {
        // extraCriteria += " and CAST(s.joining_date AS DATE) BETWEEN ? AND ? ";
        // paramList.add(df.format(startPeriod));
        // paramList.add(df.format(endPeriod));
        // } else if (startPeriod != null) {
        // extraCriteria += " and CAST(s.joining_date AS DATE) >= ? ";
        // paramList.add(df.format(startPeriod));
        // } else if (endPeriod != null) {
        // extraCriteria += " and CAST(s.joining_date AS DATE) <= ? ";
        // paramList.add(df.format(endPeriod));
        // }
        // }
        if (searchParameters.isOfficeIdPassed()) {
            extraCriteria += " and u.office_id = ? ";
            paramList.add(officeId);
        }

        if (searchParameters.isUsernamePassed()) {
            paramList.add("%".concat(username.concat("%")));
            extraCriteria += " and u.username like ? ";
        }
        if (searchParameters.isActivePassed()) {
            extraCriteria += " and u.enabled = ? ";
            paramList.add(active);
        }
        if (searchParameters.isLockedPassed()) {
            extraCriteria += " and u.nonlocked = ? ";
            paramList.add(locked);
        }
        if (searchParameters.isSelfUserPassed()) {
            extraCriteria += " and u.is_self_service_user = ? ";
            paramList.add(isSelfUser);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class AppUserMapper implements RowMapper<AppUserData> {

        private final RoleReadPlatformService roleReadPlatformService;
        private final StaffReadPlatformService staffReadPlatformService;

        AppUserMapper(final RoleReadPlatformService roleReadPlatformService, final StaffReadPlatformService staffReadPlatformService) {
            this.roleReadPlatformService = roleReadPlatformService;
            this.staffReadPlatformService = staffReadPlatformService;
        }

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String email = rs.getString("email");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final Boolean enabled = rs.getBoolean("enabled");
            final Boolean nonlocked = rs.getBoolean("nonlocked");
            final Boolean passwordNeverExpire = rs.getBoolean("passwordNeverExpires");
            final Boolean isSelfServiceUser = rs.getBoolean("isSelfServiceUser");
            final Collection<RoleData> selectedRoles = this.roleReadPlatformService.retrieveAppUserRoles(id);

            final StaffData linkedStaff;
            if (staffId != null) {
                linkedStaff = this.staffReadPlatformService.retrieveStaff(staffId);
            } else {
                linkedStaff = null;
            }
            final AppUserData appUserData = AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, null,
                    selectedRoles, linkedStaff, passwordNeverExpire, isSelfServiceUser);
            appUserData.setActive(enabled);
            appUserData.setNonlocked(nonlocked);
            return appUserData;
        }

        public String schema() {
            return " u.nonlocked, u.enabled as enabled, u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.password_never_expires as passwordNeverExpires, "
                    + " u.office_id as officeId, o.name as officeName, u.staff_id as staffId, u.is_self_service_user as isSelfServiceUser from m_appuser u "
                    + " join m_office o on o.id = u.office_id ";
        }

    }

    @Override
    public Collection<AppUserData> retrieveActiveAppUsersForRole(Long roleId) {
        final RetrieveAppUserMapper mapper = new RetrieveAppUserMapper();
        final String sql = "select " + mapper.schema() + " and ar.role_id=? ";

        return this.jdbcTemplate.query(sql, mapper, roleId);
    }

    private static final class RetrieveAppUserMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = null;
            final String firstname = null;
            final String lastname = null;
            final String email = null;
            final Long officeId = null;
            final String officeName = null;
            final Boolean passwordNeverExpire = null;
            final Boolean isSelfServiceUser = null;
            final Collection<RoleData> selectedRoles = null;

            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final StaffData linkedStaff = StaffData.lookup(staffId, null);

            final AppUserData appUserData = AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, null,
                    selectedRoles, linkedStaff, passwordNeverExpire, isSelfServiceUser);
            return appUserData;
        }

        public String schema() {
            return "  u.id id, u.staff_id staffId from m_appuser u " + " JOIN m_appuser_role ar ON ar.appuser_id = u.id "
                    + " JOIN m_staff ms on ms.id = u.staff_id WHERE u.enabled=true AND u.is_deleted=false AND ms.is_active=true ";
        }

    }

}
