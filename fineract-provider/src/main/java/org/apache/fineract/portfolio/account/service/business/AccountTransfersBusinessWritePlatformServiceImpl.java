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
package org.apache.fineract.portfolio.account.service.business;

import static org.apache.fineract.portfolio.account.AccountDetailConstants.fromAccountTypeParamName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.savings.SavingsApiConstants;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountTransfersBusinessWritePlatformServiceImpl implements AccountTransfersBusinessWritePlatformService {

    private final FromJsonHelper fromApiJsonHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public AccountTransfersBusinessWritePlatformServiceImpl(final FromJsonHelper fromApiJsonHelper, final PlatformSecurityContext context,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.context = context;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @Transactional
    @Override
    public CommandProcessingResult create(final String apiRequestBodyAsJson) {
        this.context.authenticatedUser();

        final JsonElement element = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonObject createTransfer = element.getAsJsonObject();
        createTransfer.addProperty(fromAccountTypeParamName, PortfolioAccountType.SAVINGS.getValue());
        if (!this.fromApiJsonHelper.parameterExists(SavingsApiConstants.localeParamName, element)) {
            createTransfer.addProperty(SavingsApiConstants.localeParamName, GeneralConstants.LOCALE_EN_DEFAULT);
        }

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createAccountTransfer().withJson(createTransfer.toString())
                .build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return result;

    }
}
