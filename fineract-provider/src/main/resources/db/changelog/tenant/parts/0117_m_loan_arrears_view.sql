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

CREATE OR REPLACE VIEW m_loan_arrears_view AS
SELECT ml.id, ml.is_topup, ml.account_no, ml.product_id, mpl.name product_name, ml.loan_officer_id, ms.display_name loan_officer_name, ml.number_of_repayments,
ml.term_frequency, ml.term_period_frequency_enum, ml.total_outstanding_derived, ml.total_repayment_derived, ml.principal_amount, ml.total_recovered_derived,
mlaa.principal_overdue_derived, mlaa.interest_overdue_derived, mlaa.fee_charges_overdue_derived, ml.expected_maturedon_date,
mlaa.penalty_charges_overdue_derived, mlaa.total_overdue_derived, mlaa.overdue_since_date_derived, ml.total_expected_repayment_derived,
mc.id client_id, mc.display_name client_display_name, mc.mobile_no, mc.email_address, slk.bvn, slk.nin, slk.alternateMobileNumber, ml.disbursedon_date,
mg.id group_id, mg.display_name group_display_name, ml.nominal_interest_rate_per_period, ml.annual_nominal_interest_rate, ml.currency_code, ml.submittedon_date
FROM m_loan_arrears_aging mlaa
JOIN m_loan ml ON ml.id=mlaa.loan_id
JOIN m_product_loan mpl ON mpl.id=ml.product_id
LEFT JOIN m_staff ms ON ms.id=ml.loan_officer_id 
LEFT JOIN m_client mc ON mc.id=ml.client_id  
LEFT JOIN m_group mg ON mg.id=ml.group_id 
LEFT JOIN secondLevelKYC slk ON slk.client_id=mc.id;