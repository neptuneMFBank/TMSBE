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

CREATE VIEW m_client_pending_activation AS
SELECT mc.id, mc.account_no, mc.display_name client_display_name, mc.legal_form_enum, mc.submittedon_date, 
mo.id office_id, mo.name office_name, ms.id staff_id, ms.display_name staff_display_name,
mss.id organisational_role_parent_staff_id, mss.display_name organisational_role_parent_staff_display_name,
slk.bvn, slk.iAgree 
FROM m_client mc 
LEFT JOIN secondLevelKYC slk ON slk.client_id =mc.id 
LEFT JOIN m_staff ms ON ms.id=mc.staff_id
LEFT JOIN m_staff mss ON mss.id=ms.organisational_role_parent_staff_id 
LEFT JOIN m_office mo ON mo.id=mc.office_id 
WHERE mc.status_enum = 100;