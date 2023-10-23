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
package org.apache.fineract.infrastructure.core.service.business;

import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;

public final class SearchParametersBusiness {

    private final String sqlSearch;
    private final Long officeId;
    private final Long clientId;
    private final String externalId;
    private final String name;
    private final String hierarchy;
    private final String firstname;
    private final String lastname;
    private final String status;
    private final Integer offset;
    private final Integer limit;
    private final String orderBy;
    private final String sortOrder;
    private final String accountNo;
    private final String currencyCode;

    private final Long staffId;

    private final Long loanId;

    private final Long savingsId;
    private final Boolean orphansOnly;

    // Provisning Entries Search Params
    private final Long provisioningEntryId;
    private final Long productId;
    private final Long categoryId;
    private final Boolean isSelfUser;

    private final LocalDate fromDate;
    private final LocalDate toDate;

    private final Integer legalFormId;
    private final Integer statusId;
    private final String email;
    private final String mobile;

    private final Integer type;

    private final Boolean active;

    private final Boolean showLoanProducts;
    private final Boolean showSavingsProducts;
    private final Long documentConfigId;

    private final String bvn;

    private final Long industryId;
    private final Long classificationId;
    private final Long organisationalRoleEnumId;
    private final String username;
    private final Integer depositTypeId;

    private final Long staffSupervisorId;
    private final Boolean isSupervisor;

