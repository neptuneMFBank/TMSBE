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

-- Action That Can Be Performed Create/Approve/Reject -> where Approve performs the actual transaction
--  column from_account_id should having only one status(pending) per customer

CREATE TABLE `m_transfer_approval` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `amount` DECIMAL(19,6) NOT NULL,
  `status` INT DEFAULT 100,
  `transfer_type` INT NOT NULL COMMENT 'intraBank=1, interBank=2',
  `from_account_id` BIGINT NOT NULL,
  `from_account_type` INT NOT NULL,
  `from_account_number` VARCHAR(255) DEFAULT NULL,
  `to_account_id` BIGINT DEFAULT NULL,
  `to_account_type` INT DEFAULT NULL,
  `to_account_number` VARCHAR(255) DEFAULT NULL,
  `to_bank_id` INT DEFAULT NULL,
  `activation_channel_id` INT DEFAULT NULL,
  `reason` VARCHAR(255) DEFAULT NULL,
  `created_by` BIGINT NOT NULL,
  `created_on_utc` DATETIME NULL,
  `last_modified_by` BIGINT NOT NULL,
  `last_modified_on_utc` DATETIME NULL,
  CONSTRAINT `ta_FK_bank_id` FOREIGN KEY (`to_bank_id`) REFERENCES m_code_value(`id`),
  CONSTRAINT `ta_FK_channel_id` FOREIGN KEY (`activation_channel_id`) REFERENCES m_code_value(`id`),
  CONSTRAINT `ta_FK_created_by` FOREIGN KEY (`created_by`) REFERENCES `m_appuser` (`id`),
  CONSTRAINT `ta_FK_last_modified_by` FOREIGN KEY (`last_modified_by`) REFERENCES `m_appuser` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;