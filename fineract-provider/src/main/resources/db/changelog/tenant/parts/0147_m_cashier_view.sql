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

CREATE OR REPLACE VIEW m_cashier_view AS
SELECT mc.id, mc.staff_id, mc.start_date, mc.end_date, mc.teller_id, mt.name teller_name,
mt.state teller_state, mt.office_id, mo.name office_name, ms.organisational_role_parent_staff_id
FROM m_cashiers mc
JOIN m_tellers mt ON mt.id = mc.teller_id
JOIN m_office mo ON mo.id = mt.office_id
LEFT JOIN m_staff ms ON ms.id = mc.staff_id
GROUP BY mc.staff_id, mt.id;
