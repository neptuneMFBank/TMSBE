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
package org.apache.fineract.portfolio.business.merchant.registration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MerchantRegistrationReadPlatformServiceImp implements MerchantRegistrationReadPlatformService {
        private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MerchantRegistrationReadPlatformServiceImp(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
      @Override
    public boolean isClientExist(String accountNumber ,String email, String mobileNumber,
            boolean isEmailAuthenticationMode) {
        String sql = "select count(*) from m_client where account_no = ? and email_address = ?";
        Object[] params = new Object[] { accountNumber, email };
        if (!isEmailAuthenticationMode) {
            sql = sql + " and mobile_no = ?";
            params = new Object[] { accountNumber, email, mobileNumber };
        }
        Integer count = this.jdbcTemplate.queryForObject(sql, Integer.class, params);
        if (count == 0) {
            return false;
        }
        return true;
    }
}
