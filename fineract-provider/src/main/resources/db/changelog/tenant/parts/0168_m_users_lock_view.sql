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

CREATE OR REPLACE VIEW m_users_lock_view AS
    SELECT ma.id FROM m_appuser ma
    LEFT JOIN m_users_details_view mudv ON mudv.user_id = ma.id
    WHERE
    ma.staff_id IS NOT NULL AND ma.nonlocked=1 AND
    (ma.firsttime_login_remaining=1 || (DATEDIFF(CURRENT_DATE(), DATE(mudv.last_login_date)) > 20));
