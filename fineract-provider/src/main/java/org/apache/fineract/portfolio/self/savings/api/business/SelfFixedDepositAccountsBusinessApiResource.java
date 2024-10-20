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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.savings.api.FixedDepositAccountTransactionsApiResource;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.self.client.service.AppuserClientMapperReadService;
import org.apache.fineract.portfolio.self.savings.data.SelfSavingsDataValidator;
import org.apache.fineract.portfolio.self.savings.service.AppuserSavingsMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/self/fixeddepositaccounts/business")
@Component
@Scope("singleton")
@Tag(name = "Self Fixed Deposit Account", description = "")
public class SelfFixedDepositAccountsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final SelfSavingsDataValidator dataValidator;
    private final AppuserClientMapperReadService appUserClientMapperReadService;
    private final FixedDepositAccountTransactionsApiResource fixedDepositAccountTransactionsApiResource;
    private final AppuserSavingsMapperReadService appuserSavingsMapperReadService;

    public SelfFixedDepositAccountsBusinessApiResource(PlatformSecurityContext context, SelfSavingsDataValidator dataValidator,
            AppuserClientMapperReadService appUserClientMapperReadService,
            FixedDepositAccountTransactionsApiResource fixedDepositAccountTransactionsApiResource,
            AppuserSavingsMapperReadService appuserSavingsMapperReadService) {
        this.context = context;
        this.dataValidator = dataValidator;
        this.appUserClientMapperReadService = appUserClientMapperReadService;
        this.fixedDepositAccountTransactionsApiResource = fixedDepositAccountTransactionsApiResource;
        this.appuserSavingsMapperReadService = appuserSavingsMapperReadService;
    }

    @GET
    @Path("{accountId}/transactions/{transactionId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Fixed Deposit Account Transaction", description = "Retrieves Fixed Deposit Account Transaction\n\n"
            + "Example Requests:\n" + "\n" + "self/fixeddepositaccounts/1/transactions/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SelfFixedDepositAccountsBusinessApiResourceSwagger.GetSelfFixedDepositAccountsFixedDepositAccountIdTransactionsTransactionIdResponse.class))) })
    public String retrieveFixedDepositTransaction(@PathParam("accountId") @Parameter(description = "accountId") final Long accountId,
            @PathParam("transactionId") @Parameter(description = "transactionId") final Long transactionId,
            @Context final UriInfo uriInfo) {

        this.dataValidator.validateRetrieveSavingsTransaction(uriInfo);

        validateAppuserSavingsAccountMapping(accountId);

        return this.fixedDepositAccountTransactionsApiResource.retrieveOne(accountId, transactionId, uriInfo);
    }

    private void validateAppuserSavingsAccountMapping(final Long accountId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isMappedSavings = this.appuserSavingsMapperReadService.isSavingsMappedToUser(accountId, user.getId());
        if (!isMappedSavings) {
            throw new SavingsAccountNotFoundException(accountId);
        }
    }
}
