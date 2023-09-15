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
package org.apache.fineract.portfolio.business.employer.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.address.data.business.AddressBusinessData;
import org.apache.fineract.portfolio.address.service.business.AddressBusinessReadPlatformService;
import org.apache.fineract.portfolio.business.employer.data.EmployerData;
import org.apache.fineract.portfolio.business.employer.exception.EmployerNotFoundException;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerReadPlatformServiceImpl implements EmployerReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployerReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final AddressBusinessReadPlatformService addressReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final EmployerViewMapper employerViewMapper = new EmployerViewMapper();
    private final EmployerMapper employerMapper = new EmployerMapper();

    @Override
    public EmployerData retrieveTemplate() {
        this.context.authenticatedUser();
        final AddressBusinessData addressOptions = this.addressReadPlatformService.retrieveTemplate();

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));

        final List<CodeValueData> industryOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION));

        final EmployerData employerData
                = EmployerData.template(addressOptions, clientClassificationOptions, industryOptions);
        return employerData;
    }

    @Override
    public Page<EmployerData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(employerViewMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromEmployerCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" where (").append(extraCriteria).append(")");
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

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(),
                this.employerViewMapper);
    }

    private String buildSqlStringFromEmployerCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long supervisorId = searchParameters.getStaffId();
        final Long industryId = searchParameters.getIndustryId();
        final Long classificationId = searchParameters.getClassificationId();
        final String name = searchParameters.getName();
        final Boolean active = searchParameters.isActive();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(me.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(me.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(me.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += " and me.organisational_role_parent_staff_id = ? ";
            paramList.add(supervisorId);
        }

        if (searchParameters.isActivePassed()) {
            extraCriteria += " and me.active = ? ";
            paramList.add(active);
        }

        if (searchParameters.isIndustryIdPassed()) {
            extraCriteria += " and me.industry_id = ? ";
            paramList.add(industryId);
        }

        if (searchParameters.isClassificationIdPassed()) {
            extraCriteria += " and me.client_classification_cv_id = ? ";
            paramList.add(classificationId);
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%".concat(name.concat("%")));
            extraCriteria += " and me.name like ? ";
        }
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public EmployerData retrieveOne(Long employerId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + employerMapper.schema() + " where me.id = ?";
            return this.jdbcTemplate.queryForObject(sql, employerMapper, new Object[]{employerId});
        } catch (DataAccessException e) {
            LOG.error("Employer not found: {}", e);
            throw new EmployerNotFoundException(employerId);
        }
    }

    private static final class EmployerViewMapper implements RowMapper<EmployerData> {

        public String schema() {
            return " me.id, me.name, me.mobile_no, me.email_address emailAddress, me.client_classification_cv_id classificationId, me.client_classification_value classificationValue, "
                    + " me.industry_id industryId, me.industry_value industryValue, me.active, me.staff_id, me.staff_display_name, "
                    + " me.organisational_role_parent_staff_id, me.organisational_role_parent_staff_display_name, me.created_on_utc createdOn "
                    + " from m_employer_view me ";
        }

        @Override
        public EmployerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String emailAddress = rs.getString("emailAddress");

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final Long industryId = JdbcSupport.getLong(rs, "industryId");
            final String industryValue = rs.getString("industryValue");
            final CodeValueData industry = CodeValueData.instance(industryId, industryValue);
            final boolean active = rs.getBoolean("active");

            StaffData staffData = null;
            Long staffId = rs.getLong("staff_id");
            if (staffId > 0) {
                final String staffDisplayName = rs.getString("staff_display_name");
                staffData = StaffData.lookup(staffId, staffDisplayName);
            }
            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaffId = rs.getLong("organisational_role_parent_staff_id");
            if (organisationalRoleParentStaffId > 0) {
                final String organisationalRoleParentStaffDisplayName = rs.getString("organisational_role_parent_staff_display_name");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaffId, organisationalRoleParentStaffDisplayName);
            }

            final LocalDateTime createdOnTime = JdbcSupport.getLocalDateTime(rs, "createdOn");
            final LocalDate createdOn = createdOnTime != null ? createdOnTime.toLocalDate() : null;

            final EmployerData employerDataResult = EmployerData.instance(id, null, null, null, emailAddress, null, name, null, null, null, null, classification, industry, null, null, null, active, null, staffData, supervisorStaffData, createdOn);
            return employerDataResult;
        }

    }

    private static final class EmployerMapper implements RowMapper<EmployerData> {

        public String schema() {
            return " mc.display_name as businessDisplayName, me.external_id as externalId, "
                    + " me.client_classification_cv_id classificationId, mcv.code_value client_classification_value classificationValue, "
                    + " me.business_id as businessId, me.id as id, me.mobile_no as mobileNo, me.email_address as emailAddress, "
                    + " me.email_extension as emailExtension, me.contact_person as contactPerson, me.name as name, me.slug as slug, me.rc_number as rcNumber,"
                    + " me.office_address as officeAddress, me.nearest_land_mark as nearestLandMark,"
                    + " me.state_id as stateId,  cvstate.code_value as stateValue, me.country_id as countryId,"
                    + " cvcountry.code_value as countryValue, me.industry_id as industryId, cvindustry.code_value as industryValue, me.lga_id as lgaId,"
                    + " cvlga.code_value as lgaValue, me.staff_id, ms.staff_display_name, ms.organisational_role_parent_staff_id, mss.organisational_role_parent_staff_display_name, "
                    + " me.active as active, me.created_on_utc createdOn from m_employer me "
                    + " LEFT JOIN m_client mc ON mc.id=me.business_id "
                    + " LEFT JOIN m_code_value mcv ON mcv.id=me.client_classification_cv_id "
                    + " LEFT JOIN m_code_value cvstate ON me.state_id=cvstate.id"
                    + " LEFT JOIN m_code_value cvcountry ON me.country_id=cvcountry.id"
                    + " LEFT JOIN m_code_value cvindustry ON me.industry_id=cvindustry.id"
                    + " LEFT JOIN m_code_value cvlga ON me.lga_id=cvlga.id"
                    + " LEFT JOIN m_staff ms ON ms.id=me.staff_id"
                    + " LEFT JOIN m_staff mss ON mss.id=ms.organisational_role_parent_staff_id";
        }

        @Override
        public EmployerData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final Long businessId = rs.getLong("businessId");
            final Long id = rs.getLong("id");

            final String externalId = rs.getString("externalId");
            final String contactPerson = rs.getString("contactPerson");
            final String emailExtension = rs.getString("emailExtension");
            final String emailAddress = rs.getString("emailAddress");
            final String mobileNo = rs.getString("mobileNo");
            final String name = rs.getString("name");
            final String slug = rs.getString("slug");
            final String rcNumber = rs.getString("rcNumber");
            final String officeAddress = rs.getString("officeAddress");
            final String nearestLandMark = rs.getString("nearestLandMark");

            final Long stateId = JdbcSupport.getLong(rs, "stateId");
            final String stateValue = rs.getString("stateValue");
            final CodeValueData state = CodeValueData.instance(stateId, stateValue);

            final Long lgaId = JdbcSupport.getLong(rs, "lgaId");
            final String lgaValue = rs.getString("lgaValue");
            final CodeValueData lga = CodeValueData.instance(lgaId, lgaValue);

            final Long countryId = JdbcSupport.getLong(rs, "countryId");
            final String countryValue = rs.getString("countryValue");
            final CodeValueData country = CodeValueData.instance(countryId, countryValue);

            final Long industryId = JdbcSupport.getLong(rs, "industryId");
            final String industryValue = rs.getString("industryValue");
            final CodeValueData industry = CodeValueData.instance(industryId, industryValue);

            final boolean active = rs.getBoolean("active");

            ClientData business = null;
            if (businessId > 0) {
                final String businessDisplayName = rs.getString("businessDisplayName");
                business = ClientData.instance(businessId, businessDisplayName);
            }

            StaffData staffData = null;
            Long staffId = rs.getLong("staff_id");
            if (staffId > 0) {
                final String staffDisplayName = rs.getString("staff_display_name");
                staffData = StaffData.lookup(staffId, staffDisplayName);
            }
            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaffId = rs.getLong("organisational_role_parent_staff_id");
            if (organisationalRoleParentStaffId > 0) {
                final String organisationalRoleParentStaffDisplayName = rs.getString("organisational_role_parent_staff_display_name");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaffId, organisationalRoleParentStaffDisplayName);
            }

            final LocalDateTime createdOnTime = JdbcSupport.getLocalDateTime(rs, "createdOn");
            final LocalDate createdOn = createdOnTime != null ? createdOnTime.toLocalDate() : null;

            final EmployerData employerDataResult = EmployerData.instance(id, externalId, mobileNo, contactPerson, emailAddress, emailExtension, name, slug, rcNumber, state, country,
                    classification, industry, lga, officeAddress, nearestLandMark, active, business, staffData, supervisorStaffData, createdOn);
            return employerDataResult;
        }

    }

}
