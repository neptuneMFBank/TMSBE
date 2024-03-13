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
package org.apache.fineract.portfolio.business.merchant.loanaccount.api;

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
import org.apache.fineract.portfolio.business.merchant.loanaccount.service.MerchantLoansMapperReadService;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanTransactionsBusinessApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/merchant/loans")
@Component
@Scope("singleton")
@Tag(name = "Merchant Loans", description = "")

public class MerchantLoansApiResource {

    private final PlatformSecurityContext context;
    private final LoanTransactionsBusinessApiResource loanTransactionsBusinessApiResource;
    private final MerchantLoansMapperReadService merchantLoansMapperReadService;

    @Autowired
    public MerchantLoansApiResource(final PlatformSecurityContext context,
            final LoanTransactionsBusinessApiResource loanTransactionsBusinessApiResource,
            final MerchantLoansMapperReadService merchantLoansMapperReadService) {
        this.context = context;
        this.loanTransactionsBusinessApiResource = loanTransactionsBusinessApiResource;
        this.merchantLoansMapperReadService = merchantLoansMapperReadService;

    }

    @GET
    @Path("{loanId}/transactions")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllTransactionsByLoanId(@PathParam("loanId") final Long loanId, @Context final UriInfo uriInfo,
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

        validateAppuserLoanMapping(loanId);

        return this.loanTransactionsBusinessApiResource.retrieveAllTransactionsByLoanId(loanId, uriInfo, startPeriod, endPeriod,
                transactionTypeId, transactionId, offset, limit, orderBy, sortOrder, locale, dateFormat);
    }

    private void validateAppuserLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.merchantLoansMapperReadService.isLoanMappedToMerchant(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }
}
