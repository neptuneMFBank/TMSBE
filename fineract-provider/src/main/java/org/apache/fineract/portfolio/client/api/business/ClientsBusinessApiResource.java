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
package org.apache.fineract.portfolio.client.api.business;

import com.google.gson.JsonObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.LOAN_PRODUCTS;
import static org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType.SAVINGS_PRODUCTS;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessData;
import org.apache.fineract.portfolio.client.data.business.KycBusinessData;
import org.apache.fineract.portfolio.client.service.business.ClientBusinessReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.apache.fineract.portfolio.loanaccount.guarantor.service.GuarantorReadPlatformService;
import org.apache.fineract.portfolio.products.service.business.ProductVisibilityReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients/business")
@Component
@Scope("singleton")
@Tag(name = "Client", description = """
        Clients are people and businesses that have applied (or may apply) to an MFI for loans.

        Clients can be created in Pending or straight into Active state.""")
@RequiredArgsConstructor
public class ClientsBusinessApiResource {

    private final PlatformSecurityContext context;
    private final ClientBusinessReadPlatformService clientBusinessReadPlatformService;
    private final ToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final ToApiJsonSerializer<ClientBusinessData> toBusinessApiJsonSerializer;
    private final ToApiJsonSerializer<KycBusinessData> toKycBusinessApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> clientAccountSummaryToApiJsonSerializer;
    private final ToApiJsonSerializer<JsonObject> clientAccountBalanceSummary;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final GuarantorReadPlatformService guarantorReadPlatformService;
    private final ProductVisibilityReadPlatformService loanProductVisibilityReadPlatformService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Clients", description = """
            The list capability of clients can support pagination and sorting.

            Example Requests:

            clients\business

            clients\business?fields=displayName,officeName,timeline

            clients\business?offset=10&limit=50

