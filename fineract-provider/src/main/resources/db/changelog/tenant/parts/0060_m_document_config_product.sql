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

CREATE TABLE `m_document_config_product`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `m_document_client_config_id` BIGINT NOT NULL,
      `loan_product_id` BIGINT NULL,
      `savings_product_id` BIGINT NULL,
      `created_by` BIGINT NOT NULL,
      `created_on_utc` DATETIME NULL,
      `last_modified_by` BIGINT NOT NULL,
      `last_modified_on_utc` DATETIME NULL,
      CONSTRAINT `m_document_config_product_FK_config_id` FOREIGN KEY (`m_document_client_config_id`) REFERENCES m_document_client_config(`id`),
      UNIQUE KEY `loan_product_UNIQUE_document_config` (`loan_product_id`),
      CONSTRAINT `m_document_config_FK_loan_product` FOREIGN KEY (`loan_product_id`) REFERENCES m_product_loan(`id`),
      UNIQUE KEY `savings_product_UNIQUE_document_config` (`savings_product_id`),
      CONSTRAINT `m_document_config_FK_savings_product` FOREIGN KEY (`savings_product_id`) REFERENCES m_savings_product(`id`),
      CONSTRAINT `m_document_config_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES m_appuser(`id`),
      CONSTRAINT `m_document_config_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES m_appuser(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
