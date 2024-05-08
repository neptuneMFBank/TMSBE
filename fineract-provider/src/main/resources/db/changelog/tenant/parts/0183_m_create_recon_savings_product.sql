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


-- INSERT m_savings_product
INSERT INTO fineract_default.m_savings_product (id,name,short_name,description,deposit_type_enum,currency_code,currency_digits,currency_multiplesof,nominal_annual_interest_rate,interest_compounding_period_enum,interest_posting_period_enum,interest_calculation_type_enum,interest_calculation_days_in_year_type_enum,min_required_opening_balance,lockin_period_frequency,lockin_period_frequency_enum,accounting_type,withdrawal_fee_amount,withdrawal_fee_type_enum,withdrawal_fee_for_transfer,allow_overdraft,overdraft_limit,nominal_annual_interest_rate_overdraft,min_overdraft_for_interest_calculation,min_required_balance,enforce_min_required_balance,min_balance_for_interest_calculation,withhold_tax,tax_group_id,is_dormancy_tracking_active,days_to_inactive,days_to_dormancy,days_to_escheat,max_allowed_lien_limit,is_lien_allowed) VALUES
	 (2,'Reconciliation Savings','RS','Reconciliation Savings',100,'NGN',2,100,0.000000,1,4,1,365,NULL,NULL,NULL,1,NULL,NULL,0,0,NULL,NULL,NULL,NULL,0,NULL,0,NULL,0,NULL,NULL,NULL,0.000000,0);
