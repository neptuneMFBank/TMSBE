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

ALTER TABLE m_role_loan_product_approval
ADD COLUMN savings_product_id BIGINT NULL,
ADD CONSTRAINT rlpa_FK_savings_product
FOREIGN KEY (savings_product_id)
REFERENCES m_savings_product(id),
ADD UNIQUE KEY `rlpa_UNIQUE_savings_product` (`savings_product_id`);
