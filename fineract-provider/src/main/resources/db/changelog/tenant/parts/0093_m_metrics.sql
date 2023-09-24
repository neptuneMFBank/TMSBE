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

CREATE TABLE `m_metrics`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `assigned_user_id` BIGINT NOT NULL,
      `status_enum` SMALLINT NOT NULL DEFAULT 100,
      `rank` INT NOT NULL,
      `loan_id` BIGINT NULL,
      `savings_id` BIGINT NULL,
      `created_by` BIGINT NOT NULL,
      `created_on_utc` DATETIME NULL,
      `last_modified_by` BIGINT NOT NULL,
      `last_modified_on_utc` DATETIME NULL,
      UNIQUE KEY `metrics_UNIQUE_rank_loan` (`loan_id`,`rank`),
      UNIQUE KEY `metrics_UNIQUE_rank_saving` (`savings_id`,`rank`),
      CONSTRAINT `metrics_FK_loan` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`)
      CONSTRAINT `metrics_FK_saving` FOREIGN KEY (`savings_id`) REFERENCES `m_savings_account` (`id`)
      CONSTRAINT `metrics_FK_assigned_user` FOREIGN KEY (`assigned_user_id`) REFERENCES m_appuser(`id`),
      CONSTRAINT `rlpa_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES m_appuser(`id`),
      CONSTRAINT `rlpa_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES m_appuser(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
