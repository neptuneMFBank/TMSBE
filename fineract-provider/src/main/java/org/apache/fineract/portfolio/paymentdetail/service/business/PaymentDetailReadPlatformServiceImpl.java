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
package org.apache.fineract.portfolio.paymentdetail.service.business;

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentDetailReadPlatformServiceImpl implements PaymentDetailReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public PaymentDetailReadPlatformServiceImpl(final FromJsonHelper fromJsonHelper, final PlatformSecurityContext context,
            final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.fromJsonHelper = fromJsonHelper;
    }

    @Override
    public JsonObject isReceiptNumberExisting(final String receiptNumber) {
        this.context.authenticatedUser();
        Integer cnt = this.jdbcTemplate.queryForObject("SELECT count(*) FROM m_payment_detail WHERE receipt_number=? ", Integer.class,
                receiptNumber);
        Boolean receiptNumberExists = cnt != null && cnt > 0;
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("receiptNumber", receiptNumberExists);
        return jsonObject;

    }

}
