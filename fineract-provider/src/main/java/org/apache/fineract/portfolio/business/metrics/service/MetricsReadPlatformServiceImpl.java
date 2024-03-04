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
package org.apache.fineract.portfolio.business.metrics.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.campaigns.email.data.EmailConfigurationValidator;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.EmailDetail;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.GmailBackedPlatformEmailService;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.jobs.annotation.CronTarget;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.business.metrics.domain.Metrics;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistory;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistoryRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.exception.MetricsNotFoundException;
import org.apache.fineract.portfolio.business.overdraft.domain.Overdraft;
import org.apache.fineract.portfolio.business.overdraft.domain.OverdraftRepositoryWrapper;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalConfigData;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalData;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductApprovalReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.simplifytech.data.GeneralConstants;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.apache.fineract.useradministration.service.business.AppUserBusinessReadPlatformService;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsReadPlatformServiceImpl implements MetricsReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final PaginationHelper paginationHelper;
    private final ColumnValidator columnValidator;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final MetricsMapper metricsMapper = new MetricsMapper();
    private final OverdraftRoleApprovalViewMapper overdraftRoleApprovalViewMapper = new OverdraftRoleApprovalViewMapper();
    private final MetricsLoanViewMapper metricsLoanViewMapper = new MetricsLoanViewMapper();
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final MetricsRepositoryWrapper metricsRepositoryWrapper;
    private final MetricsHistoryRepositoryWrapper metricsHistoryRepositoryWrapper;
    private final LoanProductApprovalReadPlatformService loanProductApprovalReadPlatformService;
    private final AppUserBusinessReadPlatformService appUserBusinessReadPlatformService;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final GmailBackedPlatformEmailService gmailBackedPlatformEmailService;
    private final EmailConfigurationValidator emailConfigurationValidator;
    private final AppUserRepositoryWrapper appUserRepositoryWrapper;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final OverdraftRepositoryWrapper overdraftRepositoryWrapper;

    private static final Long SUPER_USER_SERVICE_ROLE = 1L;

    private void createOverdraftMetrics(Long overdraftApprovalScheduleId) {
        boolean updateOverdraft = false;
        final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftApprovalScheduleId);
        final SavingsAccount savingsAccount = overdraft.getSavingsAccount();

        final Collection<LoanProductApprovalConfigData> overdraftApprovalConfigDatas = this.retrieveOverdraftRoleApprovalConfig();

        if (CollectionUtils.isEmpty(overdraftApprovalConfigDatas)) {
            // send a mail
            log.warn("No overdraft approval set for id {}", overdraftApprovalScheduleId);

            List<String> businessAddresses = getBusinessAddresses();
            if (!CollectionUtils.isEmpty(businessAddresses)) {
                final String subject = "Overdraft Configuration Setup";
                final String body = "No overdraft approval process configured";
                notificationToUsers(businessAddresses, subject, body);
            }
        } else {
            int nextRank = 0;
            for (LoanProductApprovalConfigData loanProductApprovalConfigData : overdraftApprovalConfigDatas) {
                int rank = nextRank++;
                int status = rank == 0 ? LoanApprovalStatus.PENDING.getValue() : LoanApprovalStatus.QUEUE.getValue();

                // isWithinRange
                final BigDecimal value = overdraft.getAmount();
                log.warn("createOverdraftMetrics value: {}", value);
                final BigDecimal minApprovalAmount = loanProductApprovalConfigData.getMinApprovalAmount() == null ? BigDecimal.ZERO
                        : loanProductApprovalConfigData.getMinApprovalAmount();
                log.warn("createOverdraftMetrics minApprovalAmount: {}", minApprovalAmount);
                final BigDecimal maxApprovalAmount = loanProductApprovalConfigData.getMaxApprovalAmount() == null ? BigDecimal.ZERO
                        : loanProductApprovalConfigData.getMaxApprovalAmount();
                log.warn("createOverdraftMetrics maxApprovalAmount: {}", maxApprovalAmount);
                final boolean isWithinRange = GeneralConstants.isWithinRange(value, minApprovalAmount, maxApprovalAmount);
                if (isWithinRange) {
                    // create loan movement approval if this condition is met
                    final RoleData roleData = loanProductApprovalConfigData.getRoleData();
                    final Long roleId = roleData.getId();
                    Collection<AppUserData> appUserDatas = this.appUserBusinessReadPlatformService.retrieveActiveAppUsersForRole(roleId);
                    if (CollectionUtils.isEmpty(appUserDatas)) {
                        // send a mail informing no user_staff assigned to role
                        log.warn("No user/staff assigned to role {} with id {} on overdraft approval config", roleData.getName(), roleId);

                        List<String> businessAddresses = getBusinessAddresses();
                        if (!CollectionUtils.isEmpty(businessAddresses)) {
                            final String subject = "Overdraft Configuration Setup";
                            final String body = String.format("No user/staff assigned to role `%s` ", roleData.getName());
                            notificationToUsers(businessAddresses, subject, body);
                        }
                        nextRank = nextRank > 0 ? nextRank-- : 0;
                    } else {
                        try {
                            final Staff staff = setAssingmentLoanApprovalCheck(appUserDatas);
                            if (ObjectUtils.isEmpty(staff)) {
                                nextRank = nextRank > 0 ? nextRank-- : 0;
                                continue;
                            }
                            final Metrics metrics = Metrics.createOverdraftMetrics(staff, status, rank, overdraft, savingsAccount);
                            this.metricsRepositoryWrapper.saveAndFlush(metrics);
                            final Long metricsId = metrics.getId();
                            if (metricsId != null) {
                                final MetricsHistory metricsHistory = MetricsHistory.instance(metrics, status);
                                this.metricsHistoryRepositoryWrapper.saveAndFlush(metricsHistory);
                            }
                            updateOverdraft = true;
                        } catch (Exception e) {
                            throw new MetricsNotFoundException("createOverdraftMetricsAssigningFailed: " + e);
                        }
                    }
                } else {
                    nextRank = nextRank > 0 ? nextRank-- : 0;
                    log.warn("Not withIn range for overdraftId: {}", overdraftApprovalScheduleId);
                }
            }
            if (updateOverdraft) {
                // update dataTable overdraft approvalCheck
                String overdraftApprovalCheckSql = "UPDATE m_overdraft ov SET ov.status_enum=? WHERE ov.id=?";
                jdbcTemplate.update(overdraftApprovalCheckSql, LoanApprovalStatus.PENDING.getValue(), overdraftApprovalScheduleId);
            }
        }
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.QUEUE_OVERDRAFT_APPROVAL_CHECKS)
    public void queueOverdraftApprovals() {
        final String sqlFinder = "select mosv.overdraft_id overdraftId from m_overdraft_schedule_view mosv ";
        List<Long> overdraftApprovalSchedule = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        for (Long overdraftApprovalScheduleId : overdraftApprovalSchedule) {
            createOverdraftMetrics(overdraftApprovalScheduleId);
        }
        log.info("{}: Records affected by queueOverdraftApprovals: {}", ThreadLocalContextUtil.getTenant().getName(),
                overdraftApprovalSchedule.size());
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.REMINDER_OVERDRAFT_APPROVAL_CHECKS)
    public void reminderOverdraftApprovals() {

        final String columnName = "mm.overdraft_id";
        final String subject = "Notification of Pending Overdraft Id `%s` Approval";
        final String body = "%s with mobile %s have an overdraft (`%s`) pending approval.%s";
        final String link = "/savings/overdraft/details?overdraftId=";
        final int type = 1;

        metricReminderProcess(columnName, type, link, subject, body);
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.REMINDER_LOAN_APPROVAL_CHECKS)
    public void reminderLoanApprovals() {

        final String columnName = "mm.loan_id";
        final String subject = "Notification of Pending Loan Id `%s` Approval";
        final String body = "%s with mobile %s have a loan (`%s`) pending approval.%s";
        final String link = "/loans/details?loanId=";
        final int type = 0;

        metricReminderProcess(columnName, type, link, subject, body);
    }

    protected void metricReminderProcess(final String columnName, final int type, final String link, final String subjectValue,
            final String bodyValue) throws DataAccessException {
        final String sql = "select " + metricsMapper.schema() + " WHERE " + columnName + " IS NOT NULL AND mm.status_enum=100 ";
        // + " WHERE mm.loan_id IS NOT NULL AND mm.status_enum=100 AND mm.created_on_utc >= DATE_SUB(NOW(), INTERVAL 24
        // HOUR) ";
        Collection<MetricsData> metricsDatas = this.jdbcTemplate.query(sql, metricsMapper);
        if (!CollectionUtils.isEmpty(metricsDatas)) {
            List<String> notifybusinessUsers;
            for (MetricsData metricsData : metricsDatas) {
                notifybusinessUsers = new ArrayList<>();
                Client client;
                Long transactionId;
                final String productName;
                if (type == 0) {
                    // check loan
                    final Long loanId = metricsData.getLoanId();
                    transactionId = loanId;
                    final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId);
                    client = loan.getClient();
                    final LoanProduct loanProduct = loan.getLoanProduct();
                    productName = loanProduct.getName();
                } else {
                    // check overdraft
                    final Long overdraftId = metricsData.getOverdraftId();
                    transactionId = overdraftId;
                    final Overdraft overdraft = this.overdraftRepositoryWrapper.findOneWithNotFoundDetection(overdraftId);
                    final SavingsAccount savingsAccount = overdraft.getSavingsAccount();
                    client = savingsAccount.getClient();
                    final SavingsProduct savingsProduct = savingsAccount.savingsProduct();
                    productName = savingsProduct.getName();
                }

                final String clientName = client.getDisplayName();
                final String mobileNo = StringUtils.defaultIfBlank(client.mobileNo(), "N/A");

                final StaffData staff = metricsData.getStaffData();
                final Long staffId = staff.getId();
                final AppUser appUser = this.appUserRepositoryWrapper.findFirstByStaffId(staffId);
                if (ObjectUtils.isNotEmpty(appUser)) {
                    getEmailAddress(appUser, notifybusinessUsers);
                }
                final StaffData organisationalRoleParentStaff = metricsData.getSupervisorStaffData();
                if (ObjectUtils.isNotEmpty(organisationalRoleParentStaff)) {
                    final Long organisationalRoleParentStaffId = organisationalRoleParentStaff.getId();
                    final AppUser appUserSupervisor = this.appUserRepositoryWrapper.findFirstByStaffId(organisationalRoleParentStaffId);
                    if (ObjectUtils.isNotEmpty(appUserSupervisor)) {
                        // set email of approval supervisor
                        getEmailAddress(appUserSupervisor, notifybusinessUsers);
                    }
                }

                if (!CollectionUtils.isEmpty(notifybusinessUsers)) {
                    String navigationUrl = "";
                    final GlobalConfigurationPropertyData appBaseUrl = this.configurationReadPlatformService
                            .retrieveGlobalConfiguration("app-base-url");
                    if (appBaseUrl.isEnabled() && StringUtils.isNotBlank(appBaseUrl.getStringValue())) {
                        StringBuilder navigationBuilder = new StringBuilder();
                        navigationBuilder.append("\n\nClick here: ");
                        navigationBuilder.append(appBaseUrl.getStringValue());
                        navigationBuilder.append(link);
                        navigationBuilder.append(transactionId);
                        navigationUrl = navigationBuilder.toString();
                    }

                    final String subject = String.format(subjectValue, transactionId);
                    final String body = String.format(bodyValue, clientName, mobileNo, productName, navigationUrl);
                    notificationToUsers(notifybusinessUsers, subject, body);
                }
            }
        }
    }

    @Override
    @Transactional
    @CronTarget(jobName = JobName.QUEUE_LOAN_APPROVAL_CHECKS)
    public void queueLoanApprovals() {
        final String sqlFinder = "select mlasv.loan_id loanId from m_loan_approval_schedule_view mlasv ";
        List<Long> loanApprovalSchedule = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        for (Long loanApprovalScheduleId : loanApprovalSchedule) {
            createLoanMetrics(loanApprovalScheduleId);
        }
        log.info("{}: Records affected by queueLoanApprovals: {}", ThreadLocalContextUtil.getTenant().getName(),
                loanApprovalSchedule.size());
    }

    @Override
    public Page<MetricsData> retrieveAll(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());
        sqlBuilder.append(metricsLoanViewMapper.schema());
        // sqlBuilder.append(metricsMapper.schema());

        if (searchParameters != null) {
            final String extraCriteria = buildSqlStringFromMetricsCriteria(searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" where (").append(extraCriteria).append(")");
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
                sqlBuilder.append(" limit ").append(searchParameters.getLimit());
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(" offset ").append(searchParameters.getOffset());
                }
            }
        }

        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(), this.metricsLoanViewMapper);
    }

    @Override
    public Collection<MetricsData> retrieveSavingsAccountMetrics(Long savingsAccountId) {
        this.context.authenticatedUser();
        final String sql = "select " + metricsMapper.schema() + " WHERE mm.savings_id = ? ORDER BY mm.rank ASC ";
        return this.jdbcTemplate.query(sql, metricsMapper, savingsAccountId);
    }

    @Override
    public Collection<MetricsData> retrieveLoanMetrics(Long loanId) {
        this.context.authenticatedUser();
        final String sql = "select " + metricsMapper.schema() + " WHERE mm.loan_id = ? ORDER BY mm.rank ASC ";
        return this.jdbcTemplate.query(sql, metricsMapper, loanId);
    }

    private void createLoanMetrics(Long loanApprovalScheduleId) {
        boolean updateLoan = false;
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanApprovalScheduleId);
        // final Client client = loan.getClient();
        // final String clientName = client.getDisplayName();
        // final String mobileNo = client.mobileNo();
        final LoanProduct loanProduct = loan.getLoanProduct();
        // final String loanProductName = loanProduct.getName();
        final Long loanProductId = loanProduct.getId();
        final LoanProductApprovalData loanProductApprovalData = this.loanProductApprovalReadPlatformService
                .retrieveOneViaLoanProduct(loanProductId);
        log.warn("createLoanMetrics loanProductApprovalData {}", loanProductApprovalData.getId());

        final Collection<LoanProductApprovalConfigData> loanProductApprovalConfigDatas = loanProductApprovalData
                .getLoanProductApprovalConfigData();

        if (CollectionUtils.isEmpty(loanProductApprovalConfigDatas)) {
            // send a mail
            log.warn("No loan approval set for loan product {} with id {}", loanProduct.getName(), loanProductId);

            List<String> businessAddresses = getBusinessAddresses();
            if (!CollectionUtils.isEmpty(businessAddresses)) {
                final String subject = "Configuration Setup";
                final String body = String.format("No loan approval process configured for loan product `%s` ", loanProduct.getName());
                notificationToUsers(businessAddresses, subject, body);
            }
        } else {
            // List<String> notifybusinessUsers = new ArrayList<>();

            int nextRank = 0;
            for (LoanProductApprovalConfigData loanProductApprovalConfigData : loanProductApprovalConfigDatas) {
                int rank = nextRank++;
                int status = rank == 0 ? LoanApprovalStatus.PENDING.getValue() : LoanApprovalStatus.QUEUE.getValue();

                // isWithinRange
                final BigDecimal value = loan.getProposedPrincipal();
                log.warn("createLoanMetrics value: {}", value);
                final BigDecimal minApprovalAmount = loanProductApprovalConfigData.getMinApprovalAmount() == null ? BigDecimal.ZERO
                        : loanProductApprovalConfigData.getMinApprovalAmount();
                log.warn("createLoanMetrics minApprovalAmount: {}", minApprovalAmount);
                final BigDecimal maxApprovalAmount = loanProductApprovalConfigData.getMaxApprovalAmount() == null ? BigDecimal.ZERO
                        : loanProductApprovalConfigData.getMaxApprovalAmount();
                log.warn("createLoanMetrics maxApprovalAmount: {}", maxApprovalAmount);
                final boolean isWithinRange = GeneralConstants.isWithinRange(value, minApprovalAmount, maxApprovalAmount);
                if ( // loanProductApprovalConfigData.getMaxApprovalAmount() == null
                     // || loanProductApprovalConfigData.getMaxApprovalAmount().compareTo(BigDecimal.ZERO) == 0
                     // || loanProductApprovalConfigData.getMaxApprovalAmount().compareTo(loan.getProposedPrincipal())
                     // >= 0
                isWithinRange) {
                    // create loan movement approval if this condition is met
                    final RoleData roleData = loanProductApprovalConfigData.getRoleData();
                    final Long roleId = roleData.getId();
                    Collection<AppUserData> appUserDatas = this.appUserBusinessReadPlatformService.retrieveActiveAppUsersForRole(roleId);
                    if (CollectionUtils.isEmpty(appUserDatas)) {
                        // send a mail informing no user_staff assigned to role
                        log.warn("No user/staff assigned to role {} with id {} on loan approval loan product config", roleData.getName(),
                                roleId);

                        List<String> businessAddresses = getBusinessAddresses();
                        if (!CollectionUtils.isEmpty(businessAddresses)) {
                            final String subject = "Loan Product Configuration Setup";
                            final String body = String.format("No user/staff assigned to role `%s` ", roleData.getName());
                            notificationToUsers(businessAddresses, subject, body);
                        }
                        nextRank = nextRank > 0 ? nextRank-- : 0;
                    } else {
                        try {
                            final Staff staff = setAssingmentLoanApprovalCheck(appUserDatas);
                            if (ObjectUtils.isEmpty(staff)) {
                                nextRank = nextRank > 0 ? nextRank-- : 0;
                                continue;
                            }
                            // final Long staffId = staff.getId();
                            // final AppUser appUser = this.appUserRepositoryWrapper.findFirstByStaffId(staffId);
                            // if (ObjectUtils.isNotEmpty(appUser)) {
                            // getEmailAddress(appUser, notifybusinessUsers);
                            // }
                            // final Staff organisationalRoleParentStaff = staff.getOrganisationalRoleParentStaff();
                            // if (ObjectUtils.isNotEmpty(organisationalRoleParentStaff)) {
                            // final Long organisationalRoleParentStaffId = organisationalRoleParentStaff.getId();
                            // final AppUser appUserSupervisor = this.appUserRepositoryWrapper
                            // .findFirstByStaffId(organisationalRoleParentStaffId);
                            // if (ObjectUtils.isNotEmpty(appUserSupervisor)) {
                            // // set email of approval supervisor
                            // getEmailAddress(appUserSupervisor, notifybusinessUsers);
                            // }
                            // }
                            final Metrics metrics = Metrics.createLoanMetrics(staff, status, rank, loan);
                            this.metricsRepositoryWrapper.saveAndFlush(metrics);
                            final Long metricsId = metrics.getId();
                            if (metricsId != null) {
                                final MetricsHistory metricsHistory = MetricsHistory.instance(metrics, status);
                                this.metricsHistoryRepositoryWrapper.saveAndFlush(metricsHistory);
                            }
                            updateLoan = true;
                        } catch (Exception e) {
                            throw new MetricsNotFoundException("createLoanMetricsAssigningFailed: " + e);
                        }
                    }
                } else {
                    nextRank = nextRank > 0 ? nextRank-- : 0;
                    log.warn("Not withIn range for loanId: {}", loanApprovalScheduleId);
                }
            }
            if (updateLoan) {
                // update dataTable loan approvalCheck
                String loanApprovalCheckSql = "UPDATE approvalCheck ac SET ac.isSentForApproval=1 WHERE ac.loan_id=?";
                jdbcTemplate.update(loanApprovalCheckSql, loanApprovalScheduleId);
                // if (!CollectionUtils.isEmpty(notifybusinessUsers)) {
                // final String subject = String.format("Notification on new Loan `%s` Awaiting Approval",
                // loanApprovalScheduleId);
                // final String body = String.format("%s with mobile %s have a loan (`%s`) pending approval.",
                // clientName, mobileNo,
                // loanProductName);
                // notificationToUsers(notifybusinessUsers, subject, body);
                // }
            }
        }
    }

    protected void getEmailAddress(final AppUser appUser, List<String> businessAddresses) {
        if (ObjectUtils.isNotEmpty(appUser)) {
            // set email of approval
            String address = appUser.getUsername();
            if (emailConfigurationValidator.isValidEmail(address)) {
                businessAddresses.add(address);
            } else {
                address = appUser.getEmail();
                if (emailConfigurationValidator.isValidEmail(address)) {
                    businessAddresses.add(address);
                }
            }
        }
    }

    protected void notificationToUsers(List<String> businessAddresses, final String subject, final String body) {
        String[] businessAddressesArray = businessAddresses.stream().toArray(String[]::new);
        final EmailDetail emailDetail = new EmailDetail(subject, body, businessAddressesArray, null);
        gmailBackedPlatformEmailService.sendDefinedEmail(emailDetail);
    }

    private Staff setAssingmentLoanApprovalCheck(final Collection<AppUserData> appUserDatas) {
        Staff staff = null;
        StaffData valueStaffData = appUserDatas.stream().findAny().map(mapper -> {
            return mapper.getStaff();
        }).orElse(null);
        HashMap<Long, Integer> staffWithExistingApprovals = new HashMap<>();
        for (AppUserData appUserData : appUserDatas) {
            final StaffData staffData = appUserData.getStaff();
            int count = this.metricsRepositoryWrapper.countByAssignedUserIdAndStatus(staffData.getId(),
                    LoanApprovalStatus.PENDING.getValue());
            staffWithExistingApprovals.put(staffData.getId(), count);
        }

        staffWithExistingApprovals = sortByValue(staffWithExistingApprovals);

        Map.Entry<Long, Integer> entry = staffWithExistingApprovals.entrySet().stream().findFirst().orElse(null);
        Long key = entry == null ? valueStaffData.getId() : entry.getKey();
        if (key == null) {
            // send mail of no staff available to be assigned
            log.warn("No user/staff available for approval assigining");
        } else {
            log.info("next picking key - {}", key);
            staff = this.staffRepositoryWrapper.findOneWithNotFoundDetection(key);
        }
        return staff;
    }

    public static HashMap<Long, Integer> sortByValue(HashMap<Long, Integer> hm) {

        HashMap<Long, Integer> temp = hm.entrySet().stream().sorted((i1, i2) -> i1.getValue().compareTo(i2.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return temp;

    }

    private List<String> getBusinessAddresses() {
        final Long roleAdminId = SUPER_USER_SERVICE_ROLE;
        List<String> businessAddresses = new ArrayList<>();
        Collection<AppUserData> appUserDatas = this.appUserBusinessReadPlatformService.retrieveActiveAppUsersForRole(roleAdminId);
        if (!CollectionUtils.isEmpty(appUserDatas)) {
            for (AppUserData appUserData : appUserDatas) {
                String address = appUserData.username();
                if (emailConfigurationValidator.isValidEmail(address)) {
                    businessAddresses.add(address);
                } else {
                    address = appUserData.getEmail();
                    if (emailConfigurationValidator.isValidEmail(address)) {
                        businessAddresses.add(address);
                    }
                }
            }
            return businessAddresses;
        } else {
            log.warn("No user available in Super User Role, Loan Approval metrics cannot be set.");
            return null;
        }
    }

    @Override
    public Collection<MetricsData> retrieveOverdraftMetrics(Long overdraftId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from
                                                                       // nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private static final class MetricsMapper implements RowMapper<MetricsData> {

        public String schema() {
            return " mm.id, mm.rank, mm.assigned_user_id staffId, ms.display_name staffDisplayName, ms.organisational_role_parent_staff_id, mss.display_name supervisorStaffDisplayName, "
                    + " mm.status_enum statusEnum, mm.loan_id loanId, mm.savings_id savingsId, mm.overdraft_id overdraftId, mm.created_on_utc createdOn, mm.last_modified_on_utc modifiedOn, "
                    + " mlv.loan_officer_id as loanOfficerId, msl.display_name as loanOfficerName, "
                    + " mlv.client_id as loanClientId, mcv.display_name as loanClientName " + "  from m_metrics mm "
                    + " LEFT JOIN m_loan_view mlv ON mlv.id=mm.loan_id" + " LEFT JOIN m_staff msl ON msl.id=mlv.loan_officer_id"
                    + " LEFT JOIN m_client_view mcv ON mcv.id=mlv.client_id" + " LEFT JOIN m_staff ms ON ms.id=mm.assigned_user_id"
                    + " LEFT JOIN m_staff mss ON mss.id=ms.organisational_role_parent_staff_id";
        }

        @Override
        public MetricsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long loanId = rs.getLong("loanId");
            final Integer rank = rs.getInt("rank");
            final Long savingsId = rs.getLong("savingsId");
            final Long overdraftId = rs.getLong("overdraftId");
            final Long id = rs.getLong("id");

            ClientData clientData = null;
            final Long loanClientId = JdbcSupport.getLong(rs, "loanClientId");
            if (loanClientId != null && loanClientId > 0) {
                final String loanClientName = rs.getString("loanClientName");
                clientData = ClientData.instance(id, loanClientName);
            }

            StaffData loanOfficerData = null;
            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            if (loanOfficerId != null && loanOfficerId > 0) {
                final String loanOfficerName = rs.getString("loanOfficerName");
                loanOfficerData = StaffData.lookup(loanOfficerId, loanOfficerName);
            }
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = LoanApprovalStatus.status(statusEnum);

            StaffData staffData = null;
            Long staffId = rs.getLong("staffId");
            if (staffId > 0) {
                final String staffDisplayName = rs.getString("staffDisplayName");
                staffData = StaffData.lookup(staffId, staffDisplayName);
            }
            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaffId = rs.getLong("organisational_role_parent_staff_id");
            if (organisationalRoleParentStaffId > 0) {
                final String organisationalRoleParentStaffDisplayName = rs.getString("supervisorStaffDisplayName");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaffId, organisationalRoleParentStaffDisplayName);
            }

            final LocalDateTime createdOnTime = JdbcSupport.getLocalDateTime(rs, "createdOn");
            final LocalDate createdOn = createdOnTime != null ? createdOnTime.toLocalDate() : null;

            final LocalDateTime modifiedOnTime = JdbcSupport.getLocalDateTime(rs, "modifiedOn");
            final LocalDate modifiedOn = modifiedOnTime != null ? modifiedOnTime.toLocalDate() : null;

            return MetricsData.instance(id, loanId, savingsId, status, staffData, supervisorStaffData, createdOn, modifiedOn, clientData,
                    loanOfficerData, overdraftId, rank);
        }

    }

    private String buildSqlStringFromMetricsCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final Long staffId = searchParameters.getStaffId();
        final Long staffSupervisorId = searchParameters.getStaffSupervisorId();
        final Long loanId = searchParameters.getLoanId();
        final Long overdraftId = searchParameters.getOverdraftId();
        final Long savingsId = searchParameters.getSavingsId();
        final Integer statusId = searchParameters.getStatusId();

        String extraCriteria = "";

        if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
            final LocalDate startPeriod = searchParameters.getFromDate();
            final LocalDate endPeriod = searchParameters.getToDate();

            final DateTimeFormatter df = DateUtils.DEFAULT_DATE_FORMATER;
            if (startPeriod != null && endPeriod != null) {
                extraCriteria += " and CAST(mm.created_on_utc AS DATE) BETWEEN ? AND ? ";
                paramList.add(df.format(startPeriod));
                paramList.add(df.format(endPeriod));
            } else if (startPeriod != null) {
                extraCriteria += " and CAST(mm.created_on_utc AS DATE) >= ? ";
                paramList.add(df.format(startPeriod));
            } else if (endPeriod != null) {
                extraCriteria += " and CAST(mm.created_on_utc AS DATE) <= ? ";
                paramList.add(df.format(endPeriod));
            }
        }

        if (searchParameters.isStaffSupervisorIdPassed()) {
            extraCriteria += " and mm.organisational_role_parent_staff_id = ? ";
            paramList.add(staffSupervisorId);
        }

        if (searchParameters.isStaffIdPassed()) {
            extraCriteria += " and mm.assigned_user_id = ? ";
            paramList.add(staffId);
        }

        if (searchParameters.isStatusIdPassed()) {
            extraCriteria += " and mm.status_enum = ? ";
            paramList.add(statusId);
        }

        if (searchParameters.isLoanIdPassed()) {
            extraCriteria += " and mm.loan_id = ? ";
            paramList.add(loanId);
        }

        if (searchParameters.isOverdraftIdPassed()) {
            extraCriteria += " and mm.overdraft_id = ? ";
            paramList.add(overdraftId);
        }

        if (searchParameters.isSavingsIdPassed()) {
            extraCriteria += " and mm.savings_id = ? ";
            paramList.add(savingsId);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

    private static final class MetricsLoanViewMapper implements RowMapper<MetricsData> {

        public String schema() {
            return " mm.id, '100' statusEnum, mm.loan_id loanId, mm.assigned_user_id staffId, mm.savings_id savingsId, mm.staff_display_name staffDisplayName, "
                    + " mm.organisational_role_parent_staff_id, mm.organisational_role_parent_staff_display_name supervisorStaffDisplayName, mm.created_on_utc createdOn, "
                    + " mlv.loan_officer_id as loanOfficerId, msl.display_name as loanOfficerName, mm.overdraft_id overdraftId, "
                    + " COALESCE(mlv.client_id,msv.client_id, '') as clientId, COALESCE(mcv.display_name,msv.display_name, '') as clientName "
                    + " FROM m_metrics_view mm " + " LEFT JOIN m_loan_view mlv ON mlv.id=mm.loan_id"
                    + " LEFT JOIN m_saving_view msv ON msv.id=mm.savings_id" + " LEFT JOIN m_staff msl ON msl.id=mlv.loan_officer_id"
                    + " LEFT JOIN m_client_view mcv ON mcv.id=mlv.client_id";
        }

        @Override
        public MetricsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Integer rank = null;
            final Long loanId = rs.getLong("loanId");
            final Long savingsId = rs.getLong("savingsId");
            final Long overdraftId = rs.getLong("overdraftId");
            final Long id = rs.getLong("id");

            ClientData clientData = null;
            final Long clientId = JdbcSupport.getLong(rs, "clientId");
            if (clientId != null && clientId > 0) {
                final String clientName = rs.getString("clientName");
                clientData = ClientData.instance(clientId, clientName);
            }

            StaffData loanOfficerData = null;
            final Long loanOfficerId = JdbcSupport.getLong(rs, "loanOfficerId");
            if (loanOfficerId != null && loanOfficerId > 0) {
                final String loanOfficerName = rs.getString("loanOfficerName");
                loanOfficerData = StaffData.lookup(loanOfficerId, loanOfficerName);
            }
            final Integer statusEnum = JdbcSupport.getInteger(rs, "statusEnum");
            final EnumOptionData status = LoanApprovalStatus.status(statusEnum);

            StaffData staffData = null;
            Long staffId = rs.getLong("staffId");
            if (staffId > 0) {
                final String staffDisplayName = rs.getString("staffDisplayName");
                staffData = StaffData.lookup(staffId, staffDisplayName);
            }
            StaffData supervisorStaffData = null;
            final Long organisationalRoleParentStaffId = rs.getLong("organisational_role_parent_staff_id");
            if (organisationalRoleParentStaffId > 0) {
                final String organisationalRoleParentStaffDisplayName = rs.getString("supervisorStaffDisplayName");
                supervisorStaffData = StaffData.lookup(organisationalRoleParentStaffId, organisationalRoleParentStaffDisplayName);
            }

            final LocalDateTime createdOnTime = JdbcSupport.getLocalDateTime(rs, "createdOn");
            final LocalDate createdOn = createdOnTime != null ? createdOnTime.toLocalDate() : null;

            final LocalDate modifiedOn = null;

            return MetricsData.instance(id, loanId, savingsId, status, staffData, supervisorStaffData, createdOn, modifiedOn, clientData,
                    loanOfficerData, overdraftId, rank);
        }

    }

    @Override
    public Collection<LoanProductApprovalConfigData> retrieveOverdraftRoleApprovalConfig() {
        this.context.authenticatedUser();
        final String sql = "select " + overdraftRoleApprovalViewMapper.schema() + " ORDER BY mroac.rank ASC ";
        return this.jdbcTemplate.query(sql, overdraftRoleApprovalViewMapper); // NOSONAR
    }

    private static final class OverdraftRoleApprovalViewMapper implements RowMapper<LoanProductApprovalConfigData> {

        public String schema() {
            return " mroac.id, mroac.role_id roleId, mr.name as roleName, mroac.min_approval_amount minApprovalAmount, mroac.max_approval_amount maxApprovalAmount, mroac.rank FROM m_role_overdraft_approval_config mroac JOIN m_role mr ON mr.id=mroac.role_id ";
        }

        @Override
        public LoanProductApprovalConfigData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final Long roleId = rs.getLong("roleId");
            final String roleName = rs.getString("roleName");
            final RoleData roleData = new RoleData(roleId, roleName, null, null);
            final BigDecimal minApprovalAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "minApprovalAmount");
            final BigDecimal maxApprovalAmount = JdbcSupport.getBigDecimalDefaultToNullIfZero(rs, "maxApprovalAmount");
            final Integer rank = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "rank");
            return LoanProductApprovalConfigData.instance(id, roleData, minApprovalAmount, maxApprovalAmount, rank);
        }

    }

}
