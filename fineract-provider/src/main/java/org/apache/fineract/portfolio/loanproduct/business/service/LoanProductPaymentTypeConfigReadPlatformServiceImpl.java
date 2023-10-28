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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductPaymentTypeConfigData;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.exception.business.LoanProductPaymentTypeConfigNotFoundException;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanProductPaymentTypeConfigReadPlatformServiceImpl implements LoanProductPaymentTypeConfigReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductPaymentTypeConfigReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanProductPaymentTypeMapper loanProductPaymentTypeMapper = new LoanProductPaymentTypeMapper();
    private final LoanProductPaymentTypeConfigMapper loanProductPaymentTypeConfigMapper = new LoanProductPaymentTypeConfigMapper();
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Override
    public LoanProductPaymentTypeConfigData retrieveTemplate() {
        this.context.authenticatedUser();
        //also remove Self Service User Role from List
        final Collection<PaymentTypeData> paymentTypeOptions
                = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        final Collection<LoanProductData> loanProductOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();

        return LoanProductPaymentTypeConfigData.template(loanProductOptions, paymentTypeOptions);
    }

    @Override
    public Page<LoanProductPaymentTypeConfigData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(loanProductPaymentTypeMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromLoanProductPaymentTypeCriteria(searchParameters, paramList);
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
                this.loanProductPaymentTypeMapper);
    }

    private String buildSqlStringFromLoanProductPaymentTypeCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long productId = searchParameters.getProductId();
        final String name = searchParameters.getName();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(lpa.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(lpa.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(lpa.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isProductIdPassed()) {
            extraCriteria += " and lpa.product_id = ? ";
            paramList.add(productId);
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%".concat(name.concat("%")));
            extraCriteria += " and lpa.name like ? ";
        }
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public LoanProductPaymentTypeConfigData retrieveOne(Long LoanProductPaymentTypeId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductPaymentTypeMapper.schema() + " where lpa.id = ?";
            LoanProductPaymentTypeConfigData loanProductPaymentTypeData = this.jdbcTemplate.queryForObject(sql, loanProductPaymentTypeMapper,
                    new Object[]{LoanProductPaymentTypeId});
            if (ObjectUtils.isNotEmpty(loanProductPaymentTypeData)) {
                fetchPaymentTypeConfig(loanProductPaymentTypeData);
            }
            return loanProductPaymentTypeData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOne Loan Product Payment not found: {}", e);
            throw new LoanProductPaymentTypeConfigNotFoundException(LoanProductPaymentTypeId);
        }
    }

    @Override
    public LoanProductPaymentTypeConfigData retrieveOneViaLoanProduct(Long loanProductId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductPaymentTypeMapper.schema() + " where lpa.product_id = ?";
            LoanProductPaymentTypeConfigData loanProductPaymentTypeData = this.jdbcTemplate.queryForObject(sql, loanProductPaymentTypeMapper,
                    new Object[]{loanProductId});
            if (ObjectUtils.isNotEmpty(loanProductPaymentTypeData)) {
                fetchPaymentTypeConfig(loanProductPaymentTypeData);
            }
            return loanProductPaymentTypeData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOneViaLoanProduct Payment not found: {}", e);
            return null;
            //throw new LoanProductPaymentTypeConfigNotFoundException("Loan Product with id " + loanProductId + " does not exist for payment.");
        }
    }

    protected void fetchPaymentTypeConfig(LoanProductPaymentTypeConfigData loanProductPaymentTypeData) {
        Collection<PaymentTypeData> retrievePaymentTypeConfig = retrievePaymentTypeConfig(loanProductPaymentTypeData.getId());
        if (!CollectionUtils.isEmpty(retrievePaymentTypeConfig)) {
            loanProductPaymentTypeData.setPaymentTypes(retrievePaymentTypeConfig);
        }
    }

    @Override
    public Collection<PaymentTypeData> retrievePaymentTypeConfig(Long LoanProductPaymentTypeId) {
        this.context.authenticatedUser();
        final String sql = "select " + loanProductPaymentTypeConfigMapper.schema() + " WHERE lpac.m_product_loan_payment_type_config_id = ? ";
        return this.jdbcTemplate.query(sql, loanProductPaymentTypeConfigMapper, new Object[]{LoanProductPaymentTypeId}); // NOSONAR
    }

    private static final class LoanProductPaymentTypeMapper implements RowMapper<LoanProductPaymentTypeConfigData> {

        public String schema() {
            return " lpa.id, lpa.name, lpa.active, lpa.description, lpa.product_id loanProductId, mpl.name loanProductName "
                    + " from m_product_loan_payment_type_config lpa " + " JOIN m_product_loan mpl ON mpl.id=lpa.product_id ";
        }

        @Override
        public LoanProductPaymentTypeConfigData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Boolean active = rs.getBoolean("active");
            final Long loanProductId = rs.getLong("loanProductId");
            final String loanProductName = rs.getString("loanProductName");
            final LoanProductData loanProductData = LoanProductData.lookup(loanProductId, loanProductName, null);
            return LoanProductPaymentTypeConfigData.lookUp(id, name, description, loanProductData, active);
        }

    }

    private static final class LoanProductPaymentTypeConfigMapper implements RowMapper<PaymentTypeData> {

        public String schema() {
            return " lpac.payment_type_id paymentTypeId, mp.value paymentTypeName "
                    + " FROM m_product_loan_payment_type_config_code lpac "
                    + " JOIN m_payment_type mp ON mp.id=lpac.payment_type_id ";
        }

        @Override
        public PaymentTypeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long paymentTypeId = rs.getLong("paymentTypeId");
            final String paymentTypeName = rs.getString("paymentTypeName");
            return PaymentTypeData.instance(paymentTypeId, paymentTypeName);
        }

    }

}
