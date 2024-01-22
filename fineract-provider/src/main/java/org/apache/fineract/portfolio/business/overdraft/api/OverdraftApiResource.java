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

import static org.apache.fineract.simplifytech.data.GeneralConstants.is;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.metrics.api.MetricsApiResourceConstants;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.business.metrics.service.MetricsReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/overdraft")
@Component
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "This defines the metrics model")
public class OverdraftApiResource {

    private final PlatformSecurityContext securityContext;
    private final ToApiJsonSerializer<MetricsData> jsonSerializer;
    private final MetricsReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @GET
    @Path("loan")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve loan Metricss", description = "Retrieve loan Metricss")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(array = @ArraySchema(schema = @Schema(implementation =
    // MetricsApiResourceSwagger.GetMetricssResponse.class)))
    ) })
    public String getLoanMetrics(@Context final UriInfo uriInfo,
            @QueryParam("staffSupervisorId") @Parameter(description = "staffSupervisorId") final Long staffSupervisorId,
            @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("loanId") @Parameter(description = "loanId") final Long loanId,
            @QueryParam("productId") @Parameter(description = "productId") final Long productId,
            @QueryParam("statusId") @Parameter(description = "statusId") final Integer statusId,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("mm.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("asc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.securityContext.authenticatedUser().validateHasReadPermission(MetricsApiResourceConstants.RESOURCENAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forMetricsLoan(loanId, offset, limit, orderBy, sortOrder,
                productId, fromDate, toDate, officeId, staffId, staffSupervisorId);

        final Page<MetricsData> metricsData = this.readPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, metricsData, MetricsApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Path("loan/{metricsId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Actions on Loan Approval", description = """
            """)
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersResponse.class))
    ) })
    public String actions(@PathParam("metricsId") @Parameter(description = "metricsId") final Long metricsId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest;
        if (is(commandParam, "approve")) {
            commandRequest = builder.approveLoanMetrics(metricsId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undo")) {
            commandRequest = builder.undoLoanMetrics(metricsId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reject")) {
            commandRequest = builder.rejectLoanMetrics(metricsId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "assign")) {
            commandRequest = builder.assignLoanMetrics(metricsId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "approve", "undo", "reject", "assign" });
        }

        return this.jsonSerializer.serialize(result);
    }
}
