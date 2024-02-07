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
    pcs.id, pcs.action_name, pcs.entity_name, pcs.office_id, pcs.office_name, pcs.api_get_url, pcs.resource_id, pcs.maker_id,
    pcs.made_on_date, pcs.checker_id, pcs.checked_on_date, pcs.processing_result_enum,
    pcs.group_id, pcs.client_id, pcs.loan_id, pcs.savings_account_id
FROM m_portfolio_command_source pcs