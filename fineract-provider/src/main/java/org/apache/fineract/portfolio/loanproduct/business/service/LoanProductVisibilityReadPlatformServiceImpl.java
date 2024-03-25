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
package org.apache.fineract.portfolio.loanproduct.business.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanproduct.business.api.LoanProductVisibilityApiResourceConstants;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductVisibilityConfigData;
import org.apache.fineract.portfolio.loanproduct.business.exception.LoanProductVisibilityNotFoundException;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service

public class LoanProductVisibilityReadPlatformServiceImpl implements LoanProductVisibilityReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductApprovalReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanProductVisibiltyConfigMapper loanProductVisibiltyConfigMapper = new LoanProductVisibiltyConfigMapper();
    private final LoanProductVisibiltyMapper loanProductVisibiltyMapper = new LoanProductVisibiltyMapper();

    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final ClientRepositoryWrapper clientRepository;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final LoanProductsMapper loanProductsMapper;

    public LoanProductVisibilityReadPlatformServiceImpl(final LoanProductRepositoryWrapper loanProductRepositoryWrapper,
            final JdbcTemplate jdbcTemplate, final PlatformSecurityContext context,
            final PaginationHelper paginationHelper,
            final ColumnValidator columnValidator,
            final DatabaseSpecificSQLGenerator sqlGenerator, final LoanProductReadPlatformService loanProductReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final ClientRepositoryWrapper clientRepository
    ) {

        this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;
        this.loanProductsMapper = new LoanProductsMapper(this.loanProductRepositoryWrapper);
        this.jdbcTemplate = jdbcTemplate;
        this.context = context;
        this.paginationHelper = paginationHelper;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.clientRepository = clientRepository;

    }

    @Override
    public Page<LoanProductVisibilityConfigData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(loanProductVisibiltyConfigMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromLoanProductVisibilityCriteria(searchParameters, paramList);
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
                this.loanProductVisibiltyConfigMapper);
    }

    private String buildSqlStringFromLoanProductVisibilityCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final String name = searchParameters.getName();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(mlpvc.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(mlpvc.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(mlpvc.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%".concat(name.concat("%")));
            extraCriteria += " and mlpvc.name like ? ";
        }
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class LoanProductVisibiltyConfigMapper implements RowMapper<LoanProductVisibilityConfigData> {

        private final String schema;

        LoanProductVisibiltyConfigMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append(" mlpvc.id as id, mlpvc.name as name, mlpvc.description as description ");
            builder.append(" from m_loanproduct_visibility_config mlpvc ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public LoanProductVisibilityConfigData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");

            final LoanProductVisibilityConfigData loanProductVisibilityConfigData = LoanProductVisibilityConfigData.instance(id, name, description);
            return loanProductVisibilityConfigData;
        }
    }

    private static final class LoanProductVisibiltyMapper implements RowMapper<Long> {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            return id;
        }
    }

    @Override
    public LoanProductVisibilityConfigData retrieveOne(Long loanProductVisibilityId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductVisibiltyConfigMapper.schema() + " where mlpvc.id = ?";
            LoanProductVisibilityConfigData loanProductVisibilityConfigData = this.jdbcTemplate.queryForObject(sql, loanProductVisibiltyConfigMapper,
                    new Object[]{loanProductVisibilityId});

            final String loanProductIdSql = "select mlpvc.loanproduct_id as id "
                    + " from m_loanproduct_visibility_config_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> loanProductIds = this.jdbcTemplate.query(loanProductIdSql, this.loanProductVisibiltyMapper,
                    new Object[]{loanProductVisibilityId});

            final String legalEnumsSql = "select mlpvc.legalenum_id as id "
                    + " from m_loanproduct_visibility_legalenum_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> LegalEnumIds = this.jdbcTemplate.query(legalEnumsSql, this.loanProductVisibiltyMapper,
                    new Object[]{loanProductVisibilityId});

            final String clientClassificationSql = "select mlpvc.clientclassification_id as id "
                    + " from m_loanproduct_visibility_clientclassification_mapping mlpvc"
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> clientClassificationIds = this.jdbcTemplate.query(clientClassificationSql, this.loanProductVisibiltyMapper,
                    new Object[]{loanProductVisibilityId});

            final String clientTypeSql = "select mlpvc.clienttype_id as id"
                    + " from m_loanproduct_visibility_clienttype_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> clientTypeIds = this.jdbcTemplate.query(clientTypeSql, this.loanProductVisibiltyMapper,
                    new Object[]{loanProductVisibilityId});

            loanProductVisibilityConfigData.setClientClassification(clientClassificationIds);
            loanProductVisibilityConfigData.setClientType(clientTypeIds);
            loanProductVisibilityConfigData.setLegalEnum(LegalEnumIds);
            loanProductVisibilityConfigData.setLoanProduct(loanProductIds);
            return loanProductVisibilityConfigData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOne Loan Product Visibility not found: {}", e);
            throw new LoanProductVisibilityNotFoundException(loanProductVisibilityId);
        }
    }

    @Override
    public LoanProductVisibilityConfigData retrieveTemplate() {
        this.context.authenticatedUser();

        final Collection<LoanProductData> loanProductOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanProductVisibilityApiResourceConstants.CLIENTTYPE));
        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(LoanProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION));
        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        return LoanProductVisibilityConfigData.template(loanProductOptions, clientTypeOptions, clientClassificationOptions, clientLegalFormOptions);
    }

    @Override
    public Collection<JsonArray> retrieveVisibileLoanProductForClient(final Long clientId) {
        Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        List<Object> paramList = new ArrayList<>(Arrays.asList());

        Long clientClassificationId = client.clientClassificationId();
        Long clientTypeId = client.clientTypeId();
        Integer legalFormId = client.getLegalForm();
        final StringBuilder sqlBuilder = new StringBuilder(200);

        String loanProductIdSql = "select mlpvc.loanproducts as loanproducts "
                + " from loan_product_visibility_config_view mlpvc ";
        sqlBuilder.append(loanProductIdSql);

        String extraCriteria = "";

        if (clientClassificationId != null) {
            paramList.add("%," + clientClassificationId + ",%");
            extraCriteria += " where mlpvc.client_classifications collate utf8mb4_unicode_ci like ? ";
        } else {
            extraCriteria += " where mlpvc.client_classifications is null ";
        }

        if (clientTypeId != null) {
            paramList.add("%," + clientTypeId + ",%");

            extraCriteria += " and mlpvc.client_types  collate utf8mb4_unicode_ci like ? ";
        } else {
            extraCriteria += " and mlpvc.client_types is null ";
        }
        if (legalFormId != null) {
            paramList.add("%," + legalFormId + ",%");

            extraCriteria += " and mlpvc.legal_enums collate utf8mb4_unicode_ci like ? ";
        } else {
            extraCriteria += " and mlpvc.legal_enums is null ";
        }

        sqlBuilder.append(extraCriteria);
        final Collection<JsonArray> loanProductIds = this.jdbcTemplate.query(sqlBuilder.toString(), this.loanProductsMapper, paramList.toArray());
        return loanProductIds;
    }

    private static final class LoanProductsMapper implements RowMapper<JsonArray> {

        private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;

        LoanProductsMapper(final LoanProductRepositoryWrapper loanProductRepositoryWrapper) {
            this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;

        }

        @Override
        public JsonArray mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            JsonArray jsonArray = new JsonArray();

            final String loanproducts = rs.getString("loanproducts");
            final String[] loanProductArray = loanproducts.split(",");
            if (loanProductArray != null && loanProductArray.length > 0) {
                for (String loanProductId : loanProductArray) {
                    LoanProduct loanProduct = this.loanProductRepositoryWrapper.findOneWithNotFoundDetection(Long.valueOf(loanProductId));
                    JsonObject jsonElement = new JsonObject();

                    jsonElement.addProperty("id", loanProductId);
                    jsonElement.addProperty("name", loanProduct.getName());
                    jsonArray.add(jsonElement);
                }
            }

            return jsonArray;
        }
    }
}
