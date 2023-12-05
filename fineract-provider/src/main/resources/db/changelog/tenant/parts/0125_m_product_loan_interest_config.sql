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

CREATE TABLE `m_product_loan_interest_config`(
      `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
      `rlpi_id` BIGINT NOT NULL,
      `min_tenor` INT DEFAULT '0',
      `max_tenor` INT DEFAULT '0',
      `nominal_interest_rate_per_period` decimal(19,6) DEFAULT '0',
      CONSTRAINT `config_FK_rlpi_id` FOREIGN KEY (`rlpi_id`) REFERENCES `m_product_loan_interest` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
