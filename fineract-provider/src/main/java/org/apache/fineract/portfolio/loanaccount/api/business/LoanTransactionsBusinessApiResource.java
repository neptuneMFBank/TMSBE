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
package org.apache.fineract.portfolio.loanaccount.api.business;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.service.business.LoanBusinessReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loans/{loanId}/transactions/business")
@Component
@Scope("singleton")
@Tag(name = "Loan Transactions", description = "Capabilities include loan repayment's, interest waivers and the ability to 'adjust' an existing transaction. An 'adjustment' of a transaction is really a 'reversal' of existing transaction followed by creation of a new transaction with the provided details.")
public class LoanTransactionsBusinessApiResource {

    private final Set<String> responseDataParameters = new HashSet<>(
            Arrays.asList("id", "type", "date", "currency", "amount", "externalId"));

    private final String resourceNameForPermissions = "LOAN";

    private final String startPeriodParameterName = "startPeriod";

    private final String endPeriodParameterName = "endPeriod";

    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final LoanBusinessReadPlatformService loanBusinessReadPlatformService;

    @Autowired
    public LoanTransactionsBusinessApiResource(final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<LoanTransactionData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final LoanBusinessReadPlatformService loanBusinessReadPlatformService) {
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.loanBusinessReadPlatformService = loanBusinessReadPlatformService;
    }

    @GET
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
        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(this.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(this.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forTransactions(transactionTypeId, transactionId, offset,
                limit, orderBy, sortOrder, fromDate, toDate, null);

        final Page<LoanTransactionData> transactionData = this.loanBusinessReadPlatformService.retrieveAllTransactionsByLoanId(loanId,
                searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, transactionData, this.responseDataParameters);
    }

}
