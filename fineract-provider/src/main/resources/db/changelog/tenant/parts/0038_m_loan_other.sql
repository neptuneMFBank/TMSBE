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

CREATE TABLE `m_loan_other`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `loan_id` BIGINT NOT NULL,
      `activation_channel_id` INT NOT NULL,
      CONSTRAINT `m_loan_other_FK_loan_id` FOREIGN KEY (`loan_id`) REFERENCES `m_loan` (`id`),
      CONSTRAINT `m_loan_other_FK_activation_channel_id` FOREIGN KEY (`activation_channel_id`) REFERENCES `m_code_value` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
