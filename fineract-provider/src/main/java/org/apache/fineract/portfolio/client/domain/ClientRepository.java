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
package org.apache.fineract.portfolio.client.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

    String FIND_CLIENT_BY_ACCOUNT_NUMBER = "select client from Client client where client.accountNumber = :accountNumber";

    @Query(FIND_CLIENT_BY_ACCOUNT_NUMBER)
    Client getClientByAccountNumber(@Param("accountNumber") String accountNumber);

    Client findByMobileNoOrEmailAddress(String mobileNo, String emailAddress);

    Client findByMobileNo(String mobileNo);

    Client findByEmailAddress(String emailAddress);

    Optional<Client> findFirstByOfficeId(Long officeId);

}
