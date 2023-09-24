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
package org.apache.fineract.portfolio.savings.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.savings.business.DepositsBusinessApiConstants;
import static org.apache.fineract.portfolio.savings.business.DepositsBusinessApiConstants.DEPOSIT_RESPONSE_DATA_PARAMETERS;
import org.apache.fineract.portfolio.savings.data.business.DepositAccountBusinessData;
import org.apache.fineract.portfolio.savings.service.business.DepositsBusinessReadPlatformService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/deposits")
@Component
@Scope("singleton")
@Tag(name = "Deposits", description = """
        View of all saving and investment deposit types""")
@RequiredArgsConstructor
public class DepositsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final ToApiJsonSerializer<DepositAccountBusinessData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DepositsBusinessReadPlatformService depositsBusinessReadPlatformService;

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Deposits", description = """
            The list capability of deposits can support pagination and sorting.
""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
        )})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("bvn") @Parameter(description = "bvn") final String bvn,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("depositTypeId") @Parameter(description = "depositTypeId") final Integer depositTypeId,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue(" ms.id ") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue(" desc ") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forDeposit(offset, limit, orderBy, sortOrder, productId, fromDate, toDate, depositTypeId, accountNo, officeId, statusId, externalId, clientId);

        final Page<DepositAccountBusinessData> clientData = this.depositsBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNo}/enquiry")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve account enquiry", description = """
            Example Requests:""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
        )})
    public String retrieveName(@PathParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        DepositAccountBusinessData depositAccountBusinessData = this.depositsBusinessReadPlatformService.retrieveName(accountNo);

        return this.toApiJsonSerializer.serialize(settings, depositAccountBusinessData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNo}/balance")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve account enquiry", description = """
            Example Requests:""")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        // , content = @Content(schema = @Schema(implementation =
        // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
        )})
    public String retrieveBalance(@PathParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(DepositsBusinessApiConstants.RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        DepositAccountBusinessData depositAccountBusinessData = this.depositsBusinessReadPlatformService.retrieveBalance(accountNo);

        return this.toApiJsonSerializer.serialize(settings, depositAccountBusinessData, DEPOSIT_RESPONSE_DATA_PARAMETERS);
    }

}
