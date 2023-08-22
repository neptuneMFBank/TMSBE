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
    private final boolean isSelfUser;

    private final LocalDate fromDate;
    private final LocalDate toDate;

    private final Integer legalFormId;
    private final Integer statusId;
    private final String email;
    private final String mobile;

    private final String type;

    private final Boolean active;

    public static SearchParametersBusiness forDocumentConfig(final String type, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final LocalDate fromDate, final LocalDate toDate, final String displayName,
            final Boolean active) {
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
        final String mobile = null;
        final Integer legalFormId = null;
        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active);
    }

    public static SearchParametersBusiness forLoansBusiness(final Long officeId, final String externalId, final Integer statusId,
            final String hierarchy, final Integer offset, final Integer limit, final String orderBy, final String sortOrder,
            final Long staffId, final String accountNo, final LocalDate fromDate, final LocalDate toDate) {

        final boolean isSelfUser = false;
        final Boolean orphansOnly = false;
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
        final String name = null;
        final String mobile = null;
        final String email = null;
        final Integer legalFormId = null;
        final String type = null;
        final Boolean active = false;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, name, hierarchy, firstname, lastname, offset, limit, orderBy,
                sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId, productId,
                provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active);
    }

    public static SearchParametersBusiness forClientsBusiness(final Long officeId, final String externalId, final Integer statusId,
            final String hierarchy, final Integer offset, final Integer limit, final String orderBy, final String sortOrder,
            final Long staffId, final String accountNo, final LocalDate fromDate, final LocalDate toDate, final String displayName,
            final Boolean orphansOnly, final boolean isSelfUser, final String email, final String mobile, final Integer legalFormId) {
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
        final String type = null;
        final Boolean active = false;

        return new SearchParametersBusiness(sqlSearch, officeId, externalId, displayName, hierarchy, firstname, lastname, offset, limit,
                orderBy, sortOrder, staffId, accountNo, loanId, savingsId, orphansOnly, isSelfUser, fromDate, toDate, status, categoryId,
                productId, provisioningEntryId, currencyCode, statusId, email, mobile, legalFormId, type, active);
    }

    private SearchParametersBusiness(final String sqlSearch, final Long officeId, final String externalId, final String name,
            final String hierarchy, final String firstname, final String lastname, final Integer offset, final Integer limit,
            final String orderBy, final String sortOrder, final Long staffId, final String accountNo, final Long loanId,
            final Long savingsId, final Boolean orphansOnly, boolean isSelfUser, final LocalDate fromDate, final LocalDate toDate,
            final String status, final Long categoryId, final Long productId, final Long provisioningEntryId, final String currencyCode,
            final Integer statusId, final String email, final String mobile, final Integer legalFormId, final String type,
            final Boolean active) {
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

    public Long getCategoryId() {
        return this.categoryId;
    }

    public boolean isCategoryIdPassed() {
        return this.categoryId != null && this.categoryId != 0;
    }

    public boolean isSelfUser() {
        return this.isSelfUser;
    }

    public Boolean getOrphansOnly() {
        return orphansOnly;
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

    public String getType() {
        return type;
    }

    public boolean isTypePassed() {
        return StringUtils.isNotBlank(this.type);
    }

    public Boolean isActive() {
        return active;
    }

    public boolean isActivePassed() {
        return active != null;
    }

}
