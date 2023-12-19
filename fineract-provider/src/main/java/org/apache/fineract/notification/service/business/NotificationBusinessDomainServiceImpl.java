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
package org.apache.fineract.notification.service.business;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.notification.data.NotificationData;
import org.apache.fineract.notification.eventandlistener.NotificationEventPublisher;
import org.apache.fineract.portfolio.businessevent.BusinessEventListener;
import org.apache.fineract.portfolio.businessevent.domain.loan.business.LoanMetricsApprovalBusinessEvent;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationBusinessDomainServiceImpl implements NotificationBusinessDomainService {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final PlatformSecurityContext context;
    private final NotificationEventPublisher notificationEventPublisher;
    private final AppUserRepository appUserRepository;

    @PostConstruct
    public void addListeners() {
        businessEventNotifierService.addPostBusinessEventListener(LoanMetricsApprovalBusinessEvent.class, new LoanMetricsApprovalListener());
    }

    private class LoanMetricsApprovalListener implements BusinessEventListener<LoanMetricsApprovalBusinessEvent> {

        @Override
        public void onBusinessEvent(LoanMetricsApprovalBusinessEvent event) {
            Loan loan = event.get();
            buildNotification("APPROVE_LOAN", "loan", loan.getId(), "Loan Pending Approval", "created", context.authenticatedUser().getId(),
                    loan.getOfficeId());
        }
    }

    private void buildNotification(String permission, String objectType, Long objectIdentifier, String notificationContent,
            String eventType, Long appUserId, Long officeId) {

        String tenantIdentifier = ThreadLocalContextUtil.getTenant().getTenantIdentifier();
        Set<Long> userIds = getNotifiableUserIds(officeId, permission);
        NotificationData notificationData = new NotificationData(objectType, objectIdentifier, eventType, appUserId, notificationContent,
                false, false, tenantIdentifier, officeId, userIds);
        try {
            notificationEventPublisher.broadcastNotification(notificationData);
        } catch (Exception e) {
            // We want to avoid rethrowing the exception to stop the business transaction from rolling back
            log.error("Error while broadcasting notification event", e);
        }
    }

    private Set<Long> getNotifiableUserIds(Long officeId, String permission) {
        Collection<AppUser> users = appUserRepository.findByOfficeId(officeId);
        Collection<AppUser> usersWithPermission = users.stream().filter(aU -> aU.hasAnyPermission(permission, "ALL_FUNCTIONS")).toList();
        return usersWithPermission.stream().map(AppUser::getId).collect(toSet());
    }
}
