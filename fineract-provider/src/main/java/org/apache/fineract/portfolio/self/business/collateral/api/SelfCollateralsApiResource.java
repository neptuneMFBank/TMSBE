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
package org.apache.fineract.portfolio.self.business.collateral.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateral.api.CollateralsApiResource;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("self/loans/{loanId}/collaterals")
@Component
@Scope("singleton")
@Tag(name = "Loan Collateral", description = "In lending agreements, collateral is a borrower's pledge of specific property to a lender, to secure repayment of a loan. The collateral serves as protection for a lender against a borrower's default - that is, any borrower failing to pay the principal and interest under the terms of a loan obligation. If a borrower does default on a loan (due to insolvency or other event), that borrower forfeits (gives up) the property pledged as collateral - and the lender then becomes the owner of the collateral")
public class SelfCollateralsApiResource {

    private final PlatformSecurityContext context;
    private final CollateralsApiResource collateralsApiResource;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;

    @Autowired
    public SelfCollateralsApiResource(
            final PlatformSecurityContext context,
            final CodeValueReadPlatformService codeValueReadPlatformService,
            final CollateralsApiResource collateralsApiResource, final AppuserLoansMapperReadService appuserLoansMapperReadService) {
        this.context = context;
        this.collateralsApiResource = collateralsApiResource;
        this.appuserLoansMapperReadService = appuserLoansMapperReadService;
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve Collateral Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "loans/1/collaterals/template")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema()))})
    public String newCollateralTemplate(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        validateAppuserLoanMapping(loanId);
        return this.collateralsApiResource.newCollateralTemplate(uriInfo, loanId);

    }

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Loan Collaterals", description = "Example Requests:\n" + "\n" + "loans/1/collaterals\n" + "\n" + "\n"
            + "loans/1/collaterals?fields=value,description")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {
        validateAppuserLoanMapping(loanId);

        return this.collateralsApiResource.retrieveCollateralDetails(uriInfo, loanId);
    }

    @GET
    @Path("{collateralId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Retrieve a Collateral", description = "Example Requests:\n" + "\n" + "/loans/1/collaterals/1\n" + "\n" + "\n"
            + "/loans/1/collaterals/1?fields=description,description")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String retrieveCollateralDetails(@Context final UriInfo uriInfo,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long CollateralId) {
        validateAppuserLoanMapping(loanId);
        return this.collateralsApiResource.retrieveCollateralDetails(uriInfo, loanId, CollateralId);

    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Create a Collateral", description = "Note: Currently, Collaterals may be added only before a Loan is approved")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String createCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        validateAppuserLoanMapping(loanId);
        return this.collateralsApiResource.createCollateral(loanId, apiRequestBodyAsJson);
    }

    @PUT
    @Path("{collateralId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Update a Collateral")
    @RequestBody(required = true, content = @Content(schema = @Schema()))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String updateCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @Parameter(hidden = true) final String jsonRequestBody) {
        validateAppuserLoanMapping(loanId);
        return this.collateralsApiResource.updateCollateral(loanId, collateralId, jsonRequestBody);
    }

    @DELETE
    @Path("{collateralId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Remove a Collateral", description = "Note: A collateral can only be removed from Loans that are not yet approved.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema())))})
    public String deleteCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId) {
        validateAppuserLoanMapping(loanId);
        return this.collateralsApiResource.deleteCollateral(loanId, collateralId);
    }

    private void validateAppuserLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.appuserLoansMapperReadService.isLoanMappedToUser(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }
}
