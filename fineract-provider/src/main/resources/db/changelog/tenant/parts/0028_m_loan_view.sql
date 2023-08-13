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

-- VIEW FOR LOANS
CREATE OR REPLACE VIEW m_loan_view AS
SELECT
    ml.id,
    ml.external_id,
    ml.account_no,
    ml.client_id,
    ml.group_id,
    ml.glim_id,
    ml.product_id,
    ml.loan_officer_id,
    ml.loanpurpose_cv_id,
    ml.loan_status_id,
    ml.loan_type_enum,
    ml.principal_amount_proposed,
    ml.principal_amount, 
    ml.approved_principal,
    ml.net_disbursal_amount ,
    ml.nominal_interest_rate_per_period,
    ml.annual_nominal_interest_rate,
    ml.term_frequency,
    ml.number_of_repayments,
    lp.can_use_for_topup,
    ml.is_topup,
    ml.is_npa,
    ml.submittedon_date,
    ml.created_by,
    ml.approvedon_userid,
    ml.approvedon_date,
    ml.disbursedon_userid,
    ml.disbursedon_date,
    ml.rejectedon_userid,
    ml.withdrawnon_userid,
    ml.rejectedon_date,
    ml.closedon_userid,
    la.total_overdue_derived,
    la.overdue_since_date_derived,
    ml.currency_code
FROM m_loan ml
join m_product_loan lp on lp.id = ml.product_id
left join m_loan_arrears_aging la on la.loan_id = ml.id
ORDER BY ml.id DESC 