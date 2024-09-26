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
package org.apache.fineract.portfolio.business.kyc.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigApiConstants;
import org.apache.fineract.portfolio.business.kyc.data.KycConfigData;
import org.apache.fineract.portfolio.business.kyc.exception.KycConfigNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KycConfigReadServiceImpl implements KycConfigReadService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private static final Logger LOG = LoggerFactory.getLogger(KycConfigReadServiceImpl.class);

    private final KycConfigMapper kycConfigMapper = new KycConfigMapper();
    private final KycParamsMapper kycParamsMapper = new KycParamsMapper();

    private static final class KycConfigMapper implements RowMapper<KycConfigData> {

        private final String schema;

        KycConfigMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append(" mkc.id as id, mkc.description as description, cv.order_position as order_position, ");
            builder.append(" cv.is_mandatory as isMandatory,cv.is_active as isActive, ");
            builder.append(" cv.code_value as kycTierValue, mkc.kyc_tier_cv_id as kycTierCodeValueId ");
            builder.append(" from m_kyc_config mkc ");
            builder.append("left join m_code_value cv on cv.id = mkc.kyc_tier_cv_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public KycConfigData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String description = rs.getString("description");
            final Long kycTierCodeValueId = JdbcSupport.getLong(rs, "kycTierCodeValueId");
            final String kycTierValue = rs.getString("kycTierValue");
            final Integer orderPosition = rs.getInt("order_position");
            final boolean isMandatory = rs.getBoolean("isMandatory");
            final boolean isActive = rs.getBoolean("isActive");
            final CodeValueData kycTier = CodeValueData.instance(kycTierCodeValueId, kycTierValue, orderPosition, null, isActive,
                    isMandatory);

            final KycConfigData kycConfigData = KycConfigData.builder().id(id).kycTier(kycTier).description(description).build();
            return kycConfigData;
        }
    }

    private static final class KycParamsMapper implements RowMapper<CodeValueData> {

        private final String schema;

        KycParamsMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append(" mkcm.kyc_tier_param_cv_id as kycParamId, ");
            builder.append(" cv.code_value as kycParamValue");
            builder.append(" from m_kyc_config_mapping mkcm ");
            builder.append("left join m_code_value cv on cv.id = mkcm.kyc_tier_param_cv_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public CodeValueData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long kycParamId = JdbcSupport.getLong(rs, "kycParamId");
            final String kycParamValue = rs.getString("kycParamValue");
            final CodeValueData kycTierParam = CodeValueData.instance(kycParamId, kycParamValue);

            return kycTierParam;
        }
    }

    @Override
    public KycConfigData retrieveOne(final Long KycConfigId) {

        this.context.authenticatedUser();
        try {
            final String sql = "select " + kycConfigMapper.schema() + " where mkc.id = ? ";
            KycConfigData kycConfigData = this.jdbcTemplate.queryForObject(sql, kycConfigMapper, KycConfigId);

            final String KycParamSql = "select " + kycParamsMapper.schema() + " where mkcm.kyc_config_id = ? ";

            final Collection<CodeValueData> kycParams = this.jdbcTemplate.query(KycParamSql, kycParamsMapper, KycConfigId);
            kycConfigData.setKycParams(kycParams);

            return kycConfigData;
        } catch (EmptyResultDataAccessException e) {
            LOG.error("retrieveOne KYC Config not found {}", e);
            throw new KycConfigNotFoundException(KycConfigId);
        }
    }

    @Override
    public Page<KycConfigData> retrieveAll(final SearchParameters searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(kycConfigMapper.schema());

        if (searchParameters != null) {

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
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.kycConfigMapper);
    }

    @Override
    public KycConfigData retrieveTemplate() {
        this.context.authenticatedUser();
        final List<CodeValueData> kyclevelOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(KycConfigApiConstants.kycLevelParamName));
        final List<CodeValueData> kycParamOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(KycConfigApiConstants.kycParamName));

        return KycConfigData.builder().kycLevelOptions(kyclevelOptions).kycParamOptions(kycParamOptions).build();
    }

}
