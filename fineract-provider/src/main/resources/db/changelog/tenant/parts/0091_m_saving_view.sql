--
-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.
--

-- VIEW FOR SAVINGS
CREATE OR REPLACE VIEW m_saving_view AS
SELECT
    msa.id AS id,
    msa.external_id,
    msp.id product_id,
    msp.name product_name,
    mc.id AS client_id,
    mc.display_name,
    mc.office_id,
    mc.bvn,
    mc.client_type_cv_id,
    o.name office_name,
    msa.account_no,
    msa.activatedon_date,
    msa.submittedon_date,
    msa.deposit_type_enum,
    msa.status_enum AS status_enum,
    MAX(msat.transaction_date) last_transaction_date,
    COALESCE(msa.account_balance_derived, 0) AS ledger_balance,
    COALESCE(account_balance_derived,0)
    - COALESCE(msa.total_savings_amount_on_hold,0) AS available_balance,
    msa.min_required_balance
    FROM m_savings_account msa
JOIN m_client_view mc ON mc.id=msa.client_id
JOIN m_savings_product msp ON msp .id =msa.product_id
LEFT JOIN m_office o on o.id = mc.office_id
LEFT JOIN m_savings_account_transaction msat ON msat.savings_account_id =msa.id
GROUP BY msa.id;
