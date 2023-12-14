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
package org.apache.fineract.notification.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.notification.data.NotificationData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationBusinessReadPlatformServiceImpl implements NotificationBusinessReadPlatformService {

    private final NotificationDataRow notificationDataRow = new NotificationDataRow();

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final ColumnValidator columnValidator;
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    private String buildSqlStringFromNotificationCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        String type = searchParameters.getName();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(nm.created_at AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(nm.created_at AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(nm.created_at AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isNamePassed()) {
            type = StringUtils.lowerCase(type);
            extraCriteria += " and LOWER(ng.object_type) = ? ";
            paramList.add(type);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public Page<NotificationData> getAllUnreadNotifications(final SearchParametersBusiness searchParameters) {
        final Long appUserId = context.authenticatedUser().getId();
        String sql = "SELECT " + sqlGenerator.calcFoundRows() + " ng.id as id, nm.user_id as userId, ng.object_type as objectType, "
                + "ng.object_identifier as objectId, ng.actor as actor, ng." + sqlGenerator.escape("action")
                + " as action, ng.notification_content "
                + "as content, ng.is_system_generated as isSystemGenerated, nm.is_read isRead, nm.created_at as createdAt "
                + "FROM notification_mapper nm INNER JOIN notification_generator ng ON nm.notification_id = ng.id " //+ "WHERE nm.user_id = ? AND nm.is_read = false order by nm.created_at desc"
                ;

        return getNotificationDataPage(searchParameters, appUserId, sql);
    }

    private Page<NotificationData> getNotificationDataPage(SearchParametersBusiness searchParameters, Long appUserId, String sql) {
        List<Object> paramList = new ArrayList<>();
        paramList.add(appUserId);
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append(sql);
        sqlBuilder.append(" WHERE nm.user_id = ? AND nm.is_read = false ");
        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromNotificationCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(extraCriteria);
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

        //Object[] params = new Object[]{appUserId};
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.notificationDataRow);
    }

    private static final class NotificationDataRow implements RowMapper<NotificationData> {

        @Override
        public NotificationData mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationData notificationData = new NotificationData();

            final Long id = rs.getLong("id");
            notificationData.setId(id);

            final String objectType = rs.getString("objectType");
            notificationData.setObjectType(objectType);

            final Long objectId = rs.getLong("objectId");
            notificationData.setObjectId(objectId);

            final Long actorId = rs.getLong("actor");
            notificationData.setActorId(actorId);

            final String action = rs.getString("action");
            notificationData.setAction(action);

            final String content = rs.getString("content");
            notificationData.setContent(content);

            final boolean isSystemGenerated = rs.getBoolean("isSystemGenerated");
            notificationData.setSystemGenerated(isSystemGenerated);

            final boolean isRead = rs.getBoolean("isRead");
            notificationData.setRead(isRead);

            final String createdAt = rs.getString("createdAt");
            notificationData.setCreatedAt(createdAt);

            return notificationData;
        }
    }
}
