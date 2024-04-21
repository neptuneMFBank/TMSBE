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

CREATE TABLE `m_savingsproduct_visibility_config_mapping` (
 `id` bigint NOT NULL AUTO_INCREMENT,
  `config_id` bigint NOT NULL,
  `savingsproduct_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_id_savingsproduct_id` (`config_id`,`savingsproduct_id`),
  CONSTRAINT `m_savingsproduct_visibility_config_id` FOREIGN KEY (`config_id`) REFERENCES `m_product_visibility_config` (`id`) ,
  CONSTRAINT `m_product_savings_id` FOREIGN KEY (`savingsproduct_id`) REFERENCES `m_savings_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
