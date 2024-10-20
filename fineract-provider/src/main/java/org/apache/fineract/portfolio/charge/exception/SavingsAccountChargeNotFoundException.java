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
package org.apache.fineract.portfolio.charge.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;

/**
 * {@link AbstractPlatformDomainRuleException} thrown when savings account charge does not exist.
 */
public class SavingsAccountChargeNotFoundException extends AbstractPlatformResourceNotFoundException {

    public SavingsAccountChargeNotFoundException() {
        super("error.msg.savings.account.charge.id.invalid", "Savings Account charge does not exist");
    }

    public SavingsAccountChargeNotFoundException(final Long id) {
        super("error.msg.savings.account.charge.id.invalid", "Savings Account charge with identifier " + id + " does not exist", id);
    }

    public SavingsAccountChargeNotFoundException(final Long id, final Long savingsAccountId) {
        super("error.msg.savings.account.charge.id.invalid.for.given.savings.account",
                "Savings Account charge with identifier " + id + " does not exist for Savings Account " + savingsAccountId, id,
                savingsAccountId);
    }

    public SavingsAccountChargeNotFoundException(Long id, EmptyResultDataAccessException e) {
        super("error.msg.savings.account.charge.id.invalid", "Savings Account charge with identifier " + id + " does not exist", id, e);
    }
}
