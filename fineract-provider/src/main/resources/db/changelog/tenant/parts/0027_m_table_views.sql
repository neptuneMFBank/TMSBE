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
CREATE OR REPLACE VIEW m_client_view AS
SELECT
    mc.id,
    mc.account_no,
    mc.external_id,
    mc.display_name,
    mc.fullname,
    mc.is_staff,
    mc.staff_id,
    mc.default_savings_account,
    mc.mobile_no,
    mc.email_address,
    mc.legal_form_enum,
    mc.client_classification_cv_id,
    mc.submittedon_date,
    mc.created_by,
    mc.status_enum,
    mc.office_id
FROM m_client mc
ORDER BY mc.id DESC