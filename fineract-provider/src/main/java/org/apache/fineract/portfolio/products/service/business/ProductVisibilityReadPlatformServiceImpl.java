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
package org.apache.fineract.portfolio.products.service.business;

import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.LOAN_PRODUCTS;
import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.SAVINGS_PRODUCTS;

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
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
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
import org.apache.fineract.portfolio.products.exception.business.ProductVisibilityNotFoundException;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductApprovalReadPlatformServiceImpl;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.business.LoanProductRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.products.api.business.ProductVisibilityApiResourceConstants;
import org.apache.fineract.portfolio.products.data.business.ProductVisibilityConfigData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.business.SavingsProductRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service

public class ProductVisibilityReadPlatformServiceImpl implements ProductVisibilityReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductApprovalReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ProductVisibiltyConfigMapper productVisibiltyConfigMapper = new ProductVisibiltyConfigMapper();
    private final ProductVisibiltyMapper productVisibiltyMapper = new ProductVisibiltyMapper();

    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final ClientRepositoryWrapper clientRepository;
    private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;
    private final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper;
    private final LoanProductsMapper loanProductsMapper;
    private final SavingsProductsMapper savingsProductsMapper;

    public ProductVisibilityReadPlatformServiceImpl(final LoanProductRepositoryWrapper loanProductRepositoryWrapper,
            final JdbcTemplate jdbcTemplate, final PlatformSecurityContext context, final PaginationHelper paginationHelper,
            final ColumnValidator columnValidator, final DatabaseSpecificSQLGenerator sqlGenerator,
            final LoanProductReadPlatformService loanProductReadPlatformService,
            final CodeValueReadPlatformService codeValueReadPlatformService, final ClientRepositoryWrapper clientRepository,
            final SavingsProductReadPlatformService savingsProductReadPlatformService,
            final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper) {

        this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;
        this.savingsProductRepositoryWrapper = savingsProductRepositoryWrapper;
        this.loanProductsMapper = new LoanProductsMapper(this.loanProductRepositoryWrapper);
        this.savingsProductsMapper = new SavingsProductsMapper(this.savingsProductRepositoryWrapper);
        this.jdbcTemplate = jdbcTemplate;
        this.context = context;
        this.paginationHelper = paginationHelper;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.loanProductReadPlatformService = loanProductReadPlatformService;
        this.codeValueReadPlatformService = codeValueReadPlatformService;
        this.clientRepository = clientRepository;
        this.savingsProductReadPlatformService = savingsProductReadPlatformService;

    }

    @Override
    public Page<ProductVisibilityConfigData> retrieveAll(final SearchParametersBusiness searchParameters, final Integer entityType) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(productVisibiltyConfigMapper.schema());
        sqlBuilder.append("  where mlpvc.product_type = ").append(entityType);

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromLoanProductVisibilityCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(extraCriteria);
            }
