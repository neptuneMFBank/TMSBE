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
package org.apache.fineract.infrastructure.security.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthenticationBusinessReadPlatformServiceImpl implements AuthenticationBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AuthenticationBusinessReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public LocalDateTime lastLoginDate(final Long userId) {
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            final lastLoginDateMapper ptm = new lastLoginDateMapper();

            String lastLoginSql = "SELECT  mllv.last_login_date  as lastLoginDate" + " FROM fineract_default.m_users_details_view mllv "
                    + " where  user_id = " + userId;
            sqlBuilder.append(lastLoginSql);

            final LocalDateTime lastLoginDate = this.jdbcTemplate.queryForObject(sqlBuilder.toString(), ptm);
            return lastLoginDate;
        } catch (DataAccessException e) {
            log.warn("lastLoginDateException: {}", e);
            return null;
        }
    }

    private static final class lastLoginDateMapper implements RowMapper<LocalDateTime> {

        lastLoginDateMapper() {

        }

        @Override
        public LocalDateTime mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final LocalDateTime lastLoginDate = JdbcSupport.getLocalDateTime(rs, "lastLoginDate");
            return lastLoginDate;

        }

    }

}
