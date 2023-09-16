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
package org.apache.fineract.portfolio.business.employer.api;

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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.fineract.portfolio.business.employer.data.EmployerData;
import org.apache.fineract.portfolio.business.employer.service.EmployerReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.business.LoanBusinessApiConstants;
import org.springframework.stereotype.Component;

@Path("/employers")
@Component
@RequiredArgsConstructor
@Tag(name = "Employer", description = "This defines the employer model")
public class EmployerApiResource {

    private final PlatformSecurityContext securityContext;
    private final ToApiJsonSerializer<EmployerData> jsonSerializer;
    private final EmployerReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Employer Template", description = """
            """)
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // ClientsApiResourceSwagger.GetClientsTemplateResponse.class))
    ) })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(EmployerApiResourceConstants.RESOURCENAME);

        EmployerData employerData = this.readPlatformService.retrieveTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, employerData, EmployerApiResourceConstants.RESPONSE_TEMPLATE_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve all Employers", description = "Retrieve list of Employers")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(array = @ArraySchema(schema = @Schema(implementation =
    // EmployerApiResourceSwagger.GetEmployersResponse.class)))
    ) })
    public String getAllEmployers(@Context final UriInfo uriInfo,
            @QueryParam("supervisorId") @Parameter(description = "supervisorId") final Long supervisorId,
            @QueryParam("industryId") @Parameter(description = "industryId") final Long industryId,
            @QueryParam("classificationId") @Parameter(description = "classificationId") final Long classificationId,
            @QueryParam("name") @Parameter(description = "name") final String name,
            @QueryParam("active") @Parameter(description = "active") Boolean active,
            @QueryParam("startPeriod") @Parameter(description = "startPeriod") final DateParam startPeriod,
            @QueryParam("endPeriod") @Parameter(description = "endPeriod") final DateParam endPeriod,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @DefaultValue("me.id") @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @DefaultValue("desc") @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @DefaultValue("en") @QueryParam("locale") final String locale,
            @DefaultValue("yyyy-MM-dd") @QueryParam("dateFormat") final String dateFormat) {
        this.securityContext.authenticatedUser().validateHasReadPermission(EmployerApiResourceConstants.RESOURCENAME);

        LocalDate fromDate = null;
        if (startPeriod != null) {
            fromDate = startPeriod.getDate(LoanBusinessApiConstants.startPeriodParameterName, dateFormat, locale);
        }
        LocalDate toDate = null;
        if (endPeriod != null) {
            toDate = endPeriod.getDate(LoanBusinessApiConstants.endPeriodParameterName, dateFormat, locale);
        }

        final SearchParametersBusiness searchParameters = SearchParametersBusiness.forEmployer(active, offset, limit, orderBy, sortOrder,
                supervisorId, fromDate, toDate, name, industryId, classificationId);

        final Page<EmployerData> employerData = this.readPlatformService.retrieveAll(searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, employerData, EmployerApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{employerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve a Employer", description = "Retrieves a Employer")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // EmployerApiResourceSwagger.GetEmployersEmployerIdResponse.class))
    ) })
    public String retrieveOneEmployer(@PathParam("employerId") @Parameter(description = "employerId") final Long employerId,
            @Context final UriInfo uriInfo) {
        this.securityContext.authenticatedUser().validateHasReadPermission(EmployerApiResourceConstants.RESOURCENAME);
        final EmployerData Employers = this.readPlatformService.retrieveOne(employerId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.jsonSerializer.serialize(settings, Employers, EmployerApiResourceConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Employer", description = """
            Creates a new Employer

            Mandatory Fields: name, code

            Optional Fields: slug, active, parentId, mobileNo, contactPerson, emailAddress, emailExtension

            parentId should be passed when it is a referencing a branch employer details""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersResponse.class))
    ) })
    public String createEmployer(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createEmployer()//
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

    @POST
    @Path("{employerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Employer", description = """
            Creates a new Employer

            Mandatory Fields: name, code

            Optional Fields: slug, active, parentId, mobileNo, contactPerson, emailAddress, emailExtension

            parentId should be passed when it is a referencing a branch employer details""")
    @RequestBody(required = true
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation = EmployerApiResourceSwagger.PostEmployersResponse.class))
    ) })
    public String actions(@PathParam("employerId") @Parameter(description = "employerId") final Long employerId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        CommandWrapper commandRequest;
        if (is(commandParam, "activate")) {
            commandRequest = builder.activateEmployer(employerId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "close")) {
            commandRequest = builder.closeEmployer(employerId).build();
            result = this.commandWritePlatformService.logCommandSource(commandRequest);
        }

        if (result == null) {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate", "close" });
        }

        return this.jsonSerializer.serialize(result);
    }

    @PUT
    @Path("{employerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Employer", description = "Updates an Employer")
    @RequestBody(required = true
    // ,content = @Content(schema = @Schema(implementation =
    // EmployerApiResourceSwagger.PutEmployersEmployerIdRequest.class))
    )
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "OK"
    // , content = @Content(schema = @Schema(implementation =
    // EmployerApiResourceSwagger.PutEmployersEmployerIdResponse.class))
    ) })
    public String updateEmployer(@PathParam("employerId") @Parameter(description = "employerId") final Long employerId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateEmployer(employerId).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandWritePlatformService.logCommandSource(commandRequest);
        return this.jsonSerializer.serialize(result);
    }

}
