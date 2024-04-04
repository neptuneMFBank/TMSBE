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
CREATE OR REPLACE VIEW m_self_activate_client_view AS
SELECT
    mckc.client_id
FROM m_client_kyc_checkers mckc  
JOIN m_client mc on mc.id = mckc.client_id 
JOIN m_selfservice_user_client_mapping msucm on msucm.client_id = mckc.client_id
WHERE mc.status_enum=100 AND mckc.has_agreement=1 AND mckc.has_personal=1 AND mckc.has_residential=1
AND mckc.has_employment=1 AND mckc.has_next_of_kin=1 AND mckc.has_identification=1