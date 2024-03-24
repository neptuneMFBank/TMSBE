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

CREATE VIEW m_client_kyc_checkers AS
SELECT
mc.id client_id,
    CASE
        WHEN
             mc.id > 0 IS NOT NULL THEN '1'
        ELSE '0'
    END AS has_personal,
    CASE
        WHEN
             mca.client_id > 0 THEN '1'
        ELSE '0'
    END AS has_residential,
    CASE
        WHEN
             ek.client_id > 0  THEN '1'
        ELSE '0'
    END AS has_employment,
    CASE
        WHEN
             slk.iAgree=1 THEN '1'
        ELSE '0'
    END AS has_agreement,
    CASE
        WHEN
             sfmk.client_id > 0  THEN '1'
        ELSE '0'
    END AS has_next_of_kin,
    CASE
        WHEN
             bk.client_id > 0  THEN '1'
        ELSE '0'
    END AS has_bank_detail,
    CASE
        WHEN
             mci.client_id > 0 THEN '1'
        ELSE '0'
    END AS has_identification,
    CASE
        WHEN
             sk.client_id > 0 THEN '1'
        ELSE '0'
    END AS has_directors
    -- ,
    -- CASE
    --     WHEN
    --          smk.client_id > 0 THEN '1'
    --     ELSE '0'
    -- END AS has_social_media
FROM
    m_client mc
LEFT JOIN
    secondLevelKYC slk ON slk.client_id =mc.id
LEFT JOIN
    m_client_address mca ON mca.client_id =mc.id AND mca.address_type_id=15
LEFT JOIN
    secondFamilyMemberKYC sfmk ON sfmk.client_id =mc.id
LEFT JOIN
    m_client_identifier mci ON mci.client_id =mc.id
LEFT JOIN
    employerKYC ek ON ek.client_id =mc.id
LEFT JOIN
    bankKYC bk ON bk.client_id =mc.id
LEFT JOIN
    signatoryKYC sk ON sk.client_id =mc.id
-- LEFT JOIN
--     socialMediaKYC smk ON smk.client_id =mc.id
WHERE (slk.bvn IS NOT NULL OR slk.tin IS NOT NULL)
GROUP BY mc.id;
