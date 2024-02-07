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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.data.AuditData;
import org.apache.fineract.commands.data.AuditSearchData;
import org.apache.fineract.commands.service.AuditReadPlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.SQLBuilder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audits/business")
@Component
@Scope("singleton")
@Tag(name = "Audits", description = "")
@RequiredArgsConstructor
public class AuditsBusinessApiResource {

    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "actionName", "entityName", "resourceId",
            "subresourceId", "maker", "madeOnDate", "checker", "checkedOnDate", "processingResult", "commandAsJson", "officeName",
            "groupLevelName", "groupName", "clientName", "loanAccountNo", "savingsAccountNo", "clientId", "loanId", "url"));

    private final String resourceNameForPermissions = "AUDIT";

    private final PlatformSecurityContext context;
    private final AuditReadPlatformService auditReadPlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer;
    private final DefaultToApiJsonSerializer<AuditSearchData> toApiJsonSerializerSearchTemplate;

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "List Audits", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )})
    public String retrieveAuditEntries(@Context final UriInfo uriInfo,
            @QueryParam("actionName") @Parameter(description = "actionName") final String actionName,
            @QueryParam("entityName") @Parameter(description = "entityName") final String entityName,
            @QueryParam("resourceId") @Parameter(description = "resourceId") final Long resourceId,
            @QueryParam("makerId") @Parameter(description = "makerId") final Long makerId,
            @QueryParam("makerDateTimeFrom") @Parameter(description = "makerDateTimeFrom") final String makerDateTimeFrom,
            @QueryParam("makerDateTimeTo") @Parameter(description = "makerDateTimeTo") final String makerDateTimeTo,
            @QueryParam("checkerId") @Parameter(description = "checkerId") final Long checkerId,
            @QueryParam("checkerDateTimeFrom") @Parameter(description = "checkerDateTimeFrom") final String checkerDateTimeFrom,
            @QueryParam("checkerDateTimeTo") @Parameter(description = "checkerDateTimeTo") final String checkerDateTimeTo,
            @QueryParam("processingResult") @Parameter(description = "processingResult") final Integer processingResult,
            @QueryParam("officeId") @Parameter(description = "officeId") final Integer officeId,
            @QueryParam("groupId") @Parameter(description = "groupId") final Integer groupId,
            @QueryParam("clientId") @Parameter(description = "clientId") final Integer clientId,
            @QueryParam("loanid") @Parameter(description = "loanid") final Integer loanId,
            @QueryParam("savingsAccountId") @Parameter(description = "savingsAccountId") final Integer savingsAccountId,
            @QueryParam("paged") @Parameter(description = "paged") final Boolean paged,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final SQLBuilder extraCriteria = getExtraCriteria(actionName, entityName, resourceId, makerId, makerDateTimeFrom, makerDateTimeTo,
                checkerId, checkerDateTimeFrom, checkerDateTimeTo, processingResult, officeId, groupId, clientId, loanId, savingsAccountId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (parameters.isPaged()) {
            final Page<AuditData> auditEntries = this.auditReadPlatformService.retrievePaginatedAuditEntries(extraCriteria,
                    settings.isIncludeJson(), parameters);
            return this.toApiJsonSerializer.serialize(settings, auditEntries, RESPONSE_DATA_PARAMETERS);
        }

        final Collection<AuditData> auditEntries = this.auditReadPlatformService.retrieveAuditEntries(extraCriteria,
                settings.isIncludeJson());

        return this.toApiJsonSerializer.serialize(settings, auditEntries, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("/template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Operation(summary = "Audit Search Template", description = "")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"
        )
    })
    public String retrieveAuditSearchTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final AuditSearchData auditSearchData = this.auditReadPlatformService.retrieveSearchTemplate("audit");

        final Set<String> RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE = new HashSet<>(
                Arrays.asList("offices", "actionNames", "entityNames", "processingResults"));

        return this.toApiJsonSerializerSearchTemplate.serialize(settings, auditSearchData, RESPONSE_DATA_PARAMETERS_SEARCH_TEMPLATE);
    }

    private SQLBuilder getExtraCriteria(final String actionName, final String entityName, final Long resourceId, final Long makerId,
            final String makerDateTimeFrom, final String makerDateTimeTo, final Long checkerId, final String checkerDateTimeFrom,
            final String checkerDateTimeTo, final Integer processingResult, final Integer officeId, final Integer groupId,
            final Integer clientId, final Integer loanId, final Integer savingsAccountId) {

        SQLBuilder extraCriteria = new SQLBuilder();
        extraCriteria.addNonNullCriteria("aud.action_name = ", actionName);
        if (entityName != null) {
            extraCriteria.addCriteria("aud.entity_name like", entityName + "%");
        }
        extraCriteria.addNonNullCriteria("aud.resource_id = ", resourceId);
        extraCriteria.addNonNullCriteria("aud.maker_id = ", makerId);
        extraCriteria.addNonNullCriteria("aud.checker_id = ", checkerId);
        extraCriteria.addNonNullCriteria("aud.made_on_date >= ", makerDateTimeFrom);
        extraCriteria.addNonNullCriteria("aud.made_on_date <= ", makerDateTimeTo);
        extraCriteria.addNonNullCriteria("aud.checked_on_date >= ", checkerDateTimeFrom);
        extraCriteria.addNonNullCriteria("aud.checked_on_date <= ", checkerDateTimeTo);
        extraCriteria.addNonNullCriteria("aud.processing_result_enum = ", processingResult);
        extraCriteria.addNonNullCriteria("aud.office_id = ", officeId);
        extraCriteria.addNonNullCriteria("aud.group_id = ", groupId);
        extraCriteria.addNonNullCriteria("aud.client_id = ", clientId);
        extraCriteria.addNonNullCriteria("aud.loan_id = ", loanId);
        extraCriteria.addNonNullCriteria("aud.savings_account_id = ", savingsAccountId);

        return extraCriteria;
    }
}
