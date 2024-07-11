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

CREATE TABLE `m_payment_type_grid` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `is_grid` bit(1) NOT NULL DEFAULT b'0',
  `is_commission` bit(1) NOT NULL DEFAULT b'0',
  `grid_json` longtext DEFAULT NULL COMMENT '[{minAmount,maxAmount,amount}]',
  `payment_type_id` int(11) NOT NULL,
  `calculation_type` smallint(6) DEFAULT NULL,
  `amount` decimal(19,6) DEFAULT NULL,
  `percent` decimal(19,6) DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_on_utc` DATETIME NULL,
  `last_modified_by` BIGINT NOT NULL,
  `last_modified_on_utc` DATETIME NULL,
  UNIQUE KEY `payment_type_grid_name_UNIQUE` (`name`),
  CONSTRAINT `payment_type_grid_FK_payment_type_id` FOREIGN KEY (`payment_type_id`) REFERENCES m_payment_type(`id`),
  CONSTRAINT `payment_type_grid_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `payment_type_grid_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
