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

CREATE TABLE `m_appuser_merchant_mapping` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `appuser_id` bigint NOT NULL,
  `client_id` bigint NOT NULL,
  UNIQUE KEY `appuser_id_client_id` (`appuser_id`,`client_id`),
  UNIQUE KEY `unique_self_client` (`client_id`),
  CONSTRAINT `m_merchant_appuser_id` FOREIGN KEY (`appuser_id`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `m_merchant_client_id` FOREIGN KEY (`client_id`) REFERENCES `m_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;