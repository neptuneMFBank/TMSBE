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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.codes.service.CodeValueReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
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
import org.apache.fineract.portfolio.address.service.business.AddressBusinessReadPlatformService;
import org.apache.fineract.portfolio.business.metrics.data.LoanApprovalStatus;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.business.metrics.domain.Metrics;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistory;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistoryRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.exception.MetricsNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalConfigData;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalData;
import org.apache.fineract.portfolio.loanproduct.business.service.LoanProductApprovalReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.service.business.AppUserBusinessReadPlatformService;
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
    private final AddressBusinessReadPlatformService addressReadPlatformService;
    private final CodeValueReadPlatformService codeValueReadPlatformService;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final MetricsMapper metricsMapper = new MetricsMapper();
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final MetricsRepositoryWrapper metricsRepositoryWrapper;
    private final MetricsHistoryRepositoryWrapper metricsHistoryRepositoryWrapper;
    private final LoanProductApprovalReadPlatformService loanProductApprovalReadPlatformService;
    private final AppUserBusinessReadPlatformService appUserBusinessReadPlatformService;
    private final StaffRepositoryWrapper staffRepositoryWrapper;

    @Override
    @Transactional
    @CronTarget(jobName = JobName.QUEUE_LOAN_APPROVAL_CHECKS)
    public void queueLoanApprovals() {
        final String sqlFinder = "select mlasv.loan_id loanId from m_loan_approval_schedule_view mlasv ";
        List<Long> loanApprovalSchedule
                = this.jdbcTemplate.queryForList(sqlFinder, Long.class);
        for (Long loanApprovalScheduleId : loanApprovalSchedule) {
            createLoanMetrics(loanApprovalScheduleId);
        }
        log.info("{}: Records affected by queueLoanApprovals: {}", ThreadLocalContextUtil.getTenant().getName(),
                loanApprovalSchedule.size());
    }

    @Override
    public Page<MetricsData> retrieveAllLoanMetrics(SearchParametersBusiness searchParameters) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Page<MetricsData> retrieveAllSavingsAccountMetrics(SearchParametersBusiness searchParameters) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<MetricsData> retrieveSavingsAccountMetrics(Long savingsAccountId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Collection<MetricsData> retrieveLoanMetrics(Long loanId) {
        this.context.authenticatedUser();
        final String sql = "select " + metricsMapper.schema() + " WHERE mm.loan_id = ? ORDER BY mm.rank ASC ";
        return this.jdbcTemplate.query(sql, metricsMapper, loanId);
    }

    private void createLoanMetrics(Long loanApprovalScheduleId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanApprovalScheduleId);
        final LoanProduct loanProduct = loan.getLoanProduct();
        final Long loanProductId = loanProduct.getId();
        final LoanProductApprovalData loanProductApprovalData
                = this.loanProductApprovalReadPlatformService.retrieveOneViaLoanProduct(loanProductId);

        final Collection<LoanProductApprovalConfigData> loanProductApprovalConfigDatas
                = loanProductApprovalData.getLoanProductApprovalConfigData();

        if (CollectionUtils.isEmpty(loanProductApprovalConfigDatas)) {
            // send a mail
            log.warn("No loan approval set for loan product {} with id {}", loanProduct.getName(), loanProductId);
        } else {
            int nextRank = 0;
            for (LoanProductApprovalConfigData loanProductApprovalConfigData : loanProductApprovalConfigDatas) {
                int rank = nextRank++;
                int status = rank == 0 ? LoanApprovalStatus.PENDING.getValue() : LoanApprovalStatus.QUEUE.getValue();
                if (loanProductApprovalConfigData.getMaxApprovalAmount() == null
                        || loanProductApprovalConfigData.getMaxApprovalAmount().compareTo(BigDecimal.ZERO) == 0
                        || loanProductApprovalConfigData.getMaxApprovalAmount().compareTo(loan.getProposedPrincipal()) >= 0) {
                    // create loan movement approval if this condition is met
                    final RoleData roleData = loanProductApprovalConfigData.getRoleData();
                    final Long roleId = roleData.getId();
                    Collection<AppUserData> appUserDatas = this.appUserBusinessReadPlatformService.retrieveActiveAppUsersForRole(roleId);
                    if (CollectionUtils.isEmpty(appUserDatas)) {
                        //send a mail informing no user_staff assigned to role
                        log.warn("No user/staff assigned to role {} with id {} on loan approval loan product onfig", roleData.getName(), roleId);
                    } else {
                        try {
                            final Staff staff = setAssingmentLoanApprovalCheck(appUserDatas);
                            final Metrics metrics = Metrics.createLoanMetrics(staff, status, rank, loan);
                            this.metricsRepositoryWrapper.saveAndFlush(metrics);
                            final Long metricsId = metrics.getId();
                            if (metricsId != null) {
                                final MetricsHistory metricsHistory = MetricsHistory.instance(metrics, status);
                                this.metricsHistoryRepositoryWrapper.saveAndFlush(metricsHistory);

                            }
                        } catch (Exception e) {
                            throw new MetricsNotFoundException("createLoanMetricsAssigningFailed: " + e);
                        }
                    }
                }
            }
        }

    }

    private Staff setAssingmentLoanApprovalCheck(final Collection<AppUserData> appUserDatas) {
        Staff staff = null;
        StaffData valueStaffData = appUserDatas
                .stream()
                .findAny().map(mapper -> {
                    return mapper.getStaff();
                })
                .orElse(null);
        HashMap<Long, Integer> staffWithExistingLoanApprovals = new HashMap<>();
        for (AppUserData appUserData : appUserDatas) {
            final StaffData staffData = appUserData.getStaff();
            int count = this.metricsRepositoryWrapper.countByAssignedUserIdAndStatus(staffData.getId(), LoanApprovalStatus.PENDING.getValue());
            staffWithExistingLoanApprovals.put(staffData.getId(), count);
        }

        staffWithExistingLoanApprovals = sortByValue(staffWithExistingLoanApprovals);

        Map.Entry<Long, Integer> entry = staffWithExistingLoanApprovals.entrySet().stream().findFirst().orElse(null);
        Long key = entry == null ? valueStaffData.getId() : entry.getKey();
        if (key == null) {
            //send mail of no staff available to be assigned
            log.warn("No user/staff available for loan approval assigining");
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

    private static final class MetricsMapper implements RowMapper<MetricsData> {

        public String schema() {
            return " mm.id, mm.assigned_user_id staffId, ms.display_name staffDisplayName, ms.organisational_role_parent_staff_id, mss.display_name supervisorStaffDisplayName, "
                    + " mm.status_enum statusEnum, mm.loan_id loanId, mm.savings_id savingsId, mm.created_on_utc createdOn, mm.last_modified_on_utc modifiedOn  "
                    + "  from m_metrics mm "
                    + " LEFT JOIN m_staff ms ON ms.id=me.assigned_user_id"
                    + " LEFT JOIN m_staff mss ON mss.id=ms.organisational_role_parent_staff_id";
        }

        @Override
        public MetricsData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long loanId = rs.getLong("loanId");
            final Long savingsId = rs.getLong("savingsId");
            final Long id = rs.getLong("id");

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

            return MetricsData.instance(id, loanId, savingsId, status, staffData, supervisorStaffData, createdOn, modifiedOn);
        }

    }

}
