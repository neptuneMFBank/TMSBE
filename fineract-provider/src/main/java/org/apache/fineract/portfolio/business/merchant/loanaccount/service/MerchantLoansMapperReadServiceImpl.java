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
package org.apache.fineract.portfolio.business.merchant.loanaccount.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class MerchantLoansMapperReadServiceImpl implements MerchantLoansMapperReadService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MerchantLoansMapperReadServiceImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Boolean isLoanMappedToMerchant(Long loanId, Long appUserId) {
        return this.jdbcTemplate.queryForObject(
                "select case when (count(*) > 0) then true else false end " + " from m_appuser_merchant_mapping as m "
                        + " left join m_loan as l on l.client_id = m.client_id " + " where l.id = ? and m.appuser_id = ? ",
                Boolean.class, loanId, appUserId);
    }

}
