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


CREATE TABLE `m_inventory` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `client_id` bigint DEFAULT NULL,
  `description` text DEFAULT NULL,
  `price` decimal(19,6) DEFAULT NULL,
  `discount_rate` decimal(19,6) DEFAULT NULL,
  `sku_code` varchar(200) DEFAULT NULL,
  `createdby_id` bigint NOT NULL,
  `created_date` datetime NOT NULL,
  `lastmodifiedby_id` bigint NOT NULL,
  `lastmodified_date` datetime NOT NULL,
  UNIQUE KEY `inventory_name_UNIQUE` (`name`),
  UNIQUE KEY `client_inventory_skucode_UNIQUE` (`client_id`,sku_code),
  CONSTRAINT `m_inventory_FK_client_id` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`),
  CONSTRAINT `m_inventory_FK_createdby_id` FOREIGN KEY (`createdby_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_inventory_dnd_FK_lastmodifiedby_id` FOREIGN KEY (`lastmodifiedby_id`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;