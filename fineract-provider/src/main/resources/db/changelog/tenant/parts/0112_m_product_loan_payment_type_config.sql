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

CREATE TABLE `m_product_loan_payment_type_config`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `active` tinyint(4) NOT NULL DEFAULT 0,
      `name` varchar(100) NOT NULL,
      `description` varchar(250) NULL,
      `product_id` BIGINT NOT NULL,
      `created_by` BIGINT NOT NULL,
      `created_on_utc` DATETIME NULL,
      `last_modified_by` BIGINT NOT NULL,
      `last_modified_on_utc` DATETIME NULL,
      UNIQUE KEY `name_UNIQUE_product_loan_payment_type_config` (`name`),
      CONSTRAINT `product_loan_payment_type_config_FK_product_id` FOREIGN KEY (`product_id`) REFERENCES m_product_loan(`id`),
      CONSTRAINT `product_loan_payment_type_config_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES m_appuser(`id`),
      CONSTRAINT `product_loan_payment_type_config_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES m_appuser(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
