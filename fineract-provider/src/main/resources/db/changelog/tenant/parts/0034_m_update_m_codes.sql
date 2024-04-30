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


-- INSERT m_code and relating to m_code_value


INSERT INTO `m_code_value`(`code_id`,`code_value`,`order_position`)
select mc.id, 'Core System', ifnull(max(mv.id), 1)
from m_code mc
left join m_code_value mv on mv.code_id = mc.id
where mc.`code_name` = "ActivationChannel"
