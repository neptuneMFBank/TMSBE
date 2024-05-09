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
package org.apache.fineract.portfolio.paymenttype.service.business;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.data.business.PaymentTypeGridData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class PaymentTypeGridReadPlatformServiceImpl implements PaymentTypeGridReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public PaymentTypeGridReadPlatformServiceImpl(final FromJsonHelper fromJsonHelper, final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.fromJsonHelper = fromJsonHelper;
    }

    @Override
    public Collection<PaymentTypeGridData> retrievePaymentTypeGrids(Long paymentTypeId) {
        // TODO Auto-generated method stub
        this.context.authenticatedUser();
        try {
            final PaymentTypeGridMapper ptm = new PaymentTypeGridMapper();
            final String sql = "select " + ptm.schema() + " where pt.payment_type_id = ? ";
            final Collection<PaymentTypeGridData> paymentTypeGridData = this.jdbcTemplate.query(sql, ptm, new Object[]{paymentTypeId}); // NOSONAR
            if (!CollectionUtils.isEmpty(paymentTypeGridData)) {
                paymentTypeGridData.forEach(obj -> {
                    if (StringUtils.isNotBlank(obj.getGridJson())) {
                        final String gridJson = obj.getGridJson();
                        JsonElement gridJsonElement = fromJsonHelper.parse(gridJson);
                        obj.setGridJsonObject(gridJsonElement);
                        obj.setGridJson(null);
                    }
                });
            }
            return paymentTypeGridData;
        } catch (DataAccessException e) {
            return null;
        }
    }

    @Override
    public Collection<PaymentTypeGridData> retrievePaymentTypeGridsViaCharge(Long chargeId) {
        this.context.authenticatedUser();
        try {
            final PaymentTypeGridMapper ptm = new PaymentTypeGridMapper();
            final String sql = "select " + ptm.schema() + " where pt.charge_id = ? ";
            final Collection<PaymentTypeGridData> paymentTypeGridData = this.jdbcTemplate.query(sql, ptm, new Object[]{chargeId}); // NOSONAR
            if (!CollectionUtils.isEmpty(paymentTypeGridData)) {
                paymentTypeGridData.forEach(obj -> {
                    if (StringUtils.isNotBlank(obj.getGridJson())) {
                        final String gridJson = obj.getGridJson();
                        JsonElement gridJsonElement = fromJsonHelper.parse(gridJson);
                        obj.setGridJsonObject(gridJsonElement);
                        obj.setGridJson(null);
                    }
                });
            }
            return paymentTypeGridData;
        } catch (DataAccessException e) {
            return null;
        }
    }

    private static final class PaymentTypeGridMapper implements RowMapper<PaymentTypeGridData> {

        public String schema() {
            return " pt.id, pt.name, pt.is_commission as isCommission, pt.is_grid as isGrid, pt.grid_json as gridJson, pt.calculation_type as calculationType, pt.amount, pt.percent, pt.payment_type_id as paymentTypeId, mpt.value as paymentTypeName, "
                    + " pt.charge_id as chargeId, mc.name as chargeName "
                    + " from m_payment_type_grid pt left join m_payment_type mpt on mpt.id = pt.payment_type_id left join m_charge mc on mc.id = pt.charge_id ";
        }

        @Override
        public PaymentTypeGridData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String gridJson = rs.getString("gridJson");
            final boolean isGrid = rs.getBoolean("isGrid");
            final boolean isCommission = rs.getBoolean("isCommission");
            final BigDecimal amount = rs.getBigDecimal("amount");
            final BigDecimal percent = rs.getBigDecimal("percent");

            PaymentTypeData paymentType = null;
            final Long paymentTypeId = JdbcSupport.getLong(rs, "paymentTypeId");
            if (paymentTypeId != null) {
                final String typeName = rs.getString("paymentTypeName");
                paymentType = PaymentTypeData.instance(paymentTypeId, typeName);
            }

            ChargeData chargeData = null;
            final Long chargeId = JdbcSupport.getLong(rs, "chargeId");
            if (chargeId != null) {
                final String chargeName = rs.getString("chargeName");
                chargeData = ChargeData.lookup(chargeId, chargeName, false);
            }
            final int calculationType = rs.getInt("calculationType");
            EnumOptionData paymentCalculationType = null;
            if (calculationType > 0) {
                paymentCalculationType = PaymentTypeEnumerations.paymentCalculationType(calculationType);
            }

            return PaymentTypeGridData.instance(id, paymentType, name, gridJson, isGrid, isCommission, paymentCalculationType, amount, percent, chargeData);
        }

    }

}
