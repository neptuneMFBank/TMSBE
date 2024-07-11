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

-- make sure to replace accounting_rules NULL to the right accounting_rulesID e.g 1 else commision will not run

CREATE OR REPLACE VIEW m_commission_vend AS
    SELECT NULL accounting_rules, msat.id, msat.amount, msat.ref_no, mpd.receipt_number, mpd.bank_number,
    mptg.calculation_type, mptg.amount grid_amount, mptg.percent grid_percent, mn.note, mpd.payment_type_id, msa.currency_code
    FROM m_savings_account_transaction msat
    JOIN m_savings_account msa ON msa.id=msat.savings_account_id
    JOIN m_payment_detail mpd ON mpd.id=msat.payment_detail_id
    JOIN m_payment_type_grid mptg ON mptg.payment_type_id=mpd.payment_type_id AND mptg.is_commission=1
    LEFT JOIN m_note mn ON mn.savings_account_transaction_id=msat.id
    WHERE mpd.bank_number>0
    AND msat.id NOT IN (SELECT mcvc.savings_account_transaction_id FROM m_commision_vat_calculated mcvc)
    GROUP BY msat.id;
