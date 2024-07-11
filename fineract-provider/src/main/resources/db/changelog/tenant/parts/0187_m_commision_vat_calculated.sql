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


CREATE TABLE `m_commision_vat_calculated` (
  `id` bigint PRIMARY KEY AUTO_INCREMENT,
  `savings_account_transaction_id` BIGINT NOT NULL,
  `type` INT(11) NOT NULL  COMMENT 'commission-1,vat-2',
  `status` INT(11) NOT NULL  COMMENT '600-done,500-failed',
  `created_date` datetime DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `m_cvc_FK_savings_account_transaction_id` FOREIGN KEY (`savings_account_transaction_id`) REFERENCES `m_savings_account_transaction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
