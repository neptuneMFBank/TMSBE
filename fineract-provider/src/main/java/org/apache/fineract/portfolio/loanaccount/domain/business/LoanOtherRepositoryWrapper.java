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
package org.apache.fineract.portfolio.loanaccount.domain.business;

import org.apache.fineract.portfolio.loanaccount.exception.business.LoanOtherNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link LoanOtherRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class LoanOtherRepositoryWrapper {

    private final LoanOtherRepository repository;

    @Autowired
    public LoanOtherRepositoryWrapper(final LoanOtherRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public LoanOther findOneWithNotFoundDetection(final Long id) {
        final LoanOther loan = this.repository.findById(id).orElseThrow(() -> new LoanOtherNotFoundException(id));
        return loan;
    }

    @Transactional(readOnly = true)
    public LoanOther findOneByLoanId(final Long loanId) {
        return this.repository.findOneByLoanId(loanId).orElse(null);
    }

    @Transactional
    public LoanOther saveAndFlush(final LoanOther loan) {
        return this.repository.saveAndFlush(loan);
    }

    @Transactional
    public LoanOther save(final LoanOther loan) {
        return this.repository.save(loan);
    }

    public void flush() {
        this.repository.flush();
    }

    public void delete(final Long loanId) {
        this.repository.deleteById(loanId);
    }

}
