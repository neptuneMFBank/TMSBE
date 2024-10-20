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

import java.util.Collection;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.portfolio.business.metrics.data.MetricsData;
import org.apache.fineract.portfolio.loanproduct.business.data.LoanProductApprovalConfigData;

public interface MetricsReadPlatformService {

    void queueLoanApprovals();

    void reminderLoanApprovals();

    Page<MetricsData> retrieveAll(final SearchParametersBusiness searchParameters);

    Collection<MetricsData> retrieveSavingsAccountMetrics(final Long savingsAccountId);

    Collection<MetricsData> retrieveLoanMetrics(final Long loanId);

    void queueOverdraftApprovals();

    void reminderOverdraftApprovals();

    Collection<MetricsData> retrieveOverdraftMetrics(final Long overdraftId);

    Collection<LoanProductApprovalConfigData> retrieveOverdraftRoleApprovalConfig();
}
