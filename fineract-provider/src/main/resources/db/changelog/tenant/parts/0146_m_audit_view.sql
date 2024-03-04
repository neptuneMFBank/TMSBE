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

CREATE OR REPLACE VIEW m_audit_view AS
SELECT
    pcs.id, pcs.action_name, pcs.entity_name, pcs.office_id, o.name office_name, pcs.api_get_url, pcs.resource_id, pcs.maker_id,
    pcs.made_on_date, pcs.checker_id, pcs.checked_on_date, pcs.processing_result_enum,
    pcs.group_id, pcs.client_id, pcs.loan_id, pcs.savings_account_id, mk.username as maker,
    ck.username as checker, ev.enum_message_property as processingResult,
    o.name as officeName, gl.level_name as groupLevelName, g.display_name as groupName, c.display_name as clientName,
    l.account_no as loanAccountNo, s.account_no as savingsAccountNo
FROM m_portfolio_command_source pcs
LEFT JOIN m_office o ON o.id=pcs.office_id
LEFT JOIN m_appuser mk on mk.id = pcs.maker_id
LEFT JOIN m_appuser ck on ck.id = pcs.checker_id
LEFT JOIN m_group g on g.id = pcs.group_id
LEFT JOIN m_group_level gl on gl.id = g.level_id
LEFT JOIN m_client c on c.id = pcs.client_id
LEFT JOIN m_loan l on l.id = pcs.loan_id
LEFT JOIN m_savings_account s on s.id = pcs.savings_account_id
LEFT JOIN r_enum_value ev on ev.enum_name = 'processing_result_enum' and ev.enum_id = pcs.processing_result_enum;
