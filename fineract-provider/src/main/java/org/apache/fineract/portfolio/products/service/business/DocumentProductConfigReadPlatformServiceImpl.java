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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.service.business.CodeDocumentReadPlatformService;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentConfigReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.products.data.business.DocumentProductConfigData;
import org.apache.fineract.portfolio.products.exception.business.DocumentProductConfigNotFoundException;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProductConfigReadPlatformServiceImpl implements DocumentProductConfigReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final CodeDocumentReadPlatformService codeReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    // data mappers
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final DocumentProductMapper documentProductMapper = new DocumentProductMapper();

    private final DocumentConfigReadPlatformService documentConfigReadPlatformService;

    private final ColumnValidator columnValidator;

    // private boolean is(final String commandParam, final String commandValue) {
    // return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    // }
    @Override
    public DocumentProductConfigData retrieveTemplate() {
        this.context.authenticatedUser();
        final Collection<DocumentConfigData> documentConfigDatas = this.documentConfigReadPlatformService
                .retrieveAllForProductConfigTemplate();
        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookup();
        final Collection<LoanProductData> loanProductDatas = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        return DocumentProductConfigData.template(loanProductDatas, documentConfigDatas, savingsProductDatas);
    }

    @Override
    public Page<DocumentProductConfigData> retrieveAll(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());

        if (searchParameters != null) {
            sqlBuilder.append(this.documentProductMapper.schema());
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
            return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(),
                    this.documentProductMapper);
        }
        return new Page<>(new ArrayList<>(), 0);
    }

    @Override
    public DocumentProductConfigData retrieveLoanProductDocument(final Long loanProductId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + this.documentProductMapper.schema() + " where mdcp.loan_product_id = ?";
            DocumentProductConfigData documentProductConfigData = this.jdbcTemplate.queryForObject(sql, this.documentProductMapper, // NOSONAR
                    loanProductId);
            if (documentProductConfigData != null) {
                DocumentConfigData documentConfig = documentProductConfigData.getConfigData();
                documentConfig = this.documentConfigReadPlatformService.retrieveOne(documentConfig.getId());
                documentProductConfigData = DocumentProductConfigData.instance(documentProductConfigData.getId(),
                        documentProductConfigData.getLoanProduct(), documentProductConfigData.getSavingsProduct(), documentConfig);
            }

            return documentProductConfigData;
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentProductConfigNotFoundException(loanProductId);
        }
    }

    @Override
    public DocumentProductConfigData retrieveSavingProductDocument(Long savingProductId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + this.documentProductMapper.schema() + " where mdcp.savings_product_id = ?";
            DocumentProductConfigData documentProductConfigData = this.jdbcTemplate.queryForObject(sql, this.documentProductMapper, // NOSONAR
                    savingProductId);
            if (documentProductConfigData != null) {
                DocumentConfigData documentConfig = documentProductConfigData.getConfigData();
                documentConfig = this.documentConfigReadPlatformService.retrieveOne(documentConfig.getId());
                documentProductConfigData = DocumentProductConfigData.instance(documentProductConfigData.getId(),
                        documentProductConfigData.getLoanProduct(), documentProductConfigData.getSavingsProduct(), documentConfig);
            }

            return documentProductConfigData;
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentProductConfigNotFoundException(savingProductId);
        }
    }

    private static final class DocumentProductMapper implements RowMapper<DocumentProductConfigData> {

        private final String schema;

        DocumentProductMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" mdcp.id, mdcp.m_document_client_config_id documentConfig, mdcp.name documentConfigName, "
                    + " mdcp.loan_product_id loanProductId, lp.name loanProductName, "
                    + " mdcp.savings_product_id savingsProductId, sp.name savingsProductName ");
            sqlBuilder.append(" from m_document_config_product mdcp "
                    + " join m_document_client_config_id mdc on mdc.id=mdcp.m_document_client_config_id "
                    + " left join loan_product_id lp on lp.id=mdcp.loan_product_id "
                    + " left join savings_product_id sp on sp.id=mdcp.savings_product_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public DocumentProductConfigData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLongDefaultToNullIfZero(rs, "id");

            final Long documentConfig = JdbcSupport.getLongDefaultToNullIfZero(rs, "documentConfig");
            final String documentConfigName = rs.getString("documentConfigName");
            final DocumentConfigData documentConfigData = DocumentConfigData.lookup(documentConfig, documentConfigName);

            final Long loanProductId = JdbcSupport.getLongDefaultToNullIfZero(rs, "loanProductId");
            final String loanProductName = rs.getString("loanProductName");
            final LoanProductData loanProductData = LoanProductData.lookup(loanProductId, loanProductName, null);

            final Long savingsProductId = JdbcSupport.getLongDefaultToNullIfZero(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");
            final SavingsProductData savingsProductData = SavingsProductData.lookup(savingsProductId, savingsProductName);

            return DocumentProductConfigData.instance(id, loanProductData, savingsProductData, documentConfigData);
        }
    }

    private String buildSqlStringFromClientCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long documentConfigId = searchParameters.getDocumentConfigId();

        String extraCriteria = "";

        // if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
        // final LocalDate startPeriod = searchParameters.getFromDate();
        // final LocalDate endPeriod = searchParameters.getToDate();
        // final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // if (startPeriod != null && endPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) BETWEEN ? AND ? ";
        // paramList.add(df.format(startPeriod));
        // paramList.add(df.format(endPeriod));
        // } else if (startPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) >= ? ";
        // paramList.add(df.format(startPeriod));
        // } else if (endPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) <= ? ";
        // paramList.add(df.format(endPeriod));
        // }
        // }
        if (searchParameters.isShowLoanProductsPassed()) {
            extraCriteria += " and mdcp.loan_product_id IS NOT NULL ";
        }
        if (searchParameters.isShowSavingsProductsPassed()) {
            extraCriteria += " and mdcp.savings_product_id IS NOT NULL ";
        }

        if (searchParameters.isDocumentConfigIdPassed()) {
            extraCriteria += " and mdcp.m_document_client_config_id = ? ";
            paramList.add(documentConfigId);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

}
