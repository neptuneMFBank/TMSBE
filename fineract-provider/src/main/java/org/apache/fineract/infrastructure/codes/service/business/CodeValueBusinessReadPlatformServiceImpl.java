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
package org.apache.fineract.infrastructure.codes.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.fineract.infrastructure.codes.data.business.CodeValueBusinessData;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CodeValueBusinessReadPlatformServiceImpl implements CodeValueBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public CodeValueBusinessReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class CodeValueBusinessDataMapper implements RowMapper<CodeValueBusinessData> {

        public String schema() {
            return " cv.id as id, cv.code_value as value, cv.code_id as codeId, cv.code_description as description, cv.order_position as position,"
                    + " cv.is_active isActive, cv.is_mandatory as mandatory, cv.code_score codeScore from m_code_value as cv join m_code c on cv.code_id = c.id ";
        }

        @Override
        public CodeValueBusinessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final Long codeScore = rs.getLong("codeScore");
            final String value = rs.getString("value");
            final Integer position = rs.getInt("position");
            final String description = rs.getString("description");
            final boolean isActive = rs.getBoolean("isActive");
            final boolean mandatory = rs.getBoolean("mandatory");

            return CodeValueBusinessData.instance(id, value, position, description, isActive, mandatory, codeScore);
        }
    }

    @Override
    public Collection<CodeValueBusinessData> retrieveCodeValuesByCode(final String code) {

        this.context.authenticatedUser();

        final CodeValueBusinessDataMapper rm = new CodeValueBusinessDataMapper();
        final String sql = "select " + rm.schema() + "where c.code_name like ? and cv.is_active = true order by position";

        return this.jdbcTemplate.query(sql, rm, new Object[] { code }); // NOSONAR
    }

}
