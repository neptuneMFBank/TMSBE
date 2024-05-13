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
package org.apache.fineract.portfolio.business.overdraft.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.accounting.journalentry.api.DateParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.business.metrics.service.MetricsReadPlatformService;
import org.apache.fineract.portfolio.business.overdraft.data.OverdraftData;
import org.apache.fineract.portfolio.business.overdraft.service.OverdraftReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Path("/overdraft")
@Component
@RequiredArgsConstructor
@Tag(name = "Overdraft", description = "This defines the Overdraft model")
public class OverdraftApiResource {

    private final PlatformSecurityContext securityContext;
    private final ToApiJsonSerializer<OverdraftData> jsonSerializer;
    private final OverdraftReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final MetricsReadPlatformService metricsReadPlatformService;

    @GET
    @Path("{overdraftId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a overdraft", description = "Returns the details of a overdraft. Example Requests: overdraft/1")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String retreiveOverdraft(@PathParam("overdraftId") @Parameter(description = "overdraftId") final Long overdraftId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(OverdraftApiResourceConstants.RESOURCENAME);

        final OverdraftData overdraftData = this.readPlatformService.retrieveOne(overdraftId);

        final Collection<MetricsData> metricsData = this.metricsReadPlatformService.retrieveOverdraftMetrics(overdraftId);
        if (!CollectionUtils.isEmpty(metricsData)) {
            overdraftData.setMetricsData(metricsData);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, overdraftData, OverdraftApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Overdraft", description = "Retrieve Overdraft")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(array = @ArraySchema(schema = @Schema(implementation =
    // MetricsApiResourceSwagger.GetMetricssResponse.class)))
    ) })
    public String retreiveAllOverdraft(@Context final UriInfo uriInfo,
            @QueryParam("savingsId") @Parameter(description = "savingsId") final Long savingsId,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.securityContext.authenticatedUser().validateHasReadPermission(OverdraftApiResourceConstants.RESOURCENAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forOverdraft(offset, limit, orderBy, sortOrder,
                savingsId, fromDate, toDate, statusId);

        final Page<OverdraftData> overdraftData = this.readPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, overdraftData, OverdraftApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Overdraft", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createOverdraft()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{overdraftId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update an Overdraft", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String update(@Parameter(description = "overdraftId") @PathParam("overdraftId") final Long overdraftId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateOverdraft(overdraftId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @POST
    @Path("/stop/{overdraftId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Stop an Overdraft", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String stop(@Parameter(description = "overdraftId") @PathParam("overdraftId") final Long overdraftId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .stopOverdraft(overdraftId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{overdraftId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Client", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String delete(@Parameter(description = "overdraftId") @PathParam("overdraftId") final Long overdraftId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteOverdraft(overdraftId) //
                .withNoJsonBody() //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

    @POST
    @Path("/submit/{overdraftId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Stop an Overdraft", description = "")
    @RequestBody(required = true)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK") })
    public String submitOverdraft(@Parameter(description = "overdraftId") @PathParam("overdraftId") final Long overdraftId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .submitOverdraft(overdraftId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.jsonSerializer.serialize(result);
    }

}
