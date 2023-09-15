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

CREATE TABLE `m_employer`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `staff_id` BIGINT NULL,
      `client_classification_cv_id` INT NOT NULL,
      `external_id` VARCHAR(100) NULL,
      `name` VARCHAR(150) NOT NULL,
      `slug` VARCHAR(20) NULL,
      `mobile_no` VARCHAR(20) NULL,
      `email_address` VARCHAR(100) NULL,
      `email_extension` VARCHAR(20) NULL,
      `contact_person` VARCHAR(150) NULL,
      `nearest_land_mark` VARCHAR(225) NULL,
      `office_address` VARCHAR(225) NULL,
      `rc_number` VARCHAR(150) NULL,
      `industry_id` INT NULL,
      `business_id` BIGINT NULL,
      `lga_id` INT NULL,
      `state_id` INT NULL,
      `country_id` INT NULL,
      `active` tinyint(4) NOT NULL DEFAULT 0,
      `created_by` BIGINT NOT NULL,
      `created_on_utc` DATETIME NULL,
      `last_modified_by` BIGINT NOT NULL,
      `last_modified_on_utc` DATETIME NULL,
      CONSTRAINT `FK_m_employer_client_classification_cv_id` FOREIGN KEY (`client_classification_cv_id`) REFERENCES `m_code_value` (`id`),
      UNIQUE KEY `employer_external_id_UNIQUE` (`external_id`),
      UNIQUE KEY `employer_name_UNIQUE` (`name`),
      CONSTRAINT `FK_m_employer_industry_id` FOREIGN KEY (`industry_id`) REFERENCES `m_code_value` (`id`),
      CONSTRAINT `FK_m_employer_staff_id` FOREIGN KEY (`staff_id`) REFERENCES `m_staff` (`id`),
      CONSTRAINT `FK_m_employer_business_id` FOREIGN KEY (`business_id`) REFERENCES `m_client` (`id`),
      CONSTRAINT `FK_m_employer_lga_id` FOREIGN KEY (`lga_id`) REFERENCES `m_code_value` (`id`),
      CONSTRAINT `FK_m_employer_country_id` FOREIGN KEY (`country_id`) REFERENCES `m_code_value` (`id`),
      CONSTRAINT `FK_m_employer_state_id` FOREIGN KEY (`state_id`) REFERENCES `m_code_value` (`id`),
      CONSTRAINT `m_employer_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES m_appuser(`id`),
      CONSTRAINT `m_employer_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES m_appuser(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
