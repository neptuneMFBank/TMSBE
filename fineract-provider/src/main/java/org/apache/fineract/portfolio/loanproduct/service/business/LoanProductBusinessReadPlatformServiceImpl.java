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
package org.apache.fineract.portfolio.loanproduct.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class LoanProductBusinessReadPlatformServiceImpl implements LoanProductBusinessReadPlatformService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final RateReadService rateReadService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;

    @Autowired
    public LoanProductBusinessReadPlatformServiceImpl(final PlatformSecurityContext context,
            final ChargeReadPlatformService chargeReadPlatformService, final JdbcTemplate jdbcTemplate,
            final FineractEntityAccessUtil fineractEntityAccessUtil, final RateReadService rateReadService,
            DatabaseSpecificSQLGenerator sqlGenerator) {
        this.context = context;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.jdbcTemplate = jdbcTemplate;
        this.fineractEntityAccessUtil = fineractEntityAccessUtil;
        this.rateReadService = rateReadService;
        this.sqlGenerator = sqlGenerator;
    }

    @Override
    public LoanProductData retrieveLoanProductData(final Long loanProductId) {

        try {
            final LoanProductLookupMapper rm = new LoanProductLookupMapper();
            final String sql = "select " + rm.schema() + " where lp.id = ?";

            return this.jdbcTemplate.queryForObject(sql, rm, new Object[] { loanProductId }); // NOSONAR

        } catch (final EmptyResultDataAccessException e) {
            throw new LoanProductNotFoundException(loanProductId, e);
        }
    }

    private static final class LoanProductLookupMapper implements RowMapper<LoanProductData> {

        public String schema() {
            return " lp.id as id, lp.name as name, lp.short_name as shortName, lp.description as description from m_product_loan lp ";
        }

        @Override
        public LoanProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String shortName = rs.getString("shortName");
            final String description = rs.getString("description");

            return LoanProductData.lookup(id, name, shortName, description);
        }
    }

}
