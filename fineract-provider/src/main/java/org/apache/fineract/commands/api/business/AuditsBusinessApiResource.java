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
package org.apache.fineract.commands.api.business;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.data.business.AuditBusinessData;
import org.apache.fineract.commands.data.business.AuditBusinessSearchData;
import org.apache.fineract.commands.service.business.AuditBusinessReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audits/business")
@Component
@Scope("singleton")
@Tag(name = "Audits", description = "")
@RequiredArgsConstructor
public class AuditsBusinessApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("id", "actionName", "entityName", "resourceId", "subresourceId", "maker", "madeOnDate", "checker",
                    "checkedOnDate", "processingResult", "commandAsJson", "officeName", "groupLevelName", "groupName", "clientName",
                    "loanAccountNo", "savingsAccountNo", "clientId", "loanId", "url", "supervisorStaffData", "roleId"));

    private final String resourceNameForPermissions = "AUDIT";

    private final PlatformSecurityContext context;
    private final AuditBusinessReadPlatformService auditBusinessReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AuditBusinessData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<AuditBusinessSearchData> toApiJsonSerializerSearchTemplate;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Audits", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAllAuditEntries(@Context final UriInfo uriInfo,
            @QueryParam("isChecker") @Parameter(description = "isChecker") final Boolean isChecker,
            @QueryParam("actionName") @Parameter(description = "actionName") final String actionName,
            @QueryParam("entityName") @Parameter(description = "entityName") final String entityName,
            @QueryParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @QueryParam("makerCheckerId") @Parameter(description = "makerCheckerId") final Long makerCheckerId,
            @QueryParam("processingResult") @Parameter(description = "processingResult") final Integer processingResult,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("aud.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forAudit(fromDate, toDate, isChecker, actionName,
                entityName, resourceId, makerCheckerId, processingResult, officeId, groupId, clientId, loanId, savingsAccountId, offset,
                limit, orderBy, sortOrder, null);

        final Page<AuditBusinessData> auditData = this.auditBusinessReadPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, auditData, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Audit Search Template", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditBusinessSearchData auditSearchData = this.auditBusinessReadPlatformService.retrieveSearchTemplate("audit");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(
                Arrays.asList("offices", "actionNames", "entityNames", "processingResults"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    @GET
    @Path("/waiting-approval")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Audits Awaiting Approval", description = "")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retrieveWaitingApproval(@Context final UriInfo uriInfo,
            @QueryParam("supervisorStaffId") @Parameter(description = "supervisorStaffId") final Long supervisorStaffId,
            @QueryParam("actionName") @Parameter(description = "actionName") final String actionName,
            @QueryParam("entityName") @Parameter(description = "entityName") final String entityName,
            @QueryParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @QueryParam("makerId") @Parameter(description = "makerId") final Long makerId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Long groupId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Long clientId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Long savingsAccountId,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("aud.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("asc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forAudit(fromDate, toDate, null, actionName, entityName,
                resourceId, makerId, null, officeId, groupId, clientId, loanId, savingsAccountId, offset, limit, orderBy, sortOrder,
                supervisorStaffId);

        final Page<AuditBusinessData> auditData = this.auditBusinessReadPlatformService.retrieveWaitingApproval(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, auditData, RESPONSE_DATA_PARAMETERS);
    }
}
