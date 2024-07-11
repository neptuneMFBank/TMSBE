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

ALTER TABLE m_account_tier_limit
    ADD  `name` VARCHAR(150) NOT NULL,
    ADD UNIQUE KEY `m_account_tier_limit_name` (`name`),
    DROP INDEX tier_UNIQUE_parent_channel,
    ADD   UNIQUE KEY `tier_UNIQUE_parent_channel_clienttype` (`parent_id`,`activation_channel_id`,`client_type_cv_id`)
