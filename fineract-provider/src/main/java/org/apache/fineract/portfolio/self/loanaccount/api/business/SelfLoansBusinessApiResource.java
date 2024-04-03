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
package org.apache.fineract.portfolio.self.loanaccount.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanTransactionsBusinessApiResource;
import org.apache.fineract.portfolio.loanaccount.api.business.LoansBusinessApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.loanaccount.data.SelfLoansDataValidator;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/loans/business")
@Component
@Scope("singleton")
@Tag(name = "Self Loans", description = "")

public class SelfLoansBusinessApiResource {

    private final PlatformSecurityContext context;
    private final LoanTransactionsBusinessApiResource loanTransactionsBusinessApiResource;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;
    private final SelfLoansDataValidator dataValidator;
    private final LoansBusinessApiResource loansBusinessApiResource;

    @Autowired
    public SelfLoansBusinessApiResource(final PlatformSecurityContext context, final AppuserClientMapperReadService appUserClientMapperReadService,
            final LoanTransactionsBusinessApiResource loanTransactionsBusinessApiResource, final LoansBusinessApiResource loansBusinessApiResource,
            final AppuserLoansMapperReadService appuserLoansMapperReadService, final SelfLoansDataValidator dataValidator) {
        this.context = context;
        this.loanTransactionsBusinessApiResource = loanTransactionsBusinessApiResource;
        this.appuserLoansMapperReadService = appuserLoansMapperReadService;
        this.dataValidator = dataValidator;
        this.loansBusinessApiResource = loansBusinessApiResource;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
    }

    @GET
    @Path("{loanId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveLoan(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId, @Context final UriInfo uriInfo) {
        this.dataValidator.validateRetrieveLoan(uriInfo);
        validateAppuserLoanMapping(loanId);
        final boolean staffInSelectedOfficeOnly = false;
        final String associations = LoanApiConstants.LOAN_ASSOCIATIONS_ALL;
        final String exclude = "transactions";
        final String fields = null;
        return this.loansBusinessApiResource.retrieveLoan(loanId, staffInSelectedOfficeOnly, associations, exclude, fields, uriInfo);
    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        validateAppuserClientsMapping(clientId);
        final String externalId = null;
        final Long officeId = null;
        final Long staffId = null;
        return this.loansBusinessApiResource.retrieveAll(uriInfo, statusId, externalId, officeId, clientId, staffId, offset,
                limit, orderBy, sortOrder, accountNo, startPeriod, endPeriod, locale, dateFormat);
    }

    @POST
    @Path("calculate")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Calculate loan repayment schedule")
    public String calculateLoanScheduleLoanApplication(@Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        HashMap<String, Object> attr = this.dataValidator.validateLoanApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) attr.get("clientId");
        validateAppuserClientsMapping(clientId);
        return this.loansBusinessApiResource.calculateLoanScheduleLoanApplication(uriInfo, apiRequestBodyAsJson);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String submitLoanApplication(
            @QueryParam("command") @Parameter(description = "command") final String commandParam, @Context final UriInfo uriInfo,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        HashMap<String, Object> attr = this.dataValidator.validateLoanApplication(apiRequestBodyAsJson);
        final Long clientId = (Long) attr.get("clientId");
        validateAppuserClientsMapping(clientId);
        return this.loansBusinessApiResource.submitLoanApplication(apiRequestBodyAsJson, uriInfo);
    }

    @PUT
    @Path("{loanId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String modifyLoanApplication(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {
        HashMap<String, Object> attr = this.dataValidator.validateModifyLoanApplication(apiRequestBodyAsJson);
        validateAppuserLoanMapping(loanId);
        final Long clientId = (Long) attr.get("clientId");
        if (clientId != null) {
            validateAppuserClientsMapping(clientId);
        }
        return this.loansBusinessApiResource.modifyLoanApplication(loanId, apiRequestBodyAsJson, uriInfo);
    }

    @GET
    @Path("{loanId}/transactions")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
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
        final boolean isLoanMappedToUser = this.appuserLoansMapperReadService.isLoanMappedToUser(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }

    private void validateAppuserClientsMapping(final Long clientId) {
        AppUser user = this.context.authenticatedUser();
        final boolean mappedClientId = this.appUserClientMapperReadService.isClientMappedToUser(clientId, user.getId());
        if (!mappedClientId) {
            throw new ClientNotFoundException(clientId);
        }
    }
}
