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

-- VIEW FOR CLIENT
CREATE OR REPLACE VIEW m_savings_amount_on_hold_view AS
SELECT msat.id, msat.savings_account_id, msat.amount, msa.account_no, msa.product_id, msp.name product_name, msa.client_id,
mc.display_name, mc.office_id, mo.name office_name, mo.hierarchy,
mc.mobile_no, mc.email_address, slk.bvn, slk.nin, slk.tin, slk.alternateMobileNumber, 
msat.appuser_id, CONCAT_WS(' ',ma.firstname,' ',ma.lastname) originator, msat.created_date 
from m_savings_account_transaction msat 
JOIN m_savings_account msa ON msa.id=msat.savings_account_id 
JOIN m_savings_product msp ON msp.id=msa.product_id
JOIN m_appuser ma ON ma.id=msat.appuser_id
JOIN m_client mc ON mc.id=msa.client_id 
JOIN m_office mo ON mo.id=mc.id
LEFT JOIN secondLevelKYC slk ON slk.client_id=mc.id 
WHERE msat.is_reversed=FALSE AND msat.transaction_type_enum=20 AND msat.reason_for_block LIKE '%InterBank transfer%'
ORDER BY msat.id DESC