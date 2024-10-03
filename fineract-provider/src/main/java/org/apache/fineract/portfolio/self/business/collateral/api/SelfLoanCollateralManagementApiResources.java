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
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.collateralmanagement.api.LoanCollateralManagementApiResources;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.apache.fineract.portfolio.self.loanaccount.service.AppuserLoansMapperReadService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("self/loan-collateral-management/{loanId}")
@Component
@Scope("singleton")
@Tag(name = "Loan Collateral Management", description = "Loan Collateral Management is for managing collateral operations")
public class SelfLoanCollateralManagementApiResources {

    private final PlatformSecurityContext context;
    private final AppuserLoansMapperReadService appuserLoansMapperReadService;
    private final LoanCollateralManagementApiResources loanCollateralManagementApiResources;

    public SelfLoanCollateralManagementApiResources(final PlatformSecurityContext context,
            final AppuserLoansMapperReadService appuserLoansMapperReadService,
            final LoanCollateralManagementApiResources loanCollateralManagementApiResources) {

        this.context = context;
        this.appuserLoansMapperReadService = appuserLoansMapperReadService;
        this.loanCollateralManagementApiResources = loanCollateralManagementApiResources;
    }

    @DELETE
    @Path("{id}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(description = "Delete Loan Collateral", summary = "Delete Loan Collateral")
    public String deleteLoanCollateral(@PathParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @PathParam("id") @Parameter(description = "loan collateral id") final Long id) {
        validateAppuserLoanMapping(loanId);

        return this.loanCollateralManagementApiResources.deleteLoanCollateral(loanId, id);
    }

    @GET
    @Path("{collateralId}")
    @Produces({MediaType.APPLICATION_JSON})
    @Consumes({MediaType.APPLICATION_JSON})
    @Operation(description = "Get Loan Collateral Details", summary = "Get Loan Collateral Details")
    public String getLoanCollateral(@PathParam("collateralId") @Parameter(description = "collateralId") final Long collateralId,
            @PathParam("loanId") @Parameter(description = "loanId") final Long loanId) {

        validateAppuserLoanMapping(loanId);
        return this.loanCollateralManagementApiResources.getLoanCollateral(collateralId);

    }

    private void validateAppuserLoanMapping(final Long loanId) {
        AppUser user = this.context.authenticatedUser();
        final boolean isLoanMappedToUser = this.appuserLoansMapperReadService.isLoanMappedToUser(loanId, user.getId());
        if (!isLoanMappedToUser) {
            throw new LoanNotFoundException(loanId);
        }
    }
}
