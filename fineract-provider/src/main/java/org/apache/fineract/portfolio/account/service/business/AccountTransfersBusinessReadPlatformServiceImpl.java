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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.api.business.AccountTransfersBusinessApiConstants;
import org.apache.fineract.portfolio.account.data.AccountTransferData;
import org.apache.fineract.portfolio.account.data.PortfolioAccountDTO;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.business.AccountTransfersBusinessDataValidator;
import org.apache.fineract.portfolio.account.service.AccountTransferEnumerations;
import org.apache.fineract.portfolio.account.service.PortfolioAccountReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AccountTransfersBusinessReadPlatformServiceImpl implements AccountTransfersBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;

    // pagination
    private final PaginationHelper paginationHelper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final PlatformSecurityContext context;

    private final AccountTransfersBusinessDataValidator accountTransfersBusinessDataValidator;
    private final FromJsonHelper fromJsonHelper;
    private final ClientBusinessReadPlatformService clientBusinessReadPlatformService;

    @Autowired
    public AccountTransfersBusinessReadPlatformServiceImpl(final JdbcTemplate jdbcTemplate,
            final ClientReadPlatformService clientReadPlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final PortfolioAccountReadPlatformService portfolioAccountReadPlatformService, final ColumnValidator columnValidator,
            DatabaseSpecificSQLGenerator sqlGenerator, PaginationHelper paginationHelper, final PlatformSecurityContext context,
            final AccountTransfersBusinessDataValidator accountTransfersBusinessDataValidator, final FromJsonHelper fromJsonHelper,
            final ClientBusinessReadPlatformService clientBusinessReadPlatformService) {
        this.jdbcTemplate = jdbcTemplate;
        this.clientReadPlatformService = clientReadPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.portfolioAccountReadPlatformService = portfolioAccountReadPlatformService;
        this.columnValidator = columnValidator;
        this.sqlGenerator = sqlGenerator;
        this.paginationHelper = paginationHelper;
        this.context = context;
        this.accountTransfersBusinessDataValidator = accountTransfersBusinessDataValidator;
        this.fromJsonHelper = fromJsonHelper;
        this.clientBusinessReadPlatformService = clientBusinessReadPlatformService;
    }

    @Override
    public AccountTransferData retrieveTemplate(final String apiRequestBodyAsJson) {

        this.context.authenticatedUser();
        this.accountTransfersBusinessDataValidator.validateForTemplate(apiRequestBodyAsJson);
        final JsonElement jsonElement = this.fromJsonHelper.parse(apiRequestBodyAsJson);
        final Long fromAccountId = this.fromJsonHelper.extractLongNamed(AccountTransfersBusinessApiConstants.fromAccountIdParamName,
                jsonElement);
        final Integer toAccountType = this.fromJsonHelper
                .extractIntegerSansLocaleNamed(AccountTransfersBusinessApiConstants.toAccountTypeParamName, jsonElement);
        final String key = this.fromJsonHelper.extractStringNamed(AccountTransfersBusinessApiConstants.keyParam, jsonElement);
        final String value = this.fromJsonHelper.extractStringNamed(AccountTransfersBusinessApiConstants.valueParam, jsonElement);
        PortfolioAccountData toAccount = null;

        Long toClientId = null;
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            final JsonObject findClientJson = new JsonObject();
            findClientJson.addProperty(AccountTransfersBusinessApiConstants.keyParam, key);
            findClientJson.addProperty(AccountTransfersBusinessApiConstants.valueParam, value);
            if (StringUtils.equalsIgnoreCase(key, "account_no")) {
                //check which account, LOANS/SAVINGS
                toAccount = this.portfolioAccountReadPlatformService.retrieveOneViaAccountNumber(value, toAccountType);
                toClientId = toAccount.clientId();
            } else {
                final ClientData toFindClient = this.clientBusinessReadPlatformService.findClient(findClientJson.toString());
                toClientId = toFindClient.getId();
            }
        }

        final Integer fromAccountType = PortfolioAccountType.SAVINGS.getValue();
        /*
         * final Long fromOfficeId, final Long fromClientId, final Long fromAccountId, final Integer fromAccountType,
         * final Long toOfficeId, final Long toClientId, final Long toAccountId, final Integer toAccountType
         */
        final EnumOptionData loanAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.LOAN);
        final EnumOptionData savingsAccountType = AccountTransferEnumerations.accountType(PortfolioAccountType.SAVINGS);

        final Integer mostRelevantFromAccountType = fromAccountType;
        final Collection<EnumOptionData> fromAccountTypeOptions = null;// Arrays.asList(savingsAccountType,
        // loanAccountType);
        final Collection<EnumOptionData> toAccountTypeOptions = Arrays.asList(loanAccountType, savingsAccountType);
        final Integer mostRelevantToAccountType = toAccountType;

        final EnumOptionData fromAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantFromAccountType);
        final EnumOptionData toAccountTypeData = AccountTransferEnumerations.accountType(mostRelevantToAccountType);

        // from settings
        OfficeData fromOffice = null;
        ClientData fromClient;// = null;
        PortfolioAccountData fromAccount;// = null;

        OfficeData toOffice = null;
        ClientData toClient = null;

        // template
        Collection<PortfolioAccountData> fromAccountOptions = null;
        Collection<PortfolioAccountData> toAccountOptions = null;

        // Long mostRelevantFromOfficeId = fromOfficeId;
        Long mostRelevantFromClientId;// = fromClientId;

        // Long mostRelevantToOfficeId=null;//= toOfficeId;
        Long mostRelevantToClientId = toClientId;

        fromAccount = this.portfolioAccountReadPlatformService.retrieveOne(fromAccountId, fromAccountType);

        // override provided fromClient with client of account
        mostRelevantFromClientId = fromAccount.clientId();

        fromClient = this.clientReadPlatformService.retrieveOne(mostRelevantFromClientId);

        Collection<OfficeData> fromOfficeOptions = null;
        Collection<ClientData> fromClientOptions = null;

        // defaults
        final LocalDate transferDate = DateUtils.getBusinessLocalDate();
        Collection<OfficeData> toOfficeOptions = fromOfficeOptions;
        Collection<ClientData> toClientOptions = null;

        if (mostRelevantToClientId != null && toAccount == null) {
            toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            toAccountOptions = retrieveToAccounts(fromAccount, mostRelevantToAccountType, mostRelevantToClientId);
        } else {
            if (mostRelevantToClientId != null) {
                toClient = this.clientReadPlatformService.retrieveOne(mostRelevantToClientId);
            }
        }
        return AccountTransferData.template(fromOffice, fromClient, fromAccountTypeData, fromAccount, transferDate, toOffice, toClient,
                toAccountTypeData, toAccount, fromOfficeOptions, fromClientOptions, fromAccountTypeOptions, fromAccountOptions,
                toOfficeOptions, toClientOptions, toAccountTypeOptions, toAccountOptions);
    }

    private Collection<PortfolioAccountData> retrieveToAccounts(final PortfolioAccountData excludeThisAccountFromOptions,
            final Integer toAccountType, final Long toClientId) {

        final String currencyCode = excludeThisAccountFromOptions != null ? excludeThisAccountFromOptions.currencyCode() : null;

        PortfolioAccountDTO portfolioAccountDTO = new PortfolioAccountDTO(toAccountType, toClientId, currencyCode, null, null);
        Collection<PortfolioAccountData> accountOptions = this.portfolioAccountReadPlatformService
                .retrieveAllForLookup(portfolioAccountDTO);
        if (!CollectionUtils.isEmpty(accountOptions)) {
            accountOptions.remove(excludeThisAccountFromOptions);
        } else {
            accountOptions = null;
        }

        return accountOptions;
    }
}
