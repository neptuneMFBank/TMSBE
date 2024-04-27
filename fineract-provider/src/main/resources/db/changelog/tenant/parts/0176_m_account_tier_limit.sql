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

CREATE TABLE `m_account_tier_limit` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `parent_id` BIGINT DEFAULT NULL,
  `client_type_cv_id` INT NOT NULL,
  `activation_channel_id` INT DEFAULT NULL,
  `daily_withdrawal_limit` DECIMAL(19,6) DEFAULT NULL,
  `single_deposit_limit` DECIMAL(19,6) DEFAULT NULL,
  `cumulative_balance` DECIMAL(19,6) DEFAULT NULL,
  `description` TEXT DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_on_utc` DATETIME NULL,
  `last_modified_by` BIGINT NOT NULL,
  `last_modified_on_utc` DATETIME NULL,
  UNIQUE KEY `tier_UNIQUE_parent_channel` (`parent_id`,`activation_channel_id`),
  CONSTRAINT `tier_FK_parent_id` FOREIGN KEY (`parent_id`) REFERENCES m_code_value(`id`),
  CONSTRAINT `tier_FK_type_id` FOREIGN KEY (`client_type_cv_id`) REFERENCES m_account_tier_limit(`id`),
  CONSTRAINT `tier_FK_channel_id` FOREIGN KEY (`activation_channel_id`) REFERENCES m_code_value(`id`),
  CONSTRAINT `tier_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `tier_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;