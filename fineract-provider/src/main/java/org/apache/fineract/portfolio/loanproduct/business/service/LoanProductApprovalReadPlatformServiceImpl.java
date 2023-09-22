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
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalConfigData;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalData;
import org.apache.fineract.portfolio.loanproduct.business.exception.LoanProductApprovalNotFoundException;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.service.RoleReadPlatformService;
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
public class LoanProductApprovalReadPlatformServiceImpl implements LoanProductApprovalReadPlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(LoanProductApprovalReadPlatformServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final LoanProductApprovalMapper loanProductApprovalMapper = new LoanProductApprovalMapper();
    private final LoanProductApprovalConfigMapper loanProductApprovalConfigMapper = new LoanProductApprovalConfigMapper();
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final RoleReadPlatformService roleReadPlatformService;

    @Override
    public LoanProductApprovalData retrieveTemplate() {
        this.context.authenticatedUser();
        final Collection<RoleData> roleOptions = this.roleReadPlatformService.retrieveAllActiveRoles();

        final Collection<LoanProductData> loanProductOptions = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup();

        return LoanProductApprovalData.template(loanProductOptions, roleOptions);
    }

    @Override
    public Page<LoanProductApprovalData> retrieveAll(final SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(loanProductApprovalMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromLoanProductApprovalCriteria(searchParameters, paramList);
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

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.loanProductApprovalMapper);
    }

    private String buildSqlStringFromLoanProductApprovalCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

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
            extraCriteria += " and lpa.loan_product_id = ? ";
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
    public LoanProductApprovalData retrieveOne(Long loanProductApprovalId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductApprovalMapper.schema() + " where lpa.id = ?";
            LoanProductApprovalData loanProductApprovalData = this.jdbcTemplate.queryForObject(sql, loanProductApprovalMapper, new Object[]{loanProductApprovalId});
            Collection<LoanProductApprovalConfigData> retrieveConfig
                    = retrieveConfig(loanProductApprovalId);
            if (!CollectionUtils.isEmpty(retrieveConfig)) {
                loanProductApprovalData = LoanProductApprovalData.lookUpFinal(retrieveConfig, loanProductApprovalData);
            }
            return loanProductApprovalData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOne Loan Product Approval not found: {}", e);
            throw new LoanProductApprovalNotFoundException(loanProductApprovalId);
        }
    }

    @Override
    public LoanProductApprovalData retrieveOneViaLoanProduct(Long loanProductId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + loanProductApprovalMapper.schema() + " where lpa.loan_product_id = ?";
            LoanProductApprovalData loanProductApprovalData = this.jdbcTemplate.queryForObject(sql, loanProductApprovalMapper, new Object[]{loanProductId});
            if (loanProductApprovalData != null) {
                Collection<LoanProductApprovalConfigData> retrieveConfig
                        = retrieveConfig(loanProductApprovalData.getId());
                if (!CollectionUtils.isEmpty(retrieveConfig)) {
                    loanProductApprovalData = LoanProductApprovalData.lookUpFinal(retrieveConfig, loanProductApprovalData);
                }
            }
            return loanProductApprovalData;
        } catch (DataAccessException e) {
            LOG.error("retrieveOneViaLoanProduct Approval not found: {}", e);
            throw new LoanProductApprovalNotFoundException("Loan Product with id " + loanProductId + " does not exist for approval.");
        }
    }

    @Override
    public Collection<LoanProductApprovalConfigData> retrieveConfig(Long loanProductApprovalId) {
        this.context.authenticatedUser();
        final String sql = "select " + loanProductApprovalConfigMapper.schema();
        return this.jdbcTemplate.query(sql, loanProductApprovalConfigMapper, new Object[]{loanProductApprovalId}); // NOSONAR
    }

    private static final class LoanProductApprovalMapper implements RowMapper<LoanProductApprovalData> {

        public String schema() {
            return " lpa.id, lpa.name, lpa.loan_product_id loanProductId,"
                    + " mpl.name loanProductName "
                    + " from m_role_loan_product_approval lpa "
                    + " JOIN m_product_loan mpl ON mpl.id=lpa.loan_product_id ";
        }

        @Override
        public LoanProductApprovalData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final Long loanProductId = rs.getLong("loanProductId");
            final String loanProductName = rs.getString("loanProductName");
            final LoanProductData loanProductData = LoanProductData.lookup(loanProductId, loanProductName, null);
            return LoanProductApprovalData.lookUp(id, name, loanProductData);
        }

    }

    private static final class LoanProductApprovalConfigMapper implements RowMapper<LoanProductApprovalConfigData> {

        public String schema() {
            return " lpac.id, lpac.role_id roleId, mr.name as roleName, lpac.max_approval_amount maxApprovalAmount, lpac.rank "
                    + " from m_role_loan_product_approval_config lpac "
                    + " JOIN m_role mr ON mr.id=lpac.role_id ";
        }

        @Override
        public LoanProductApprovalConfigData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long roleId = rs.getLong("roleId");
            final String roleName = rs.getString("roleName");
            final RoleData roleData = new RoleData(roleId, roleName, null, null);
            final BigDecimal maxApprovalAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxApprovalAmount");
            final Integer rank = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "rank");
            return LoanProductApprovalConfigData.instance(id, roleData, maxApprovalAmount, rank);
        }

    }

}
