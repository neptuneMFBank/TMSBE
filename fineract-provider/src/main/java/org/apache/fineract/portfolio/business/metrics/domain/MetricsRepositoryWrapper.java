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
package org.apache.fineract.portfolio.business.metrics.domain;

import java.util.List;
import org.apache.fineract.portfolio.business.metrics.exception.MetricsNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetricsRepositoryWrapper {

    private final MetricsRepository repository;

    @Autowired
    public MetricsRepositoryWrapper(final MetricsRepository repository) {
        this.repository = repository;
    }

    public void saveAndFlush(final Metrics metrics) {
        this.repository.saveAndFlush(metrics);
    }

    public boolean existsByLoanId(final Long loanId) {
        return this.repository.existsByLoanId(loanId);
    }

    public boolean existsBySavingsAccountId(final Long savingsAccountId) {
        return this.repository.existsBySavingsAccountId(savingsAccountId);
    }

    @Transactional(readOnly = true)
    public Metrics findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new MetricsNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public int countByAssignedUserIdAndStatus(Long assignedUserId, Integer status) {
        return this.repository.countByAssignedUserIdAndStatus(assignedUserId, status);
    }

    @Transactional(readOnly = true)
    public List<Metrics> findBySavingsAccountId(final Long savingsAccountId) {
        return this.repository.findBySavingsAccountId(savingsAccountId);
    }

    @Transactional(readOnly = true)
    public List<Metrics> findByLoanId(final Long loanId) {
        return this.repository.findByLoanId(loanId);
    }

    @Transactional(readOnly = true)
    public List<Metrics> findByOverdraftId(final Long overdraftId) {
        return this.repository.findByOverdraftId(overdraftId);
    }

}
