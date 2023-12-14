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
package org.apache.fineract.portfolio.loanproduct.domain.business;

import java.util.List;
import org.apache.fineract.portfolio.loanproduct.exception.business.LoanProductPaymentTypeConfigNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanProductPaymentTypeConfigRepositoryWrapper {

    private final LoanProductPaymentTypeConfigRepository repository;

    @Autowired
    public LoanProductPaymentTypeConfigRepositoryWrapper(final LoanProductPaymentTypeConfigRepository repository) {
        this.repository = repository;
    }

    public void saveAndFlush(final LoanProductPaymentTypeConfig loanProductPaymentTypeConfig) {
        this.repository.saveAndFlush(loanProductPaymentTypeConfig);
    }

    @Transactional(readOnly = true)
    public List<LoanProductPaymentTypeConfig> findByLoanProductId(final Long loanProductId) {
        return this.repository.findByLoanProductId(loanProductId);
    }

    @Transactional(readOnly = true)
    public LoanProductPaymentTypeConfig findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new LoanProductPaymentTypeConfigNotFoundException(id));
    }

}
