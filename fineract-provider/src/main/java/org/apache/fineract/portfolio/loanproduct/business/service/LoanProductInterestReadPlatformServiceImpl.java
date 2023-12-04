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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductInterestConfigData;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductInterestData;
import org.apache.fineract.portfolio.loanproduct.business.exception.LoanProductInterestNotFoundException;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
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
public class LoanProductInterestReadPlatformServiceImpl implements LoanProductInterestReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductInterestReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanProductInterestMapper loanProductInterestMapper = new LoanProductInterestMapper();
    private final LoanProductInterestConfigMapper loanProductInterestConfigMapper = new LoanProductInterestConfigMapper();
    private final LoanProductReadPlatformService loanProductReadPlatformService;

    @Override
    public LoanProductInterestData retrieveTemplate() {
        this.context.authenticatedUser();

        final Collection<LoanProductData> loanProductOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();

        return LoanProductInterestData.template(loanProductOptions);
    }

    @Override
    public Page<LoanProductInterestData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(loanProductInterestMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromLoanProductInterestCriteria(searchParameters, paramList);
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
                this.loanProductInterestMapper);
    }

    private String buildSqlStringFromLoanProductInterestCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long productId = searchParameters.getProductId();
        final String name = searchParameters.getName();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(lpi.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(lpi.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(lpi.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isProductIdPassed()) {
            extraCriteria += " and lpi.loan_product_id = ? ";
            paramList.add(productId);
        }

        if (searchParameters.isNamePassed()) {
            paramList.add("%".concat(name.concat("%")));
            extraCriteria += " and lpi.name like ? ";
        }
        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public LoanProductInterestData retrieveOne(Long loanProductApprovalId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductInterestMapper.schema() + " where lpi.id = ?";
            LoanProductInterestData loanProductInterestData = this.jdbcTemplate.queryForObject(sql, loanProductInterestMapper,
                    new Object[]{loanProductApprovalId});
            Collection<LoanProductInterestConfigData> retrieveConfig = retrieveConfig(loanProductApprovalId);
            if (!CollectionUtils.isEmpty(retrieveConfig)) {
                loanProductInterestData = LoanProductInterestData.lookUpFinal(retrieveConfig, loanProductInterestData);
            }
            return loanProductInterestData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOne Loan Product Interest not found: {}", e);
            throw new LoanProductInterestNotFoundException(loanProductApprovalId);
        }
    }

    @Override
    public LoanProductInterestData retrieveOneViaLoanProduct(Long loanProductId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductInterestMapper.schema() + " where lpi.loan_product_id = ?";
            LoanProductInterestData loanProductInterestData = this.jdbcTemplate.queryForObject(sql, loanProductInterestMapper,
                    new Object[]{loanProductId});
            if (loanProductInterestData != null) {
                Collection<LoanProductInterestConfigData> retrieveConfig = retrieveConfig(loanProductInterestData.getId());
                if (!CollectionUtils.isEmpty(retrieveConfig)) {
                    loanProductInterestData = LoanProductInterestData.lookUpFinal(retrieveConfig, loanProductInterestData);
                }
            }
            return loanProductInterestData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOneViaLoanProduct Interest not found: {}", e);
            throw new LoanProductInterestNotFoundException("Loan Product with id " + loanProductId + " does not exist for interest.");
        }
    }

    @Override
    public Collection<LoanProductInterestConfigData> retrieveConfig(Long loanProductInterestId) {
        this.context.authenticatedUser();
        final String sql = "select " + loanProductInterestConfigMapper.schema() + " WHERE lpic.rlpi_id = ? ORDER BY lpic.rank ASC ";
        return this.jdbcTemplate.query(sql, loanProductInterestConfigMapper, new Object[]{loanProductInterestId}); // NOSONAR
    }

    private static final class LoanProductInterestMapper implements RowMapper<LoanProductInterestData> {

        public String schema() {
            return " lpi.id, lpi.name, lpi.description, lpi.active, lpi.loan_product_id loanProductId," + " mpl.name loanProductName "
                    + " from m_product_loan_interest lpi " + " JOIN m_product_loan mpl ON mpl.id=lpi.loan_product_id ";
        }

        @Override
        public LoanProductInterestData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Long loanProductId = rs.getLong("loanProductId");
            final String loanProductName = rs.getString("loanProductName");
            final Boolean active = rs.getBoolean("active");
            final LoanProductData loanProductData = LoanProductData.lookup(loanProductId, loanProductName, null);
            return LoanProductInterestData.lookUp(id, name, loanProductData, description, active);
        }

    }

    private static final class LoanProductInterestConfigMapper implements RowMapper<LoanProductInterestConfigData> {

        public String schema() {
            return " lpic.id, lpic.min_tenor minTenor, lpic.max_tenor maxTenor, lpic.nominal_interest_rate_per_period interestRatePerPeriod "
                    + " from m_product_loan_interest_config lpic " + " JOIN m_role mr ON mr.id=lpic.role_id ";
        }

        @Override
        public LoanProductInterestConfigData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final BigDecimal minTenor = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minTenor");
            final BigDecimal maxTenor = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxTenor");
            final BigDecimal interestRatePerPeriod = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "interestRatePerPeriod");
            return LoanProductInterestConfigData.instance(id, minTenor, maxTenor, interestRatePerPeriod);
        }

    }

}