    public static SearchParametersBusiness forMetricsLoan(final Long loanId, final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Long productId, final LocalDate fromDate, final LocalDate toDate,
            final Long officeId, final Long staffId, final Long staffSupervisorId) {
        final Long classificationId = null;
        final String accountNo = null;
        final Long clientId = null;
        final Integer statusId = null;
        final String externalId = null;
        final Integer depositTypeId = null;
        final Boolean active = null;
        final Long organisationalRoleEnumId = null;
        final Long industryId = null;
        final String displayName = null;
        final Boolean orphansOnly = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean isSelfUser = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;

        final String sqlSearch = null;

        final String hierarchy = null;
        final String username = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Boolean isSupervisor = null;
        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forDeposit(final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Long productId, final LocalDate fromDate, final LocalDate toDate,
            final Integer depositTypeId, final String accountNo, final Long officeId, final Integer statusId, final String externalId,
            final Long clientId, final String displayName, final Boolean orphansOnly) {
        final Long classificationId = null;
        final Boolean active = null;
        final Long organisationalRoleEnumId = null;
        final Long staffId = null;
        final Long industryId = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean isSelfUser = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;

        final String sqlSearch = null;

        final String hierarchy = null;
        final String username = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forLoanProductApproval(final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Long productId, final LocalDate fromDate, final LocalDate toDate, final String displayName) {
        final Long classificationId = null;
        final Boolean active = null;
        final Long organisationalRoleEnumId = null;
        final Long staffId = null;
        final Long industryId = null;
        final String accountNo = null;
        final Boolean orphansOnly = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean isSelfUser = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final Integer statusId = null;
        final String sqlSearch = null;
        final String externalId = null;
        final String hierarchy = null;
        final String username = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Long clientId = null;
        final Long officeId = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forUser(final Boolean active, final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final LocalDate fromDate, final LocalDate toDate, final Long officeId, final String username,
            final Boolean isSelfUser) {
        final Long organisationalRoleEnumId = null;
        final String displayName = null;
        final Long supervisorId = null;
        final Long industryId = null;
        final Long classificationId = null;
        final String accountNo = null;
        final Boolean orphansOnly = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final Integer statusId = null;
        final String sqlSearch = null;
        final String externalId = null;
        final String hierarchy = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Long clientId = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, supervisorId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status,
                categoryId, productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forStaff(final Boolean active, final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Long supervisorId, final LocalDate fromDate, final LocalDate toDate, final String displayName,
            final Long organisationalRoleEnumId, final Long officeId, final Boolean isSupervisor) {
        final Long industryId = null;
        final Long classificationId = null;
        final String accountNo = null;
        final Boolean orphansOnly = null;
        final Boolean isSelfUser = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final Integer statusId = null;
        final String sqlSearch = null;
        final String externalId = null;
        final String hierarchy = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Long clientId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, supervisorId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status,
                categoryId, productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forEmployer(final Boolean active, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final Long supervisorId, final LocalDate fromDate, final LocalDate toDate,
            final String displayName, final Long industryId, final Long classificationId) {
        final String accountNo = null;
        final Boolean orphansOnly = null;
        final Boolean isSelfUser = null;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long organisationalRoleEnumId = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final Integer statusId = null;
        final String sqlSearch = null;
        final String externalId = null;
        final String hierarchy = null;
        final Integer type = null;
        final Long documentConfigId = null;
        final Long clientId = null;
        final Long officeId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, supervisorId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status,
                categoryId, productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forClientPendingActivation(final LocalDate fromDate, final LocalDate toDate,
            final Integer legalFormId, final Long officeId, final Long supervisorStaffId, final String bvn, final String displayName,
            final String accountNo, final Integer offset, final Integer limit, final String orderBy, final String sortOrder) {

        final Long industryId = null;
        final Long organisationalRoleEnumId = null;
        final Long classificationId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long documentConfigId = null;
        final Integer type = null;
        final Boolean active = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final String sqlSearch = null;
        final String externalId = null;
        final Integer statusId = null;
        final String hierarchy = null;
        final Boolean orphansOnly = false;
        final Boolean isSelfUser = false;
        final String email = null;
        final String mobile = null;
        final Long clientId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, supervisorStaffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status,
                categoryId, productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, bvn, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forDocumentProductConfig(final Long documentConfigId, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final Boolean showLoanProducts, final Boolean showSavingsProducts) {
        final Long organisationalRoleEnumId = null;
        final Long industryId = null;
        final Long classificationId = null;
        final Integer type = null;
        final Boolean active = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final String sqlSearch = null;
        final Long officeId = null;
        final String externalId = null;
        final Integer statusId = null;
        final String hierarchy = null;
        final Long staffId = null;
        final String accountNo = null;
        final Boolean orphansOnly = false;
        final Boolean isSelfUser = false;
        final String email = null;
        final String displayName = null;
        final String mobile = null;
        final LocalDate toDate = null;
        final LocalDate fromDate = null;
        final Integer legalFormId = null;
        final Long clientId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forDocumentConfig(final Integer type, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final LocalDate fromDate, final LocalDate toDate, final String displayName,
            final Boolean active) {
        final Long organisationalRoleEnumId = null;
        final Long industryId = null;
        final Long classificationId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final Long documentConfigId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final String sqlSearch = null;
        final Long officeId = null;
        final String externalId = null;
        final Integer statusId = null;
        final String hierarchy = null;
        final Long staffId = null;
        final String accountNo = null;
        final Boolean orphansOnly = false;
        final Boolean isSelfUser = false;
        final String email = null;
        final String mobile = null;
        final Integer legalFormId = null;
        final Long clientId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forLoansBusiness(final Long clientId, final Long officeId, final String externalId,
            final Integer statusId, final String hierarchy, final Integer offset, final Integer limit, final String orderBy,
            final String sortOrder, final Long staffId, final String accountNo, final LocalDate fromDate, final LocalDate toDate) {

        final Long organisationalRoleEnumId = null;
        final Long industryId = null;
        final Long classificationId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Boolean isSelfUser = null;
        final Boolean orphansOnly = false;
        final Long savingsId = null;
        final Long documentConfigId = null;

        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final String sqlSearch = null;
        final String name = null;
        final String mobile = null;
        final String email = null;
        final Integer legalFormId = null;
        final Integer type = null;
        final Boolean active = false;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, name, hierarchy, firstname, lastname, offset, limit, orderBy,
                sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId, productId,
                provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId, showLoanProducts,
                showSavingsProducts, documentConfigId, null, industryId, classificationId, organisationalRoleEnumId, username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    public static SearchParametersBusiness forClientsBusiness(final Long officeId, final String externalId, final Integer statusId,
            final String hierarchy, final Integer offset, final Integer limit, final String orderBy, final String sortOrder,
            final Long staffId, final String accountNo, final LocalDate fromDate, final LocalDate toDate, final String displayName,
            final Boolean orphansOnly, final Boolean isSelfUser, final String email, final String mobile, final Integer legalFormId,
            final String bvn) {

        final Long industryId = null;
        final Long organisationalRoleEnumId = null;
        final Long classificationId = null;
        final Boolean showLoanProducts = null;
        final Boolean showSavingsProducts = null;
        final Long savingsId = null;
        final String status = null;
        final Long categoryId = null;
        final Long productId = null;
        final Long provisioningEntryId = null;
        final String currencyCode = null;
        final String firstname = null;
        final String lastname = null;
        final Long loanId = null;
        final String sqlSearch = null;
        final Integer type = null;
        final Boolean active = false;
        final Long clientId = null;
        final Long documentConfigId = null;
        final String username = null;
        final Integer depositTypeId = null;
        final Long staffSupervisorId = null;
        final Boolean isSupervisor = null;
        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active, clientId,
                showLoanProducts, showSavingsProducts, documentConfigId, bvn, industryId, classificationId, organisationalRoleEnumId,
                username, depositTypeId, staffSupervisorId, isSupervisor);
    }

    private SearchParametersBusiness(final String sqlSearch, final Long officeId, final String externalId, final String name,
            final String hierarchy, final String firstname, final String lastname, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final Long staffId, final String accountNo, final Long loanId,
            final Long savingsId, final Boolean orphansOnly, Boolean isSelfUser, final LocalDate fromDate, final LocalDate toDate,
            final String status, final Long categoryId, final Long productId, final Long provisioningEntryId, final String currencyCode,
            final Integer statusId, final String email, final String mobile, final Integer legalFormId, final Integer type,
            final Boolean active, final Long clientId, final Boolean showLoanProducts, final Boolean showSavingsProducts,
            final Long documentConfigId, final String bvn, final Long industryId, final Long classificationId,
            final Long organisationalRoleEnumId, final String username, final Integer depositTypeId, final Long staffSupervisorId, final Boolean isSupervisor) {
        this.isSupervisor = isSupervisor;
        this.username = username;
        this.depositTypeId = depositTypeId;
        this.industryId = industryId;
        this.organisationalRoleEnumId = organisationalRoleEnumId;
        this.classificationId = classificationId;
        this.sqlSearch = sqlSearch;
        this.officeId = officeId;
        this.externalId = externalId;
        this.name = name;
        this.hierarchy = hierarchy;
        this.firstname = firstname;
        this.lastname = lastname;
        this.offset = offset;
        this.limit = limit;
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
        this.staffId = staffId;
        this.accountNo = accountNo;
        this.loanId = loanId;
        this.savingsId = savingsId;
        this.orphansOnly = orphansOnly;
        this.currencyCode = currencyCode;
        this.provisioningEntryId = provisioningEntryId;
        this.productId = productId;
        this.categoryId = categoryId;
        this.isSelfUser = isSelfUser;
        this.status = status;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.statusId = statusId;
        this.email = email;
        this.mobile = mobile;
        this.legalFormId = legalFormId;
        this.type = type;
        this.active = active;
        this.clientId = clientId;
        this.showSavingsProducts = showSavingsProducts;
        this.showLoanProducts = showLoanProducts;
        this.documentConfigId = documentConfigId;
        this.bvn = bvn;
        this.staffSupervisorId = staffSupervisorId;
    }

    public boolean isOrderByRequested() {
        return StringUtils.isNotBlank(this.orderBy);
    }

    public boolean isSortOrderProvided() {
        return StringUtils.isNotBlank(this.sortOrder);
    }

    public static Integer getCheckedLimit(final Integer limit) {

        final Integer maxLimitAllowed = 200;
        // default to max limit first off
        Integer checkedLimit = maxLimitAllowed;

        if (limit != null && limit > 0) {
            checkedLimit = limit;
        } else if (limit != null) {
            // unlimited case: limit provided and 0 or less
            checkedLimit = null;
        }

        return checkedLimit;
    }

    public boolean isOfficeIdPassed() {
        return this.officeId != null && this.officeId != 0;
    }

    public boolean isCurrencyCodePassed() {
        return this.currencyCode != null;
    }

    public boolean isLimited() {
        return this.limit != null && this.limit > 0;
    }

    public boolean isOffset() {
        return this.offset != null;
    }

    public boolean isScopedByOfficeHierarchy() {
        return StringUtils.isNotBlank(this.hierarchy);
    }

    public String getSqlSearch() {
        return this.sqlSearch;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean isExternalIdPassed() {
        return StringUtils.isNotBlank(this.externalId);
    }

    public String getName() {
        return this.name;
    }

    public boolean isNamePassed() {
        return StringUtils.isNotBlank(this.name);
    }

    public String getHierarchy() {
        return this.hierarchy;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getOffset() {
        return this.offset;
    }

    public Integer getLimit() {
        return this.limit;
    }

    public String getOrderBy() {
        return this.orderBy;
    }

    public String getSortOrder() {
        return this.sortOrder;
    }

    public boolean isStaffIdPassed() {
        return this.staffId != null && this.staffId != 0;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getAccountNo() {
        return this.accountNo;
    }

    public boolean isAccountNoPassed() {
        return StringUtils.isNotBlank(this.accountNo);
    }

    public boolean isLoanIdPassed() {
        return this.loanId != null && this.loanId != 0;
    }

    public boolean isSavingsIdPassed() {
        return this.savingsId != null && this.savingsId != 0;
    }

    public Long getLoanId() {
        return this.loanId;
    }

    public Long getSavingsId() {
        return this.savingsId;
    }

    public Boolean isOrphansOnly() {
        if (this.orphansOnly != null) {
            return this.orphansOnly;
        }
        return false;
    }

    public Long getProvisioningEntryId() {
        return this.provisioningEntryId;
    }

    public boolean isProvisioningEntryIdPassed() {
        return this.provisioningEntryId != null && this.provisioningEntryId != 0;
    }

    public Long getProductId() {
        return this.productId;
    }

    public boolean isProductIdPassed() {
        return this.productId != null && this.productId != 0;
    }

    public Long getClientId() {
        return clientId;
    }

    public boolean isClientIdPassed() {
        return this.clientId != null && this.clientId != 0;
    }

    public Long getCategoryId() {
        return this.categoryId;
    }

    public boolean isCategoryIdPassed() {
        return this.categoryId != null && this.categoryId != 0;
    }

    public Boolean isSelfUser() {
        return this.isSelfUser;
    }

    public Boolean getOrphansOnly() {
        return orphansOnly;
    }

    public Boolean isSelfUserPassed() {
        return isSelfUser != null;
    }

    public boolean isIsSelfUser() {
        return isSelfUser;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public boolean isFromDatePassed() {
        return this.fromDate != null;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public boolean isToDatePassed() {
        return this.toDate != null;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public boolean isStatusIdPassed() {
        return this.statusId != null && this.statusId != 0;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailPassed() {
        return StringUtils.isNotBlank(this.email);
    }

    public String getMobile() {
        return mobile;
    }

    public boolean isMobilePassed() {
        return StringUtils.isNotBlank(this.mobile);
    }

    public Integer getLegalFormId() {
        return legalFormId;
    }

    public boolean isLegalFormIdPassed() {
        return this.legalFormId != null && this.legalFormId != 0;
    }

    public Integer getType() {
        return type;
    }

    public boolean isTypePassed() {
        return this.type != null && this.type != 0;
    }

    public Boolean isActive() {
        return active;
    }

    public boolean isActivePassed() {
        return active != null;
    }

    public Boolean getShowLoanProducts() {
        return showLoanProducts;
    }

    public boolean isShowLoanProductsPassed() {
        return showLoanProducts != null;
    }

    public boolean isShowSavingsProductsPassed() {
        return showSavingsProducts != null;
    }

    public Boolean getShowSavingsProducts() {
        return showSavingsProducts;
    }

    public Long getDocumentConfigId() {
        return documentConfigId;
    }

    public boolean isDocumentConfigIdPassed() {
        return this.documentConfigId != null && this.documentConfigId != 0;
    }

    public String getBvn() {
        return bvn;
    }

    public boolean isBvnPassed() {
        return StringUtils.isNotBlank(this.bvn);
    }

    public Long getIndustryId() {
        return industryId;
    }

    public boolean isIndustryIdPassed() {
        return this.industryId != null && this.industryId != 0;
    }

    public Long getClassificationId() {
        return classificationId;
    }

    public boolean isClassificationIdPassed() {
        return this.classificationId != null && this.classificationId != 0;
    }

    public Integer getDepositTypeId() {
        return depositTypeId;
    }

    public boolean isDepositTypeIdPassed() {
        return this.depositTypeId != null && this.depositTypeId != 0;
    }

    public Long getOrganisationalRoleEnumId() {
        return organisationalRoleEnumId;
    }

    public boolean isOrganisationalRoleEnumIdPassed() {
        return this.organisationalRoleEnumId != null && this.organisationalRoleEnumId != 0;
    }

    public String getUsername() {
        return username;
    }

    public boolean isUsernamePassed() {
        return StringUtils.isNotBlank(this.username);
    }

    public Long getStaffSupervisorId() {
        return staffSupervisorId;
    }

    public boolean isStaffSupervisorIdPassed() {
        return this.staffSupervisorId != null && this.staffSupervisorId != 0;
    }

    public Boolean getIsSupervisor() {
        return isSupervisor;
    }

    public boolean isSupervisorPassed() {
        return isSupervisor != null;
    }

    public boolean isOrphansOnlyPassed() {
        return orphansOnly != null;
    }
}
