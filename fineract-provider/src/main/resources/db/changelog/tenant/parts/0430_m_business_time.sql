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

CREATE TABLE `m_business_time` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `week_day_id` INT NOT NULL,
  `role_id` BIGINT  NOT NULL,
  `start_time` TIME DEFAULT NULL,
  `end_time` TIME DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_on_utc` DATETIME NULL,
  `last_modified_by` BIGINT NOT NULL,
  `last_modified_on_utc` DATETIME NULL,
  UNIQUE KEY `business_time_UNIQUE_role_weekday` (`role_id`,week_day_id),
  CONSTRAINT `business_time_FK_role_id` FOREIGN KEY (`role_id`) REFERENCES m_role(`id`),
  CONSTRAINT `business_time_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `business_time_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

