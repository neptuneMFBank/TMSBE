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

CREATE TABLE `m_document_client_config`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `active` tinyint(4) NOT NULL DEFAULT 0,
      `name` varchar(100) NOT NULL,
      `description` varchar(250) NULL,
      `legal_form_id` INT NULL,
      `created_by` BIGINT NOT NULL,
      `created_on_utc` DATETIME NULL,
      `last_modified_by` BIGINT NOT NULL,
      `last_modified_on_utc` DATETIME NULL,
      UNIQUE KEY `legal_form_id_UNIQUE_m_document_client_config` (`name`),
      UNIQUE KEY `name_UNIQUE_m_document_client_config` (`legal_form_id`),
      CONSTRAINT `m_document_client_config_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES m_appuser(`id`),
      CONSTRAINT `m_document_client_config_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES m_appuser(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
