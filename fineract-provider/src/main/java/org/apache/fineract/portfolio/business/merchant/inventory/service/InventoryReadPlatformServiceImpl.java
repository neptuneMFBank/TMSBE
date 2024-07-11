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
package org.apache.fineract.portfolio.business.merchant.inventory.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.documentmanagement.data.DocumentData;
import org.apache.fineract.infrastructure.documentmanagement.service.DocumentReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.business.merchant.inventory.data.InventoryData;
import org.apache.fineract.portfolio.business.merchant.inventory.exception.InventoryNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class InventoryReadPlatformServiceImpl implements InventoryReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final InventoryMapper inventoryMapper;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ColumnValidator columnValidator;
    private final DocumentReadPlatformService documentReadPlatformService;
    private final PlatformSecurityContext context;

    @Autowired
    public InventoryReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate, final RoutingDataSource dataSource,
            final PaginationHelper paginationHelper, final DatabaseSpecificSQLGenerator sqlGenerator, final ColumnValidator columnValidator,
            final DocumentReadPlatformService documentReadPlatformService, final PlatformSecurityContext context) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.inventoryMapper = new InventoryMapper();
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
        this.columnValidator = columnValidator;
        this.documentReadPlatformService = documentReadPlatformService;
        this.context = context;
    }

    @Override
    public InventoryData retrieveOne(Long inventoryId) {
        try {
            final String sql = "select " + this.inventoryMapper.schema + " where mi.id = ? ";
            return this.jdbcTemplate.queryForObject(sql, this.inventoryMapper, new Object[] { inventoryId });
        } catch (final EmptyResultDataAccessException e) {
            throw new InventoryNotFound(inventoryId, e);
        }
    }

    @Override
    public InventoryData retrieveOneByLink(String link) {
        try {
            final String sql = "select " + this.inventoryMapper.schema + " where mi.link = ? ";
            InventoryData inventoryData = this.jdbcTemplate.queryForObject(sql, this.inventoryMapper, new Object[] { link });
            if (inventoryData != null) {
                this.context.authenticatedUser().validateHasReadPermission("DOCUMENT");
                final Collection<DocumentData> documentDatas = this.documentReadPlatformService.retrieveAllDocuments("inventory",
                        inventoryData.getId());
                inventoryData.setDocumentDatas(documentDatas);
            }

            return inventoryData;
        } catch (final EmptyResultDataAccessException e) {
            throw new InventoryNotFound(link, e);
        }
    }

    @Override
    public Page<InventoryData> retrieveAll(final SearchParametersBusiness searchParameters) {

        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.inventoryMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
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
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.inventoryMapper);
    }

    private String buildSqlStringFromClientCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        final String name = searchParameters.getName();
        final Long clientId = searchParameters.getClientId();

        String extraCriteria = "";

        if (clientId != null) {
            extraCriteria += " and mi.client_id = ? ";
            paramList.add(clientId);
        }

        if (name != null) {
            final String nameFinal = StringUtils.lowerCase(name);
            paramList.add("%" + nameFinal + "%");
            extraCriteria += " and LOWER(mi.name ) like ? ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class InventoryMapper implements RowMapper<InventoryData> {

        final String schema;

        private InventoryMapper() {
            final StringBuilder sql = new StringBuilder(400);
            sql.append("mi.id as id, ");
            sql.append("mi.name as name, ");
            sql.append("mi.description as description, ");
            sql.append("mi.price as price, ");
            sql.append("mi.discount_rate as discountRate, ");
            sql.append("mi.client_id as clientId, ");
            sql.append("mi.sku_code as skuCode, ");
            sql.append("mi.link as link, ");
            sql.append("mi.createdby_id as createdbyId, ");
            sql.append("mi.created_date as createdDate, ");
            sql.append("mi.lastmodifiedby_id as lastmodifiedbyId, ");
            sql.append("mi.lastmodified_date as lastmodifiedDate ");
            sql.append("from m_inventory mi ");

            this.schema = sql.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public InventoryData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLong(rs, "id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final String skuCode = rs.getString("skuCode");
            final String link = rs.getString("link");

            final BigDecimal price = rs.getBigDecimal("price");
            final BigDecimal discountRate = rs.getBigDecimal("discountRate");

            return InventoryData.instance(id, name, description, skuCode, price, discountRate, link);
        }
    }

}
