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

CREATE VIEW m_employer_view AS
SELECT me.id, me.name, me.mobile_no, me.email_address,
me.client_classification_cv_id, mcv.code_value client_classification_value,
me.industry_id, mcvv.code_value industry_value, me.active,
me.staff_id, ms.display_name staff_display_name,
mss.id organisational_role_parent_staff_id, mss.display_name organisational_role_parent_staff_display_name, me.created_on_utc
FROM m_employer me
LEFT JOIN m_code_value mcv ON mcv.id=me.client_classification_cv_id
LEFT JOIN m_code_value mcvv ON mcvv.id=me.industry_id
LEFT JOIN m_staff ms ON ms.id=me.staff_id
LEFT JOIN m_staff mss ON mss.id=ms.organisational_role_parent_staff_id;
