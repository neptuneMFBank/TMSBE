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
package org.apache.fineract.infrastructure.codes.domain.business;

import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.codes.domain.CodeRepository;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.exception.CodeNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link CodeRepository} that is responsible for checking if {@link CodeValue} is returned when using
 * <code>findOne</code> and <code>findByCodeNameAndId</code> repository methods and throwing an appropriate not found
 * exception.
 * </p>
 *
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code base where {@link CodeRepository} is
 * required.
 * </p>
 */
@Service
public class CodeRepositoryWrapper {

    private final CodeRepository repository;

    @Autowired
    public CodeRepositoryWrapper(final CodeRepository repository) {
        this.repository = repository;
    }

    public Code findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new CodeNotFoundException(id));
    }

}
