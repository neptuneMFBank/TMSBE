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
package org.apache.fineract.portfolio.client.service.business;

import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.amountParameterName;
import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.countParameterName;
import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.ledgerAmountParameterName;
import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.statusParameterName;
import static org.apache.fineract.portfolio.client.data.business.ClientBusinessApiCollectionConstants.totalOverdueDerivedParameterName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.data.business.CodeValueBusinessData;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.codes.service.business.CodeValueBusinessReadPlatformService;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.infrastructure.documentmanagement.service.business.DocumentConfigReadPlatformService;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.address.data.business.AddressBusinessData;
import org.apache.fineract.portfolio.address.service.business.AddressBusinessReadPlatformService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;
import org.apache.fineract.portfolio.client.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
import org.apache.fineract.portfolio.client.data.ClientNonPersonData;
import org.apache.fineract.portfolio.client.data.ClientTimelineData;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessData;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessDataValidator;
import org.apache.fineract.portfolio.client.data.business.KycBusinessData;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientNotFoundException;
import org.apache.fineract.portfolio.client.service.ClientFamilyMembersReadPlatformService;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagementRepositoryWrapper;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionData;
import org.apache.fineract.portfolio.loanaccount.service.business.LoanBusinessReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountTransactionData;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.business.SavingsAccountBusinessReadPlatformService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientBusinessReadPlatformServiceImpl implements ClientBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final LoanBusinessReadPlatformService loanBusinessReadPlatformService;
    private final SavingsAccountBusinessReadPlatformService savingsAccountBusinessReadPlatformService;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final CodeValueBusinessReadPlatformService codeValueBusinessReadPlatformService;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    // data mappers
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ClientMapper clientMapper = new ClientMapper();
    private final ClientBusinessMapper clientBusinessMapper = new ClientBusinessMapper();
    private final ParentGroupsMapper clientGroupsMapper = new ParentGroupsMapper();
    private final ClientLookupMapper lookupMapper = new ClientLookupMapper();
    private final ClientPendingActivationMapper clientPendingActivationMapper = new ClientPendingActivationMapper();
    private final ClientLookupKycLevelMapper clientLookupKycLevelMapper = new ClientLookupKycLevelMapper();

    private final AddressBusinessReadPlatformService addressReadPlatformService;
    private final ClientFamilyMembersReadPlatformService clientFamilyMembersReadPlatformService;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final ColumnValidator columnValidator;
    private final ClientCollateralManagementRepositoryWrapper clientCollateralManagementRepositoryWrapper;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;

    private final DocumentConfigReadPlatformService documentConfigReadPlatformService;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;

    private final ClientBusinessDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromJsonHelper;

    private final LoanActiveSummaryMapper loanActiveSummaryMapper = new LoanActiveSummaryMapper();
    private final SavingsActiveSummaryMapper savingActiveSummaryMapper = new SavingsActiveSummaryMapper();

    @Value("${savings.product.reconcile.id}")
    private int savingsProductId;

    private Long defaultToUsersOfficeIfNull(final Long officeId) {
        Long defaultOfficeId = officeId;
        if (defaultOfficeId == null) {
            defaultOfficeId = this.context.authenticatedUser().getOffice().getId();
        }
        return defaultOfficeId;
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.QUEUE_SELF_CLIENT_ACTIVATE)
    public void queueSelfClientActivate() {
        final String sqlFinder = "select msac.client_id clientId from m_self_activate_client_view msac ";
        List<Long> selfActivateClient = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        log.info("queueSelfClientActivate start");
        for (Long selfActivateClientId : selfActivateClient) {
            String clientUpdateSql = "UPDATE m_client SET status_enum=?, activation_date=CURRENT_TIMESTAMP WHERE id=?";
            jdbcTemplate.update(clientUpdateSql, ClientStatus.ACTIVE.getValue(), selfActivateClientId);
        }
        log.info("{}: Records affected by queueSelfClientActivate: {}", ThreadLocalContextUtil.getTenant().getName(),
                selfActivateClient.size());
    }

    @Override
    public ClientBusinessData retrieveOne(final Long clientId, final boolean showTemplate, final Boolean staffInSelectedOfficeOnly) {
        this.context.authenticatedUser();
        try {
            // final String hierarchy = this.context.officeHierarchy();
            // final String hierarchySearchString = hierarchy + "%";

            final String sql = "select " + this.clientBusinessMapper.schema()
            // + " where ( o.hierarchy like ? or transferToOffice.hierarchy like ?) and c.id = ?";
                    + " where c.id = ?";
            ClientBusinessData clientData = this.jdbcTemplate.queryForObject(sql, this.clientBusinessMapper, // NOSONAR
                    // hierarchySearchString, hierarchySearchString,
                    clientId);

            // Get client collaterals
            final Collection<ClientCollateralManagement> clientCollateralManagements = this.clientCollateralManagementRepositoryWrapper
                    .getCollateralsPerClient(clientId);
            final Set<ClientCollateralManagementData> clientCollateralManagementDataSet = new HashSet<>();

            // Map to client collateral data class
            for (ClientCollateralManagement clientCollateralManagement : clientCollateralManagements) {
                BigDecimal total = clientCollateralManagement.getTotal();
                BigDecimal totalCollateral = clientCollateralManagement.getTotalCollateral(total);
                clientCollateralManagementDataSet
                        .add(ClientCollateralManagementData.setCollateralValues(clientCollateralManagement, total, totalCollateral));
            }

            if (clientData != null && showTemplate) {
                final Integer legalFormid = clientData.getLegalForm() != null ? clientData.getLegalForm().getId().intValue() : null;
                final ClientBusinessData templateData = this.retrieveTemplate(clientData.officeId(), staffInSelectedOfficeOnly,
                        legalFormid);
                clientData = ClientBusinessData.templateOnTop(clientData, templateData);
                Collection<SavingsAccountData> savingAccountOptions = this.savingsAccountReadPlatformService.retrieveForLookup(clientId,
                        null);
                if (savingAccountOptions != null && !savingAccountOptions.isEmpty()) {
                    clientData = ClientBusinessData.templateWithSavingAccountOptions(clientData, savingAccountOptions);
                }
            }

            final String clientGroupsSql = "select " + this.clientGroupsMapper.parentGroupsSchema();

            final Collection<GroupGeneralData> parentGroups = this.jdbcTemplate.query(clientGroupsSql, this.clientGroupsMapper, // NOSONAR
                    clientId);

            return ClientBusinessData.setParentGroups(clientData, parentGroups, clientCollateralManagementDataSet);

        } catch (final EmptyResultDataAccessException e) {
            throw new ClientNotFoundException(clientId, e);
        }
    }

    @Override
    public ClientBusinessData retrieveTemplate(final Long officeId, final Boolean staffInSelectedOfficeOnly, final Integer legalFormId) {
        this.context.authenticatedUser();

        final Long defaultOfficeId = defaultToUsersOfficeIfNull(officeId);
        AddressBusinessData address = null;

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();

        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookupByType(null);

        final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
                .retrieveGlobalConfiguration("Enable-Address");

        final Boolean isAddressEnabled = configuration.isEnabled();
        // if (isAddressEnabled) {
        address = this.addressReadPlatformService.retrieveTemplate();
        // }

        final ClientFamilyMembersData familyMemberOptions = this.clientFamilyMembersReadPlatformService.retrieveTemplate();

        Collection<StaffData> staffOptions = null;

        final boolean loanOfficersOnly = false;
        if (staffInSelectedOfficeOnly != null) {
            if (staffInSelectedOfficeOnly) {
                staffOptions = this.staffReadPlatformService.retrieveAllStaffForDropdown(defaultOfficeId);
            } else {
                staffOptions = this.staffReadPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(defaultOfficeId,
                        loanOfficersOnly);
            }
        }
        if (CollectionUtils.isEmpty(staffOptions)) {
            staffOptions = null;
        }
        final List<CodeValueData> genderOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.GENDER));

        final List<CodeValueData> clientTypeOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_TYPE));

        final List<CodeValueData> clientClassificationOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_CLASSIFICATION));

        final List<CodeValueData> clientNonPersonConstitutionOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION));

        final List<CodeValueData> clientNonPersonMainBusinessLineOptions = new ArrayList<>(
                this.codeValueReadPlatformService.retrieveCodeValuesByCode(ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE));

        final List<CodeValueBusinessData> activationChannelOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.ActivationChannelPARAM));
        final List<CodeValueBusinessData> bankAccountTypeOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.bankAccountTypePARAM));
        final List<CodeValueBusinessData> bankOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.bankPARAM));
        final List<CodeValueBusinessData> salaryRangeOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.salaryRangePARAM));
        final List<CodeValueBusinessData> employmentTypeOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.employmentTypePARAM));
        final List<CodeValueBusinessData> titleOptions = new ArrayList<>(
                this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.TitlePARAM));
        // List<CodeValueBusinessData> industryOptions = null;

        DocumentConfigData documentConfigData = null;
        if (legalFormId != null) {
            documentConfigData = this.documentConfigReadPlatformService.retrieveDocumentConfigViaClientLegalForm(legalFormId);
            // if (legalFormId > 1) {
            // industryOptions = new ArrayList<>(
            // this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.IndustryPARAM));
            // }
        }

        // final List<CodeValueBusinessData> countryValuesOptions = new ArrayList<>(
        // this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.COUNTRYPARAM));
        // final List<CodeValueBusinessData> stateValuesOptions = new ArrayList<>(
        // this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.STATEPARAM));
        // final List<CodeValueBusinessData> lgaValuesOptions = new ArrayList<>(
        // this.codeValueBusinessReadPlatformService.retrieveCodeValuesByCode(ClientBusinessApiConstants.LGAPARAM));
        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());

        List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.CLIENT.getName(), null);
        if (datatableTemplates == null) {
            datatableTemplates = this.readWriteNonCoreDataService.retrieveDatatableNames(EntityTables.CLIENT.getName());
        }
        // legalFormId => (to pick the documentType to be used)
        return ClientBusinessData.template(defaultOfficeId, LocalDate.now(DateUtils.getDateTimeZoneOfTenant()), offices, staffOptions, null,
                genderOptions, savingsProductDatas, clientTypeOptions, clientClassificationOptions, clientNonPersonConstitutionOptions,
                clientNonPersonMainBusinessLineOptions, clientLegalFormOptions, familyMemberOptions,
                new ArrayList<>(Arrays.asList(address)), isAddressEnabled, datatableTemplates // ,countryValuesOptions,
                // stateValuesOptions
                // , lgaValuesOptions
                , activationChannelOptions, bankAccountTypeOptions, bankOptions, salaryRangeOptions, employmentTypeOptions,
                documentConfigData, titleOptions
        // , industryOptions
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientData> retrieveAll(final SearchParametersBusiness searchParameters) {

        if (searchParameters != null && searchParameters.getStatus() != null
                && ClientStatus.fromString(searchParameters.getStatus()) == ClientStatus.INVALID) {
            final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
            final String defaultUserMessage = "The Status value '" + searchParameters.getStatus() + "' is not supported.";
            final ApiParameterError error = ApiParameterError.parameterError("validation.msg.client.status.value.is.not.supported",
                    defaultUserMessage, "status", searchParameters.getStatus());
            dataValidationErrors.add(error);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }

        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.clientMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        // log.info("clientRetrieveAl: {}-{}", sqlBuilder.toString(), ArrayUtils.toString(paramList.toArray()));
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.clientMapper);
    }

    private String buildSqlStringFromClientCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {
        final String nin = searchParameters.getUsername();
        final String bvn = searchParameters.getBvn();
        final Integer statusId = searchParameters.getStatusId();
        final Integer legalFormId = searchParameters.getLegalFormId();
        final Long officeId = searchParameters.getOfficeId();
        final Long staffId = searchParameters.getStaffId();
        final String externalId = searchParameters.getExternalId();
        final String displayName = searchParameters.getName();
        final String accountNo = searchParameters.getAccountNo();
        final String mobile = searchParameters.getMobile();
        final String email = searchParameters.getEmail();

        // officeId, externalId, statusId, hierarchy, offset,
        // limit, orderBy, sortOrder, staffId,
        // accountNo, fromDate, toDate, displayName, orphansOnly, isSelfUser
        String extraCriteria = "";

        if (searchParameters.isSelfUser()) {
            final String appUserID = String.valueOf(context.authenticatedUser().getId());
            extraCriteria += " and c.id in (select umap.client_id from m_selfservice_user_client_mapping as umap where umap.appuser_id = ? ) ";
            paramList.add(appUserID);
        }

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(c.submittedon_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(c.submittedon_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(c.submittedon_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isBvnPassed()) {
            paramList.add(bvn);
            extraCriteria += " and slk.bvn = ? ";
        }
        if (searchParameters.isUsernamePassed()) {
            paramList.add(nin);
            extraCriteria += " and slk.nin = ? ";
        }
        if (officeId != null) {
            extraCriteria += " and c.office_id = ? ";
            paramList.add(officeId);
        }

        if (externalId != null) {
            paramList.add("%" + externalId + "%");
            extraCriteria += " and c.external_id like ? ";
        }

        if (searchParameters.isLegalFormIdPassed()) {
            paramList.add(legalFormId);
            extraCriteria += " and c.legal_form_enum = ? ";
        }

        if (searchParameters.isAccountNoPassed()) {
            paramList.add(accountNo);
            extraCriteria += " and c.account_no = ? ";
        }

        if (displayName != null) {
            final String displayNameFinal = StringUtils.lowerCase(displayName);
            paramList.add("%" + displayNameFinal + "%");
            extraCriteria += " and LOWER(c.display_name) like ? ";
        }

        if (searchParameters.isStatusIdPassed()) {
            extraCriteria += " and c.status_enum = ? ";
            paramList.add(statusId);
        }
        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += " and c.staff_id = ? ";
            paramList.add(staffId);
        }
        if (searchParameters.isMobilePassed()) {
            extraCriteria += " and c.mobile_no = ? ";
            paramList.add(mobile);
        }
        if (searchParameters.isEmailPassed()) {
            extraCriteria += " and c.email_address = ? ";
            paramList.add(email);
        }

        if (searchParameters.isScopedByOfficeHierarchy()) {
            paramList.add(searchParameters.getHierarchy() + "%");
            extraCriteria += " and o.hierarchy like ? ";
        }

        if (searchParameters.isOrphansOnly()) {
            extraCriteria += " and c.id NOT IN (select client_id from m_group_client) ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    @Override
    public ClientData findClient(String apiRequestBodyAsJson) {
        this.context.authenticatedUser();
        this.fromApiJsonDeserializer.validateFindClient(apiRequestBodyAsJson);
        final JsonElement jsonElement = this.fromJsonHelper.parse(apiRequestBodyAsJson);
        try {
            final String key = this.fromJsonHelper.extractStringNamed(ClientBusinessApiConstants.keyParam, jsonElement);
            final String value = this.fromJsonHelper.extractStringNamed(ClientBusinessApiConstants.valueParam, jsonElement);

            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.lookupMapper.schema());
            sqlBuilder.append(" WHERE ");
            if (StringUtils.equalsIgnoreCase(key, "bvn") || StringUtils.equalsIgnoreCase(key, "nin")) {
                sqlBuilder.append(" slk.");
            } else {
                sqlBuilder.append(" c.");
            }
            sqlBuilder.append(key);
            sqlBuilder.append("=");
            sqlBuilder.append("'");
            sqlBuilder.append(value);
            sqlBuilder.append("'");
            String sql = sqlBuilder.toString();
            log.info("findClient: {}", sql);

            return this.jdbcTemplate.queryForObject(sql, this.lookupMapper);

        } catch (Exception e) {
            log.warn("findClient: {}", e);
            throw new ClientNotFoundException();
        }
    }

    @Override
    public KycBusinessData retrieveKycLevel(Long clientId) {
        this.context.authenticatedUser();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.clientLookupKycLevelMapper.schema());
            sqlBuilder.append(" WHERE mck.client_id=? ");

            String sql = sqlBuilder.toString();
            log.info("retrieveKycLevel: {}", sql);

            return this.jdbcTemplate.queryForObject(sql, this.clientLookupKycLevelMapper, clientId);

        } catch (Exception e) {
            log.warn("retrieveKycLevel: {}", e);
            throw new ClientNotFoundException();
        }
    }

    @Override
    public JsonObject retrieveBalance(Long clientId) {
        this.context.authenticatedUser();
        final JsonObject jsonObjectBalance = new JsonObject();
        // loans
        // loanActiveSummaryMapper savingActiveSummaryMapper fixedActiveSummaryMapper recurringActiveSummaryMapper
        // currentActiveSummaryMapper
        JsonObject jsonObjectLoan = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.loanActiveSummaryMapper.schema());
            sqlBuilder.append(" WHERE ml.client_id=? AND ml.loan_status_id=300 ");

            String sql = sqlBuilder.toString();
            jsonObjectLoan = this.jdbcTemplate.queryForObject(sql, this.loanActiveSummaryMapper, clientId);
            if (jsonObjectLoan != null && !jsonObjectLoan.isJsonNull()) {
                jsonObjectLoan.addProperty("name", "Loan");
            }
            jsonObjectBalance.add("loanAccount", jsonObjectLoan);
        } catch (DataAccessException e) {
            log.warn("retrieveBalance Loan: ", e);
            jsonObjectLoan.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("loanAccount", jsonObjectLoan);
        }
        // savings
        JsonObject jsonObjectSaving = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.savingActiveSummaryMapper.schema());
            sqlBuilder.append(" AND ms.product_id <> ").append(savingsProductId); // to remove reconciliation wallet
                                                                                  // summation
            // sqlBuilder.append(this.savingActiveSummaryMapper.savingsSchema());

            String sql = sqlBuilder.toString();
            jsonObjectSaving = this.jdbcTemplate.queryForObject(sql, this.savingActiveSummaryMapper, clientId,
                    DepositAccountType.SAVINGS_DEPOSIT.getValue());
            if (jsonObjectSaving != null && !jsonObjectSaving.isJsonNull()) {
                jsonObjectSaving.addProperty("name", "Savings");
            }
            jsonObjectBalance.add("savingDeposit", jsonObjectSaving);
        } catch (DataAccessException e) {
            log.warn("retrieveBalance savingDeposit: {}", e);
            jsonObjectSaving.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("savingDeposit", jsonObjectSaving);
        }
        // fixed
        JsonObject jsonObjectFixed = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.savingActiveSummaryMapper.schema());
            // sqlBuilder.append(this.savingActiveSummaryMapper.fixedSchema());

            String sql = sqlBuilder.toString();
            jsonObjectFixed = this.jdbcTemplate.queryForObject(sql, this.savingActiveSummaryMapper, clientId,
                    DepositAccountType.FIXED_DEPOSIT.getValue());
            if (jsonObjectFixed != null && !jsonObjectFixed.isJsonNull()) {
                jsonObjectFixed.addProperty("name", "Fixed Deposit");
            }
            jsonObjectBalance.add("fixedDeposit", jsonObjectFixed);
        } catch (DataAccessException e) {
            log.warn("retrieveBalance fixedDeposit: {}", e);
            jsonObjectFixed.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("fixedDeposit", jsonObjectFixed);
        }
        // recurring
        JsonObject jsonObjectRecurring = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.savingActiveSummaryMapper.schema());
            // sqlBuilder.append(this.savingActiveSummaryMapper.recurringSchema());

            String sql = sqlBuilder.toString();
            jsonObjectRecurring = this.jdbcTemplate.queryForObject(sql, this.savingActiveSummaryMapper, clientId,
                    DepositAccountType.RECURRING_DEPOSIT.getValue());
            if (jsonObjectRecurring != null && !jsonObjectRecurring.isJsonNull()) {
                jsonObjectRecurring.addProperty("name", "Recurring Deposit");
            }
            jsonObjectBalance.add("recurringDeposit", jsonObjectRecurring);
        } catch (DataAccessException e) {
            log.warn("retrieveBalance recurringDeposit: {}", e);
            jsonObjectRecurring.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("recurringDeposit", jsonObjectRecurring);
        }
        // current
        JsonObject jsonObjectCurrent = new JsonObject();
        try {
            final StringBuilder sqlBuilder = new StringBuilder(200);
            sqlBuilder.append("select ");
            sqlBuilder.append(this.savingActiveSummaryMapper.schema());
            // sqlBuilder.append(this.savingActiveSummaryMapper.currentSchema());

            String sql = sqlBuilder.toString();
            jsonObjectCurrent = this.jdbcTemplate.queryForObject(sql, this.savingActiveSummaryMapper, clientId,
                    DepositAccountType.CURRENT_DEPOSIT.getValue());
            if (jsonObjectCurrent != null && !jsonObjectCurrent.isJsonNull()) {
                jsonObjectCurrent.addProperty("name", "Current Deposit");
            }
            jsonObjectBalance.add("currentDeposit", jsonObjectCurrent);
        } catch (DataAccessException e) {
            log.warn("retrieveBalance currentDeposit: {}", e);
            jsonObjectCurrent.addProperty(statusParameterName, Boolean.FALSE);
            jsonObjectBalance.add("currentDeposit", jsonObjectCurrent);
        }
        return jsonObjectBalance;
    }

    @Override
    public KycBusinessData isClientExisting(String email, String mobileNo, String altMobileNo, String bvn, String nin, String tin) {
        Integer cnt = this.jdbcTemplate.queryForObject(
                "SELECT count(*) FROM client_unique_view WHERE email_address=? OR mobile_no=? OR alternateMobileNumber=? OR bvn=? OR nin=? OR tin=?  ",
                Integer.class, email, mobileNo, altMobileNo, bvn, nin, tin);
        Boolean agreement = cnt != null && cnt > 0;
        if (cnt != null && cnt > 1) {
            throw new ClientNotFoundException("error.msg.client.duplicate",
                    "Customer account is not profiled correctly or duplicates, please contact support");
        }
        Long clientId = null;
        if (agreement) {
            clientId = this.jdbcTemplate.queryForObject(
                    "SELECT id FROM client_unique_view WHERE email_address=? OR mobile_no=? OR alternateMobileNumber=? OR bvn=? OR nin=? OR tin=?  ",
                    Long.class, email, mobileNo, altMobileNo, bvn, nin, tin);
        }
        return new KycBusinessData(clientId, null, null, null, null, null, null, agreement, null, null);
    }

    private static final class ClientLookupKycLevelMapper implements RowMapper<KycBusinessData> {

        private final String schema;

        ClientLookupKycLevelMapper() {
            final StringBuilder builder = new StringBuilder(200);

            builder.append(
                    "mck.client_id,mck.has_personal,mck.has_residential,mck.has_employment,mck.has_agreement,mck.has_next_of_kin,mck.has_bank_detail,mck.has_identification,mck.has_directors,mck.has_social_media ");
            builder.append(" from m_client_kyc_checkers mck ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public KycBusinessData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long clientId = rs.getLong("client_id");

            final Boolean personal = rs.getBoolean("has_personal");
            final Boolean residential = rs.getBoolean("has_residential");
            final Boolean employment = rs.getBoolean("has_employment");
            final Boolean nextOfKin = rs.getBoolean("has_next_of_kin");
            final Boolean bankDetail = rs.getBoolean("has_bank_detail");
            final Boolean identification = rs.getBoolean("has_identification");
            final Boolean directors = rs.getBoolean("has_directors");
            final Boolean agreement = rs.getBoolean("has_agreement");
            final Boolean socialMedia = rs.getBoolean("has_social_media");

            return KycBusinessData.instance(clientId, personal, residential, employment, nextOfKin, bankDetail, identification, agreement,
                    directors, socialMedia);
        }
    }

    private static final class ClientLookupMapper implements RowMapper<ClientData> {

        private final String schema;

        ClientLookupMapper() {
            final StringBuilder builder = new StringBuilder(200);

            builder.append("c.id as id, c.display_name as displayName, ");
            builder.append("c.office_id as officeId, o.name as officeName ");
            builder.append("from m_client_view c ");
            builder.append("join m_office o on o.id = c.office_id ");
            builder.append("left join secondLevelKYC slk on slk.client_id = c.id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String displayName = rs.getString("displayName");
            final Long officeId = rs.getLong("officeId");
            final String officeName = rs.getString("officeName");

            return ClientData.lookup(id, displayName, officeId, officeName);
        }
    }

    private static final class ClientMapper implements RowMapper<ClientData> {

        private final String schema;

        ClientMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append("c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum, ");
            sqlBuilder.append(" c.office_id as officeId, o.name as officeName, ");
            sqlBuilder.append("c.fullname as fullname, c.display_name as displayName, ");
            sqlBuilder.append("c.mobile_no as mobileNo, ");
            sqlBuilder.append("c.is_staff as isStaff, ");
            sqlBuilder.append("c.email_address as emailAddress, ");
            sqlBuilder.append("c.client_classification_cv_id as classificationId, ");
            sqlBuilder.append("cvclassification.code_value as classificationValue, ");
            sqlBuilder.append("c.legal_form_enum as legalFormEnum, ");
            sqlBuilder.append("c.staff_id as staffId, s.display_name as staffName,");
            sqlBuilder.append("c.default_savings_account as savingsAccountId, ");

            sqlBuilder.append("c.submittedon_date as submittedOnDate, ");
            sqlBuilder.append("sbu.username as submittedByUsername ");

            sqlBuilder.append("from m_client_view c ");
            sqlBuilder.append("join m_office o on o.id = c.office_id ");
            sqlBuilder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            sqlBuilder.append("left join m_staff s on s.id = c.staff_id ");
            sqlBuilder.append("left join m_appuser sbu on sbu.id = c.created_by ");
            sqlBuilder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            sqlBuilder.append("left join secondLevelKYC slk on slk.client_id = c.id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isStaff = rs.getBoolean("isStaff");
            final String emailAddress = rs.getString("emailAddress");

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");

            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if (legalFormEnum != null) {
                legalForm = ClientEnumerations.legalForm(legalFormEnum);
            }

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, null, null, null, null, null,
                    null, null, null, null, null);

            return ClientData.instance(accountNo, status, null, officeId, officeName, null, null, id, null, null, null, fullname,
                    displayName, externalId, mobileNo, emailAddress, null, null, null, null, staffId, staffName, timeline, null, null,
                    savingsAccountId, null, classification, legalForm, null, isStaff);

        }

    }

    private static final class ClientBusinessMapper implements RowMapper<ClientBusinessData> {

        private final String schema;

        ClientBusinessMapper() {
            final StringBuilder builder = new StringBuilder(400);

            builder.append(
                    "c.id as id, c.account_no as accountNo, c.external_id as externalId, c.status_enum as statusEnum,c.sub_status as subStatus, ");
            builder.append(
                    "cvSubStatus.code_value as subStatusValue,cvSubStatus.code_description as subStatusDesc,c.office_id as officeId, o.name as officeName, ");
            builder.append("c.transfer_to_office_id as transferToOfficeId, transferToOffice.name as transferToOfficeName, ");
            builder.append("c.firstname as firstname, c.middlename as middlename, c.lastname as lastname, ");
            builder.append("c.fullname as fullname, c.display_name as displayName, ");
            builder.append("c.mobile_no as mobileNo, ");
            builder.append("c.is_staff as isStaff, ");
            builder.append("c.email_address as emailAddress, ");
            builder.append("c.date_of_birth as dateOfBirth, ");
            builder.append("c.gender_cv_id as genderId, ");
            builder.append("cv.code_value as genderValue, ");
            builder.append("c.client_type_cv_id as clienttypeId, ");
            builder.append("cvclienttype.code_value as clienttypeValue, ");
            builder.append("c.client_classification_cv_id as classificationId, ");
            builder.append("cvclassification.code_value as classificationValue, ");
            builder.append("c.legal_form_enum as legalFormEnum, ");

            builder.append("c.submittedon_date as submittedOnDate, ");
            builder.append("sbu.username as submittedByUsername, ");
            builder.append("sbu.firstname as submittedByFirstname, ");
            builder.append("sbu.lastname as submittedByLastname, ");

            builder.append("c.closedon_date as closedOnDate, ");
            builder.append("clu.username as closedByUsername, ");
            builder.append("clu.firstname as closedByFirstname, ");
            builder.append("clu.lastname as closedByLastname, ");

            // builder.append("c.submittedon as submittedOnDate, ");
            builder.append("acu.username as activatedByUsername, ");
            builder.append("acu.firstname as activatedByFirstname, ");
            builder.append("acu.lastname as activatedByLastname, ");

            builder.append("cnp.constitution_cv_id as constitutionId, ");
            builder.append("cvConstitution.code_value as constitutionValue, ");
            builder.append("cnp.incorp_no as incorpNo, ");
            builder.append("cnp.incorp_validity_till as incorpValidityTill, ");
            builder.append("cnp.main_business_line_cv_id as mainBusinessLineId, ");
            builder.append("cvMainBusinessLine.code_value as mainBusinessLineValue, ");
            builder.append("cnp.remarks as remarks, ");

            builder.append("c.activation_date as activationDate, c.image_id as imageId, ");
            builder.append("c.staff_id as staffId, s.display_name as staffName, ");
            builder.append("c.default_savings_product as savingsProductId, sp.name as savingsProductName, ");
            builder.append("c.default_savings_account as savingsAccountId ");
            builder.append("from m_client c ");
            builder.append("join m_office o on o.id = c.office_id ");
            builder.append("left join m_client_non_person cnp on cnp.client_id = c.id ");
            builder.append("left join m_staff s on s.id = c.staff_id ");
            builder.append("left join m_savings_product sp on sp.id = c.default_savings_product ");
            builder.append("left join m_office transferToOffice on transferToOffice.id = c.transfer_to_office_id ");
            builder.append("left join m_appuser sbu on sbu.id = c.created_by ");
            builder.append("left join m_appuser acu on acu.id = c.activatedon_userid ");
            builder.append("left join m_appuser clu on clu.id = c.closedon_userid ");
            builder.append("left join m_code_value cv on cv.id = c.gender_cv_id ");
            builder.append("left join m_code_value cvclienttype on cvclienttype.id = c.client_type_cv_id ");
            builder.append("left join m_code_value cvclassification on cvclassification.id = c.client_classification_cv_id ");
            builder.append("left join m_code_value cvSubStatus on cvSubStatus.id = c.sub_status ");
            builder.append("left join m_code_value cvConstitution on cvConstitution.id = cnp.constitution_cv_id ");
            builder.append("left join m_code_value cvMainBusinessLine on cvMainBusinessLine.id = cnp.main_business_line_cv_id ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientBusinessData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final String accountNo = rs.getString("accountNo");

            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = ClientEnumerations.status(statusEnum);

            final Long subStatusId = JdbcSupport.getLong(rs, "subStatus");
            final String subStatusValue = rs.getString("subStatusValue");
            final String subStatusDesc = rs.getString("subStatusDesc");
            final boolean isActive = false;
            final CodeValueData subStatus = CodeValueData.instance(subStatusId, subStatusValue, subStatusDesc, isActive);

            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");

            final Long transferToOfficeId = JdbcSupport.getLong(rs, "transferToOfficeId");
            final String transferToOfficeName = rs.getString("transferToOfficeName");

            final Long id = JdbcSupport.getLong(rs, "id");
            final String firstname = rs.getString("firstname");
            final String middlename = rs.getString("middlename");
            final String lastname = rs.getString("lastname");
            final String fullname = rs.getString("fullname");
            final String displayName = rs.getString("displayName");
            final String externalId = rs.getString("externalId");
            final String mobileNo = rs.getString("mobileNo");
            final boolean isStaff = rs.getBoolean("isStaff");
            final String emailAddress = rs.getString("emailAddress");
            final LocalDate dateOfBirth = JdbcSupport.getLocalDate(rs, "dateOfBirth");
            final Long genderId = JdbcSupport.getLong(rs, "genderId");
            final String genderValue = rs.getString("genderValue");
            final CodeValueData gender = CodeValueData.instance(genderId, genderValue);

            final Long clienttypeId = JdbcSupport.getLong(rs, "clienttypeId");
            final String clienttypeValue = rs.getString("clienttypeValue");
            final CodeValueData clienttype = CodeValueData.instance(clienttypeId, clienttypeValue);

            final Long classificationId = JdbcSupport.getLong(rs, "classificationId");
            final String classificationValue = rs.getString("classificationValue");
            final CodeValueData classification = CodeValueData.instance(classificationId, classificationValue);

            final LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
            final Long imageId = JdbcSupport.getLong(rs, "imageId");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final String staffName = rs.getString("staffName");

            final Long savingsProductId = JdbcSupport.getLong(rs, "savingsProductId");
            final String savingsProductName = rs.getString("savingsProductName");
            final Long savingsAccountId = JdbcSupport.getLong(rs, "savingsAccountId");

            final LocalDate closedOnDate = JdbcSupport.getLocalDate(rs, "closedOnDate");
            final String closedByUsername = rs.getString("closedByUsername");
            final String closedByFirstname = rs.getString("closedByFirstname");
            final String closedByLastname = rs.getString("closedByLastname");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedOnDate");
            final String submittedByUsername = rs.getString("submittedByUsername");
            final String submittedByFirstname = rs.getString("submittedByFirstname");
            final String submittedByLastname = rs.getString("submittedByLastname");

            final String activatedByUsername = rs.getString("activatedByUsername");
            final String activatedByFirstname = rs.getString("activatedByFirstname");
            final String activatedByLastname = rs.getString("activatedByLastname");

            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legalFormEnum");
            EnumOptionData legalForm = null;
            if (legalFormEnum != null) {
                legalForm = ClientEnumerations.legalForm(legalFormEnum);
            }

            final Long constitutionId = JdbcSupport.getLong(rs, "constitutionId");
            final String constitutionValue = rs.getString("constitutionValue");
            final CodeValueData constitution = CodeValueData.instance(constitutionId, constitutionValue);
            final String incorpNo = rs.getString("incorpNo");
            final LocalDate incorpValidityTill = JdbcSupport.getLocalDate(rs, "incorpValidityTill");
            final Long mainBusinessLineId = JdbcSupport.getLong(rs, "mainBusinessLineId");
            final String mainBusinessLineValue = rs.getString("mainBusinessLineValue");
            final CodeValueData mainBusinessLine = CodeValueData.instance(mainBusinessLineId, mainBusinessLineValue);
            final String remarks = rs.getString("remarks");

            final ClientNonPersonData clientNonPerson = new ClientNonPersonData(constitution, incorpNo, incorpValidityTill,
                    mainBusinessLine, remarks);

            final ClientTimelineData timeline = new ClientTimelineData(submittedOnDate, submittedByUsername, submittedByFirstname,
                    submittedByLastname, activationDate, activatedByUsername, activatedByFirstname, activatedByLastname, closedOnDate,
                    closedByUsername, closedByFirstname, closedByLastname);

            return ClientBusinessData.instance(accountNo, status, subStatus, officeId, officeName, transferToOfficeId, transferToOfficeName,
                    id, firstname, middlename, lastname, fullname, displayName, externalId, mobileNo, emailAddress, dateOfBirth, gender,
                    activationDate, imageId, staffId, staffName, timeline, savingsProductId, savingsProductName, savingsAccountId,
                    clienttype, classification, legalForm, clientNonPerson, isStaff);

        }
    }

    private static final class ParentGroupsMapper implements RowMapper<GroupGeneralData> {

        public String parentGroupsSchema() {
            return "gp.id As groupId , gp.account_no as accountNo, gp.display_name As groupName from m_client cl JOIN m_group_client gc ON cl.id = gc.client_id "
                    + "JOIN m_group gp ON gp.id = gc.group_id WHERE cl.id  = ?";
        }

        @Override
        public GroupGeneralData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long groupId = JdbcSupport.getLong(rs, "groupId");
            final String groupName = rs.getString("groupName");
            final String accountNo = rs.getString("accountNo");

            return GroupGeneralData.lookup(groupId, accountNo, groupName);
        }
    }

    @Override
    public Page<ClientBusinessData> retrievePendingActivation(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();

        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(" ");
        sqlBuilder.append(this.clientPendingActivationMapper.schema());

        if (searchParameters != null) {

            final String extraCriteria = buildSqlStringFromClientPendingActivationCriteria(searchParameters, paramList);

            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
        }
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(),
                this.clientPendingActivationMapper);
    }

    private String buildSqlStringFromClientPendingActivationCriteria(final SearchParametersBusiness searchParameters,
            List<Object> paramList) {

        final Integer legalFormId = searchParameters.getLegalFormId();
        final Long officeId = searchParameters.getOfficeId();
        final Long supervisorStaffId = searchParameters.getStaffId();
        final String bvn = searchParameters.getBvn();
        final String displayName = searchParameters.getName();
        final String accountNo = searchParameters.getAccountNo();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();
            // final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(cpa.submittedon_date AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(cpa.submittedon_date AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(cpa.submittedon_date AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (officeId != null) {
            extraCriteria += " and cpa.office_id = ? ";
            paramList.add(officeId);
        }

        if (searchParameters.isBvnPassed()) {
            paramList.add(bvn);
            extraCriteria += " and cpa.bvn = ? ";
        }

        if (searchParameters.isLegalFormIdPassed()) {
            paramList.add(legalFormId);
            extraCriteria += " and cpa.legal_form_enum = ? ";
        }

        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += " and cpa.organisational_role_parent_staff_id = ? ";
            paramList.add(supervisorStaffId);
        }

        if (searchParameters.isAccountNoPassed()) {
            paramList.add(accountNo);
            extraCriteria += " and cpa.account_no = ? ";
        }

        if (displayName != null) {
            paramList.add("%" + displayName + "%");
            extraCriteria += " and cpa.client_display_name like ? ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class ClientPendingActivationMapper implements RowMapper<ClientBusinessData> {

        private final String schema;

        ClientPendingActivationMapper() {
            final StringBuilder builder = new StringBuilder(200);

            builder.append(" cpa.id, cpa.account_no, cpa.client_display_name, cpa.legal_form_enum, cpa.submittedon_date,");
            builder.append(" cpa.office_id, cpa.office_name, cpa.staff_id, cpa.staff_display_name, ");
            builder.append(" cpa.organisational_role_parent_staff_id, cpa.organisational_role_parent_staff_display_name, ");
            builder.append(" cpa.bvn, cpa.iAgree ");
            builder.append(" from m_client_pending_activation cpa ");

            this.schema = builder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public ClientBusinessData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String accountNo = rs.getString("account_no");
            final String clientDisplayName = rs.getString("client_display_name");

            final LocalDate submittedOnDate = JdbcSupport.getLocalDate(rs, "submittedon_date");

            final Integer legalFormEnum = JdbcSupport.getInteger(rs, "legal_form_enum");
            EnumOptionData legalForm = null;
            if (legalFormEnum != null) {
                legalForm = ClientEnumerations.legalForm(legalFormEnum);
            }

            String officeName = null;
            Long officeId = rs.getLong("office_id");
            if (officeId > 0) {
                officeName = rs.getString("office_name");
            } else {
                officeId = null;
            }
            String staffDisplayName = null;
            Long staffId = rs.getLong("staff_id");
            if (staffId > 0) {
                staffDisplayName = rs.getString("staff_display_name");
            } else {
                staffId = null;
            }
            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaffId = rs.getLong("organisational_role_parent_staff_id");
            if (organisationalRoleParentStaffId > 0) {
                final String organisationalRoleParentStaffDisplayName = rs.getString("organisational_role_parent_staff_display_name");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaffId, organisationalRoleParentStaffDisplayName);
            }
            final String bvn = rs.getString("bvn");
            final Boolean iAgree = rs.getBoolean("iAgree");

            final ClientTimelineData clientTimelineData = new ClientTimelineData(submittedOnDate, null, null, null, null, null, null, null,
                    null, null, null, null);

            return ClientBusinessData.pendingActivation(accountNo, officeId, officeName, id, clientDisplayName, staffId, staffDisplayName,
                    clientTimelineData, legalForm, supervisorStaffData, bvn, iAgree);
        }
    }

    public static final class LoanActiveSummaryMapper implements RowMapper<JsonObject> {

        public String schema() {
            return " SUM(COALESCE(ml.total_outstanding_derived,0)) loanBalance, " + " COUNT(ml.id) AS totalCount "
                    + " FROM m_loan_view ml ";
            // return " SUM(CASE WHEN ml.loan_status_id=300 THEN COALESCE(ml.total_outstanding_derived,0) END)
            // loanBalance, "
            // + " COUNT(ml.loan_status_id=300) AS totalCount"
            // + " FROM m_loan_view ml WHERE ml.client_id=?";
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final BigDecimal totalLoanBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "loanBalance");
            final Long totalLoanCount = rs.getLong("totalCount");
            final JsonObject loanSummary = new JsonObject();
            loanSummary.addProperty(statusParameterName, Boolean.TRUE);
            loanSummary.addProperty(amountParameterName, totalLoanBalance);
            loanSummary.addProperty(countParameterName, totalLoanCount);
            return loanSummary;
        }
    }

    public static final class LoanPrincipalAmountSummaryMapper implements RowMapper<JsonObject> {

        public String schema() {
            return " SUM(COALESCE(ml.principal_amount,0)) principalAmount, "
                    + " SUM(COALESCE(mla.total_overdue_derived,0)) totalOverdueDerived, " + " COUNT(ml.id) AS totalCount "
                    + " FROM m_loan_view ml " + " LEFT JOIN m_loan_arrears_aging mla ON mla.loan_id=ml.id ";
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final BigDecimal totalOverdueDerived = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "totalOverdueDerived");
            final BigDecimal principalAmount = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "principalAmount");
            final Long totalLoanCount = rs.getLong("totalCount");
            final JsonObject loanSummary = new JsonObject();
            loanSummary.addProperty(statusParameterName, Boolean.TRUE);
            loanSummary.addProperty(amountParameterName, principalAmount);
            loanSummary.addProperty(totalOverdueDerivedParameterName, totalOverdueDerived);
            loanSummary.addProperty(countParameterName, totalLoanCount);
            return loanSummary;
        }
    }

    public static final class ClientCountSummaryMapper implements RowMapper<JsonObject> {

        public String schema() {
            return " COUNT(mcv.id) AS totalCount " + " FROM m_client_view mcv ";
        }

        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long totalCount = rs.getLong("totalCount");
            final JsonObject clientSummary = new JsonObject();
            clientSummary.addProperty(statusParameterName, Boolean.TRUE);
            clientSummary.addProperty(countParameterName, totalCount);
            return clientSummary;
        }
    }

    public static final class SavingsActiveSummaryMapper implements RowMapper<JsonObject> {

        public String schema() {
            return "  SUM(COALESCE(ms.available_balance,0)) availableBalance, SUM(COALESCE(ms.ledger_balance,0)) ledgerBalance, "
                    + " COUNT(ms.id) totalCount FROM m_saving_view ms "
                    + " WHERE ms.client_id=? AND ms.status_enum=300 AND ms.deposit_type_enum=? ";
        }

        // public String savingsSchema() {
        // return " SUM(CASE WHEN ms.deposit_type_enum=100 THEN COALESCE(ms.available_balance,0) END) availableBalance,
        // "
        // + " SUM(CASE WHEN ms.deposit_type_enum=100 THEN COALESCE(ms.ledger_balance,0) END) ledgerBalance, "
        // + " COUNT(ms.status_enum=300) AS totalCount"
        // + " FROM m_saving_view ms WHERE ms.client_id=?";
        // }
        //
        // public String fixedSchema() {
        // return " SUM(CASE WHEN ms.deposit_type_enum=200 THEN COALESCE(ms.available_balance,0) END) availableBalance,
        // "
        // + " SUM(CASE WHEN ms.deposit_type_enum=200 THEN COALESCE(ms.ledger_balance,0) END) ledgerBalance, "
        // + " COUNT(ms.status_enum=300) AS totalCount"
        // + " FROM m_saving_view ms WHERE ms.client_id=?";
        // }
        //
        // public String recurringSchema() {
        // return " SUM(CASE WHEN ms.deposit_type_enum=300 THEN COALESCE(ms.available_balance,0) END) availableBalance,
        // "
        // + " SUM(CASE WHEN ms.deposit_type_enum=300 THEN COALESCE(ms.ledger_balance,0) END) ledgerBalance, "
        // + " COUNT(ms.status_enum=300) AS totalCount"
        // + " FROM m_saving_view ms WHERE ms.client_id=?";
        // }
        //
        // public String currentSchema() {
        // return " SUM(CASE WHEN ms.deposit_type_enum=400 THEN COALESCE(ms.available_balance,0) END) availableBalance,
        // "
        // + " SUM(CASE WHEN ms.deposit_type_enum=400 THEN COALESCE(ms.ledger_balance,0) END) ledgerBalance, "
        // + " COUNT(ms.status_enum=300) AS totalCount"
        // + " FROM m_saving_view ms WHERE ms.client_id=?";
        // }
        @Override
        public JsonObject mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final BigDecimal availableBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "availableBalance");
            final BigDecimal ledgerBalance = JdbcSupport.getBigDecimalDefaultToZeroIfNull(rs, "ledgerBalance");
            final Long totalLoanCount = rs.getLong("totalCount");
            final JsonObject currentSummary = new JsonObject();
            currentSummary.addProperty(statusParameterName, Boolean.TRUE);
            currentSummary.addProperty(amountParameterName, availableBalance);
            currentSummary.addProperty(ledgerAmountParameterName, ledgerBalance);
            currentSummary.addProperty(countParameterName, totalLoanCount);
            return currentSummary;
        }
    }

    @Override
    public Collection<Long> retrieveMerchantClients(Long aUserID) {
        String sql = "SELECT  m.client_id FROM m_appuser_merchant_mapping m INNER JOIN m_client c ON c.id = m.client_id WHERE m.appuser_id = ?";
        return jdbcTemplate.queryForList(sql, Long.class, aUserID);
    }

    @Override
    public JsonObject retrieveClientTransactions(final Long clientId, SearchParametersBusiness searchParameters) {
        boolean showLoanTransactions = true;
        boolean showSavingTransactions = true;
        final JsonObject jsonObjectBalance = new JsonObject();
        if (searchParameters != null) {
            if (searchParameters.getTransactionTypeId() != null) {
                if (searchParameters.getTransactionTypeId().intValue() == GlobalEntityType.LOANS.getValue()) {
                    showSavingTransactions = false;
                } else if (searchParameters.getTransactionTypeId().intValue() == GlobalEntityType.SAVINGS_ACCOUNT.getValue()) {
                    showLoanTransactions = false;
                }
                searchParameters = SearchParametersBusiness.forTransactions(null, null, searchParameters.getOffset(),
                        searchParameters.getLimit(), searchParameters.getOrderBy(), searchParameters.getSortOrder(),
                        searchParameters.getFromDate(), searchParameters.getToDate(), searchParameters.getDepositTypeId());
            }
        }

        if (showLoanTransactions) {
            final Page<LoanTransactionData> loanTransactionData = this.loanBusinessReadPlatformService
                    .retrieveAllTransactionsByClientId(clientId, searchParameters);
            final String stringLoanTransactions = this.fromJsonHelper.toJson(loanTransactionData);
            final JsonElement jsonObjectLoanTransactions = this.fromJsonHelper.parse(stringLoanTransactions);
            jsonObjectBalance.add("loanTransactions", jsonObjectLoanTransactions);
        }
        if (showSavingTransactions) {
            final Page<SavingsAccountTransactionData> savingTransactionData = this.savingsAccountBusinessReadPlatformService
                    .retrieveAllTransactionsByClientId(clientId, searchParameters);
            final String stringSavingTransactions = this.fromJsonHelper.toJson(savingTransactionData);
            final JsonElement jsonObjectSavingTransactions = this.fromJsonHelper.parse(stringSavingTransactions);
            jsonObjectBalance.add("savingTransactions", jsonObjectSavingTransactions);
        }
        return jsonObjectBalance;
    }
}
