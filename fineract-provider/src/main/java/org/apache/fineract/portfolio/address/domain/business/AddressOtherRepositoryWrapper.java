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
package org.apache.fineract.portfolio.address.domain.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Wrapper for {@link AddressOtherRepository} that adds NULL checking and Error handling capabilities
 * </p>
 */
@Service
public class AddressOtherRepositoryWrapper {

    private final AddressOtherRepository repository;

    @Autowired
    public AddressOtherRepositoryWrapper(final AddressOtherRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AddressOther findOneByAddressId(final Long addressId) {
        return this.repository.findOneByAddressId(addressId).orElse(null);
    }

    @Transactional
    public AddressOther saveAndFlush(final AddressOther addressOther) {
        return this.repository.saveAndFlush(addressOther);
    }

    @Transactional
    public AddressOther save(final AddressOther addressOther) {
        return this.repository.save(addressOther);
    }

    public void flush() {
        this.repository.flush();
    }

    public void delete(final Long addressOtherId) {
        this.repository.deleteById(addressOtherId);
    }

}
