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

CREATE OR REPLACE VIEW loan_product_visibility_config_view AS
    SELECT
        mlpvc.id,
        mlpvc.name,
        mlpvc.description,
        GROUP_CONCAT(DISTINCT cv.loanproduct_id) AS loanproducts,
        CONCAT(',',
                GROUP_CONCAT(DISTINCT clientclassification.clientclassification_id),
                ',') AS client_classifications,
        CONCAT(',',
                GROUP_CONCAT(DISTINCT clienttype.clienttype_id),
                ',') AS client_types,
        CONCAT(',',
                GROUP_CONCAT(DISTINCT legalenum.legalenum_id),
                ',') AS legal_enums
    FROM
        m_loanproduct_visibility_config mlpvc
            LEFT JOIN
        m_loanproduct_visibility_config_mapping cv ON cv.config_id = mlpvc.id
            LEFT JOIN
        m_loanproduct_visibility_clientclassification_mapping clientclassification ON clientclassification.config_id = mlpvc.id
            LEFT JOIN
        m_loanproduct_visibility_clienttype_mapping clienttype ON clienttype.config_id = mlpvc.id
            LEFT JOIN
        m_loanproduct_visibility_legalenum_mapping legalenum ON legalenum.config_id = mlpvc.id
    GROUP BY mlpvc.id;
