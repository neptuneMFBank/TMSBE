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

CREATE TABLE `m_address_other`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `address_id` BIGINT NOT NULL,
      `residence_status_id` INT NULL,
      `date_moved_in` DATE NULL,
      CONSTRAINT `m_address_other_FK_address_id` FOREIGN KEY (`address_id`) REFERENCES `m_address` (`id`),
      CONSTRAINT `m_address_other_FK_residence_status_id` FOREIGN KEY (`residence_status_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;