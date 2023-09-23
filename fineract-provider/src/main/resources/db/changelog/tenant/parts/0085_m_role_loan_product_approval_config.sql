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

CREATE TABLE `m_role_loan_product_approval_config`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `rlpa_id` BIGINT NOT NULL,
      `role_id` BIGINT NOT NULL,
      `max_approval_amount` decimal(19,6) DEFAULT NULL,
      `rank` INT NOT NULL,
      UNIQUE KEY `rlpa_UNIQUE_rank` (`rlpa_id`,`rank`),
      CONSTRAINT `config_FK_rlpa` FOREIGN KEY (`rlpa_id`) REFERENCES `m_role_loan_product_approval` (`id`),
      CONSTRAINT `config_FK_rlpa_role` FOREIGN KEY (`role_id`) REFERENCES `m_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