//            sqlBuilder.append(")");
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
                this.productVisibiltyConfigMapper);
    }

    private String buildSqlStringFromLoanProductVisibilityCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList
    // ,final Long entityName
    ) {

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
        // if (ObjectUtils.isNotEmpty(entityName)) {
        // extraCriteria += " and mlpvc.product_type = "+ entityName ;
        // }
        // if (StringUtils.isNotBlank(extraCriteria)) {
        // extraCriteria = extraCriteria.substring(4);
        // }
        return extraCriteria;
    }

    private static final class ProductVisibiltyConfigMapper implements RowMapper<ProductVisibilityConfigData> {

        private final String schema;

        ProductVisibiltyConfigMapper() {
            final StringBuilder builder = new StringBuilder(400);
            builder.append(" mlpvc.id as id, mlpvc.name as name, mlpvc.description as description ");
            builder.append(" from m_product_visibility_config mlpvc ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ProductVisibilityConfigData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");

            final ProductVisibilityConfigData loanProductVisibilityConfigData = ProductVisibilityConfigData.instance(id, name, description);
            return loanProductVisibilityConfigData;
        }
    }

    private static final class ProductVisibiltyMapper implements RowMapper<Long> {

        @Override
        public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            return id;
        }
    }

    @Override
    public ProductVisibilityConfigData retrieveOne(final Long productVisibilityId, final Integer entityType) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + productVisibiltyConfigMapper.schema() + " where mlpvc.id = ? and mlpvc.product_type = ? ";
            ProductVisibilityConfigData productVisibilityConfigData = this.jdbcTemplate.queryForObject(sql, productVisibiltyConfigMapper,
                    new Object[]{productVisibilityId, entityType});

            final String savingsProductIdSql = "select mlpvc.savingsproduct_id as id "
                    + " from m_savingsproduct_visibility_config_mapping mlpvc " + " where mlpvc.config_id = ? ";

            final Collection<Long> savingsProductIds = this.jdbcTemplate.query(savingsProductIdSql, this.productVisibiltyMapper,
                    new Object[]{productVisibilityId});

            final String loanProductIdSql = "select mlpvc.loanproduct_id as id " + " from m_loanproduct_visibility_config_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> loanProductIds = this.jdbcTemplate.query(loanProductIdSql, this.productVisibiltyMapper,
                    new Object[]{productVisibilityId});

            final String legalEnumsSql = "select mlpvc.legalenum_id as id " + " from m_product_visibility_legalenum_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> LegalEnumIds = this.jdbcTemplate.query(legalEnumsSql, this.productVisibiltyMapper,
                    new Object[]{productVisibilityId});

            final String clientClassificationSql = "select mlpvc.clientclassification_id as id "
                    + " from m_product_visibility_clientclassification_mapping mlpvc" + " where mlpvc.config_id = ? ";

            final Collection<Long> clientClassificationIds = this.jdbcTemplate.query(clientClassificationSql, this.productVisibiltyMapper,
                    new Object[]{productVisibilityId});

            final String clientTypeSql = "select mlpvc.clienttype_id as id" + " from m_product_visibility_clienttype_mapping mlpvc "
                    + " where mlpvc.config_id = ? ";

            final Collection<Long> clientTypeIds = this.jdbcTemplate.query(clientTypeSql, this.productVisibiltyMapper,
                    new Object[]{productVisibilityId});

            productVisibilityConfigData.setClientClassification(clientClassificationIds);
            productVisibilityConfigData.setClientType(clientTypeIds);
            productVisibilityConfigData.setLegalEnum(LegalEnumIds);
            if (Objects.equals(entityType, LOAN_PRODUCTS.getValue())) {
                productVisibilityConfigData.setLoanProduct(loanProductIds);
            } else if (Objects.equals(entityType, SAVINGS_PRODUCTS.getValue())) {
                productVisibilityConfigData.setSavingsProduct(savingsProductIds);
            }
            return productVisibilityConfigData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOne Loan Product Visibility not found: {}", e);
            throw new ProductVisibilityNotFoundException(GlobalEntityType.fromInt(entityType).getCode() + " visibility with " + productVisibilityId + " does not exist");
        }
    }

    @Override
    public ProductVisibilityConfigData retrieveTemplate(final Integer entityType) {
        this.context.authenticatedUser();

        Collection<LoanProductData> loanProductOptions = null;
        Collection<SavingsProductData> savingsProductOptions = null;

        if (Objects.equals(entityType, LOAN_PRODUCTS.getValue())) {
            loanProductOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        } else if (Objects.equals(entityType, SAVINGS_PRODUCTS.getValue())) {
            savingsProductOptions = this.savingsProductReadPlatformService.retrieveAllForLookup();
        }
        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductVisibilityApiResourceConstants.CLIENTTYPE));
        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ProductVisibilityApiResourceConstants.CLIENTCLASSIFICATION));
        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        return ProductVisibilityConfigData.template(loanProductOptions, clientTypeOptions, clientClassificationOptions,
                clientLegalFormOptions, savingsProductOptions);
    }

    @Override
    public JsonObject retrieveVisibileProductForClient(final Long clientId, final Integer entityType) {
        this.context.authenticatedUser();
        Client client = this.clientRepository.findOneWithNotFoundDetection(clientId);
        List<Object> paramList = new ArrayList<>(Arrays.asList());

        Long clientClassificationId = client.clientClassificationId();
        Long clientTypeId = client.clientTypeId();
        Integer legalFormId = client.getLegalForm();

        final StringBuilder sqlBuilder = new StringBuilder(200);

        sqlBuilder.append("select ");
        String productIdSql = "";
        if (Objects.equals(entityType, SAVINGS_PRODUCTS.getValue())) {
            productIdSql = " mlpvc.savingsproducts as savingsproducts " + " from product_visibility_config_view mlpvc ";
        } else if (Objects.equals(entityType, LOAN_PRODUCTS.getValue())) {
            productIdSql = " mlpvc.loanproducts as loanproducts " + " from product_visibility_config_view mlpvc ";
        }
        sqlBuilder.append(productIdSql);
        sqlBuilder.append(" where mlpvc.product_type = ").append(entityType);
        String extraCriteria = "";

        if (clientClassificationId != null) {
            paramList.add("%," + clientClassificationId + ",%");
            extraCriteria += " and mlpvc.client_classifications like ? ";
        } else {
            extraCriteria += " and mlpvc.client_classifications is null ";
        }

        if (clientTypeId != null) {
            paramList.add("%," + clientTypeId + ",%");
            extraCriteria += " and mlpvc.client_types  like ? ";
        } else {
            extraCriteria += " and mlpvc.client_types is null ";
        }

        if (legalFormId != null) {
            paramList.add("%," + legalFormId + ",%");
            extraCriteria += " and mlpvc.legal_enums like ? ";
        } else {
            extraCriteria += " and mlpvc.legal_enums is null ";
        }

//        if (StringUtils.isNotBlank(extraCriteria)) {
//            extraCriteria = extraCriteria.substring(4);
//            extraCriteria = " where " + extraCriteria;
//        }
        sqlBuilder.append(extraCriteria);
        JsonObject productIds = null;
        try {
            if (Objects.equals(entityType, SAVINGS_PRODUCTS.getValue())) {
                productIds = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), this.savingsProductsMapper, paramList.toArray());
            } else if (Objects.equals(entityType, LOAN_PRODUCTS.getValue())) {
                productIds = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), this.loanProductsMapper, paramList.toArray());

            }
            return productIds;
        } catch (final EmptyResultDataAccessException e) {
            throw new ProductVisibilityNotFoundException("no " + GlobalEntityType.fromInt(entityType).getCode() + "  available for this client");
        }
    }

    private static final class LoanProductsMapper implements RowMapper<JsonObject> {

        private final LoanProductRepositoryWrapper loanProductRepositoryWrapper;

        LoanProductsMapper(final LoanProductRepositoryWrapper loanProductRepositoryWrapper) {
            this.loanProductRepositoryWrapper = loanProductRepositoryWrapper;

        }

        @Override
        public JsonObject mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final JsonArray jsonArray = new JsonArray();

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
            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("loanproduct", jsonArray);

            return jsonObject;
        }
    }

    private static final class SavingsProductsMapper implements RowMapper<JsonObject> {

        private final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper;

        SavingsProductsMapper(final SavingsProductRepositoryWrapper savingsProductRepositoryWrapper) {
            this.savingsProductRepositoryWrapper = savingsProductRepositoryWrapper;

        }

        @Override
        public JsonObject mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final JsonArray jsonArray = new JsonArray();

            final String savingsproducts = rs.getString("savingsproducts");
            final String[] savingsProductArray = savingsproducts.split(",");
            if (savingsProductArray != null && savingsProductArray.length > 0) {
                for (String savingsProductId : savingsProductArray) {
                    SavingsProduct savingsProduct = this.savingsProductRepositoryWrapper
                            .findOneWithNotFoundDetection(Long.valueOf(savingsProductId));
                    JsonObject jsonElement = new JsonObject();

                    jsonElement.addProperty("id", savingsProductId);
                    jsonElement.addProperty("name", savingsProduct.getName());
                    jsonArray.add(jsonElement);
                }
            }
            final JsonObject jsonObject = new JsonObject();
            jsonObject.add("savingsproduct", jsonArray);

            return jsonObject;
        }
    }

}
