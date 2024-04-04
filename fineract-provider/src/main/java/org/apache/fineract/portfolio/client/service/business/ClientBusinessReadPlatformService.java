/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.client.service.business;

import com.google.gson.JsonObject;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessData;
import org.apache.fineract.portfolio.client.data.business.KycBusinessData;

public interface ClientBusinessReadPlatformService {

    ClientBusinessData retrieveOne(final Long clientId, final boolean showTemplate, final Boolean staffInSelectedOfficeOnly);

    ClientBusinessData retrieveTemplate(Long officeId, Boolean staffInSelectedOfficeOnly, final Integer legalFormId);

    Page<ClientData> retrieveAll(SearchParametersBusiness searchParameters);

    Page<ClientBusinessData> retrievePendingActivation(SearchParametersBusiness searchParameters);

    ClientData findClient(final String apiRequestBodyAsJson);

    KycBusinessData retrieveKycLevel(Long clientId);

    JsonObject retrieveBalance(Long clientId);

    KycBusinessData isClientExisting(final String email, final String mobileNo, final String altMobileNo, final String bvn,
            final String nin, final String tin);

    void queueSelfClientActivate();

}
