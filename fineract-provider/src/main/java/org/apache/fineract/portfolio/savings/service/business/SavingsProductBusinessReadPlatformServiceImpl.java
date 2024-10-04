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
package org.apache.fineract.portfolio.savings.service.business;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.entityaccess.domain.FineractEntityType;
import org.apache.fineract.infrastructure.entityaccess.service.FineractEntityAccessUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class SavingsProductBusinessReadPlatformServiceImpl implements SavingsProductBusinessReadPlatformService{

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private final FineractEntityAccessUtil fineractEntityAccessUtil;
    private final SavingProductLookupMapper savingsProductLookupsRowMapper = new SavingProductLookupMapper();
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    @Override
    public Collection<SavingsProductData> retrieveAvailableSavingsProductsForMix() {
        this.context.authenticatedUser();

        final SavingsProductLookupMapper rm = new SavingsProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.productMixSchema();

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.SAVINGS_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " and sp.id in ( " + inClause + " ) ";
        }

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public Collection<SavingsProductData> retrieveRestrictedProductsForMix(Long productId) {
        this.context.authenticatedUser();

        final SavingsProductLookupMapper rm = new SavingsProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.restrictedProductsSchema() + " where pm.product_id=? ";
        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause1 = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.SAVINGS_PRODUCT);
        if (inClause1 != null && !inClause1.trim().isEmpty()) {
            sql += " and rp.id in ( " + inClause1 + " ) ";
        }

        sql += " UNION Select " + rm.derivedRestrictedProductsSchema() + " where pm.restricted_product_id=?";

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause2 = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.SAVINGS_PRODUCT);
        if (inClause2 != null && !inClause2.trim().isEmpty()) {
            sql += " and sp.id in ( " + inClause2 + " ) ";
        }

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId, productId }); // NOSONAR
    }

    @Override
    public Collection<SavingsProductData> retrieveAllowedProductsForMix(Long productId) {
        this.context.authenticatedUser();

        final SavingsProductLookupMapper rm = new SavingsProductLookupMapper(sqlGenerator);

        String sql = "Select " + rm.schema() + " where ";

        // Check if branch specific products are enabled. If yes, fetch only
        // products mapped to current user's office
        String inClause = fineractEntityAccessUtil
                .getSQLWhereClauseForProductIDsForUserOffice_ifGlobalConfigEnabled(FineractEntityType.SAVINGS_PRODUCT);
        if (inClause != null && !inClause.trim().isEmpty()) {
            sql += " sp.id in ( " + inClause + " ) and ";
        }

        sql += "sp.id not in (" + "Select pm.restricted_product_id from m_savings_product_mix pm where pm.product_id=? " + "UNION "
                + "Select pm.product_id from m_savings_product_mix pm where pm.restricted_product_id=?)";

        return this.jdbcTemplate.query(sql, rm, new Object[] { productId, productId }); // NOSONAR
    }

    private static final class SavingProductLookupMapper implements RowMapper<SavingsProductData> {

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return SavingsProductData.lookup(id, name);
        }
    }

    private static final class SavingsProductLookupMapper implements RowMapper<SavingsProductData> {

        private final DatabaseSpecificSQLGenerator sqlGenerator;

        SavingsProductLookupMapper(DatabaseSpecificSQLGenerator sqlGenerator) {
            this.sqlGenerator = sqlGenerator;
        }

        public String schema() {
            return "sp.id as id, sp.name as name from m_savings_product sp";
        }

//        public String activeOnlySchema() {
//            return schema() + " where (close_date is null or close_date >= " + sqlGenerator.currentBusinessDate() + ")";
//        }

        public String productMixSchema() {
            return "sp.id as id, sp.name as name FROM m_savings_product sp left join m_savings_product_mix pm on pm.product_id=sp.id where sp.id not IN("
                    + "select sp.id from m_savings_product sp inner join m_savings_product_mix pm on pm.product_id=sp.id)";
        }

        public String restrictedProductsSchema() {
            return "pm.restricted_product_id as id, rp.name as name from m_savings_product_mix pm join m_savings_product rp on rp.id = pm.restricted_product_id ";
        }

        public String derivedRestrictedProductsSchema() {
            return "pm.product_id as id, sp.name as name from m_savings_product_mix pm join m_savings_product sp on sp.id=pm.product_id";
        }

        @Override
        public SavingsProductData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");

            return SavingsProductData.lookup(id, name);
        }
    }
}