            clients\business?orderBy=displayName&sortOrder=DESC""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
    ) })
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("bvn") @Parameter(description = "bvn") final String bvn,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("email") @Parameter(description = "email") final String email,
            @QueryParam("mobile") @Parameter(description = "mobile") final String mobile,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("legalFormId") @Parameter(description = "legalFormId") final Integer legalFormId,
            @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("underHierarchy") @Parameter(description = "underHierarchy") final String hierarchy,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @DefaultValue("20") @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue(" c.id ") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue(" desc ") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("orphansOnly") @Parameter(description = "orphansOnly") final Boolean orphansOnly,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        return this.retrieveAll(uriInfo, officeId, externalId, displayName, statusId, hierarchy, offset, limit, orderBy, sortOrder,
                orphansOnly, false, startPeriod, endPeriod, locale, dateFormat, staffId, accountNo, email, mobile, legalFormId, bvn);
    }

    public String retrieveAll(final UriInfo uriInfo, final Long officeId, final String externalId, final String displayName,
            final Integer statusId, final String hierarchy, final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Boolean orphansOnly, final boolean isSelfUser, final DateParam startPeriod,
            final DateParam endPeriod, final String locale, final String dateFormat, final Long staffId, final String accountNo,
            final String email, final String mobile, final Integer legalFormId, final String bvn) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forClientsBusiness(officeId, externalId, statusId,
                hierarchy, offset, limit, orderBy, sortOrder, staffId, accountNo, fromDate, toDate, displayName, orphansOnly, isSelfUser,
                email, mobile, legalFormId, bvn);

        final Page<ClientData> clientData = this.clientBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = """
            Example Requests:

            clients/1


            clients/1?template=true


            clients/1?fields=id,displayName,officeName""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
    ) })
    public String retrieveOne(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final Boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientBusinessData clientData = this.clientBusinessReadPlatformService.retrieveOne(clientId, settings.isTemplate(),
                staffInSelectedOfficeOnly);

        return this.toBusinessApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Client Details Template", description = """
            This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:

            Field Defaults
            Allowed Value Lists

            Example Request:

            clients/template""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsTemplateResponse.class))
    ) })
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @Parameter(description = "officeId") @QueryParam("officeId") final Long officeId,
            // @QueryParam("commandParam") @Parameter(description = "commandParam") final String commandParam,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final Boolean staffInSelectedOfficeOnly,
            @QueryParam("legalFormId") final Integer legalFormId) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        ClientBusinessData clientData;
        // if (is(commandParam, "close")) {
        // clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        // } else if (is(commandParam, "acceptTransfer")) {
        // clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_CLOSURE_REASON);
        // } else if (is(commandParam, "reject")) {
        // clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_REJECT_REASON);
        // } else if (is(commandParam, "withdraw")) {
        // clientData = this.clientReadPlatformService.retrieveAllNarrations(ClientApiConstants.CLIENT_WITHDRAW_REASON);
        // } else {
        clientData = this.clientBusinessReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly, legalFormId);
        // }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    // private boolean is(final String commandParam, final String commandValue) {
    // return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    // }
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Client", description = """
            Note:

            1. You can enter either:firstname/middlename/lastname - for a person (middlename is optional) OR fullname - for a business or organisation (or person known by one name).

            2.If address is enable(enable-address=true), then additional field called address has to be passed.

            Mandatory Fields: firstname and lastname OR fullname, officeId, active=true and activationDate OR active=false, if(address enabled) address

            Optional Fields: groupId, externalId, accountNo, staffId, mobileNo, savingsProductId, genderId, clientTypeId, clientClassificationId""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsResponse.class))
    ) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createClientBusiness()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client", description = """
            Note: You can update any of the basic attributes of a client (but not its associations) using this API.

            Changing the relationship between a client and its office is not supported through this API. An API specific to handling transfers of clients between offices is available for the same.

            The relationship between a client and a group must be removed through the Groups API.""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.PutClientsClientIdRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.PutClientsClientIdResponse.class))
    ) })
    public String update(@Parameter(description = "clientId") @PathParam("clientId") final Long clientId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateClientBusiness(clientId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("/find")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Client", description = """
            Note: Search by mobile_no,email_address,account_no
                                                                {"key":"email_address","value":"kunlethompson2@gmail.com"}
                                                          """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.PostClientsResponse.class))
    ) })
    public String findClient(@Context final UriInfo uriInfo, @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientData clientData = this.clientBusinessReadPlatformService.findClient(apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);

    }

    @GET
    @Path("{clientId}/kyc-level")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Client", description = """
            Example Requests:""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdResponse.class))
    ) })
    public String retrieveKycLevel(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        KycBusinessData clientData = this.clientBusinessReadPlatformService.retrieveKycLevel(clientId);

        return this.toKycBusinessApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.KYC_CHECKERS_DATA_PARAMETERS);
    }

    @GET
    @Path("pend")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Pend Clients", description = """
            Example Requests:""")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = ClientsApiResourceSwagger.GetClientsResponse.class))
    ) })
    public String retrievePendingActivation(@Context final UriInfo uriInfo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("displayName") @Parameter(description = "displayName") final String displayName,
            @QueryParam("bvn") @Parameter(description = "bvn") final String bvn,
            @QueryParam("legalFormId") @Parameter(description = "legalFormId") final Integer legalFormId,
            @QueryParam("supervisorStaffId") @Parameter(description = "supervisorStaffId") final Long supervisorStaffId,
            @QueryParam("accountNo") @Parameter(description = "accountNo") final String accountNo,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forClientPendingActivation(fromDate, toDate, legalFormId,
                officeId, supervisorStaffId, bvn, displayName, accountNo, offset, limit, orderBy, sortOrder);

        final Page<ClientBusinessData> clientData = this.clientBusinessReadPlatformService.retrievePendingActivation(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toBusinessApiJsonSerializer.serialize(settings, clientData, ClientBusinessApiConstants.CLIENT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{clientId}/balance")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve client accounts balance", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsClientIdAccountsResponse.class))
    ), @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String retrieveAssociatedAccounts(@PathParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final JsonObject retrieveBalance = this.clientBusinessReadPlatformService.retrieveBalance(clientId);
        final Set<String> CLIENT_BALANCE_DATA_PARAMETERS = new HashSet<>(
                Arrays.asList("loanAccount", "savingDeposit", "fixedDeposit", "recurringDeposit"
                // , "currentDeposit"
                ));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.clientAccountBalanceSummary.serialize(settings, retrieveBalance, CLIENT_BALANCE_DATA_PARAMETERS);
    }

    @GET
    @Path("exist")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "is Client Existing", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad Request") })
    public String isClientExisting(@QueryParam("email") final String email, @QueryParam("mobileNo") final String mobileNo,
            @QueryParam("altMobileNo") final String altMobileNo, @QueryParam("bvn") final String bvn, @QueryParam("nin") final String nin,
            @QueryParam("tin") final String tin) {
        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);
        final KycBusinessData businessData = this.clientBusinessReadPlatformService.isClientExisting(email, mobileNo, altMobileNo, bvn, nin,
                tin);
        return this.clientAccountBalanceSummary.serialize(businessData);

    }

    @GET
    @Path("{clientId}/loanproducts/visibility")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveVisibileLoanProduct(@PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonObject LoanProducts = this.loanProductVisibilityReadPlatformService.retrieveVisibileProductForClient(clientId, LOAN_PRODUCTS.getValue());

        return this.toApiJsonSerializer.serialize(LoanProducts);
    }
 @GET
    @Path("{clientId}/savingsproducts/visibility")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveVisibileSavingsProduct(@PathParam("clientId") final Long clientId) {

        this.context.authenticatedUser().validateHasReadPermission(ClientApiConstants.CLIENT_RESOURCE_NAME);

        final JsonObject LoanProducts = this.loanProductVisibilityReadPlatformService.retrieveVisibileProductForClient(clientId, SAVINGS_PRODUCTS.getValue());

        return this.toApiJsonSerializer.serialize(LoanProducts);
    }

}
