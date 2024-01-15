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


-- INSERT m_permission
CREATE OR REPLACE VIEW m_loan_pending_disbursement_view AS
SELECT A.*
FROM m_loan_view A
JOIN m_metrics B ON A.id = B.loan_id 
WHERE 
A.loan_status_id = 200 AND 
B.status_enum  = 100
AND NOT EXISTS (
    SELECT 1
    FROM m_metrics C
    WHERE C.loan_id = A.id  
    AND C.status_enum = 50
);