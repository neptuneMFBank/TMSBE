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
package org.apache.fineract.portfolio.self.savings.api.business;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.api.business.SavingsAccountTransactionsBusinessApiResource;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/savingsaccounts/business")
@Component
@Scope("singleton")

@Tag(name = "Self Savings Account", description = "")
public class SelfSavingsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final SavingsAccountTransactionsBusinessApiResource savingsAccountTransactionsBusinessApiResource;
    private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;

    @Autowired
    public SelfSavingsBusinessApiResource(final PlatformSecurityContext context,
            final SavingsAccountTransactionsBusinessApiResource savingsAccountTransactionsBusinessApiResource,
            final AppuserSavingsMapperReadService appuserSavingsMapperReadService,
            final AppuserClientMapperReadService appUserClientMapperReadService) {
        this.context = context;

        this.savingsAccountTransactionsBusinessApiResource = savingsAccountTransactionsBusinessApiResource;
        this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;

    }

    @GET
    @Path("{savingsId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllBySavingsId(@PathParam("savingsId") final Long savingsId, @Context final UriInfo uriInfo,
            @QueryParam("startPeriod") @Parameter(description = "fromDate") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "toDate") final DateParam endPeriod,
            @QueryParam("transactionTypeId") @Parameter(description = "transactionTypeId") final Long transactionTypeId,
            @QueryParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        validateAppuserSavingsAccountMapping(savingsId);

        return this.savingsAccountTransactionsBusinessApiResource.retrieveAllBySavingsId(savingsId, uriInfo, startPeriod, endPeriod,
                transactionTypeId, transactionId, offset, limit, orderBy, sortOrder, locale, dateFormat);

    }

    private void validateAppuserSavingsAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMappedSavings = this.appuserSavingsMapperReadService.isSavingsMappedToUser(accountId, user.getId());
        if (!isMappedSavings) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }

}
