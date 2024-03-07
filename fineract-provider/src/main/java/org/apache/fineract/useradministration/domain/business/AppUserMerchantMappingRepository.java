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
package org.apache.fineract.useradministration.domain.business;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserMerchantMappingRepository extends JpaRepository<AppUserMerchantMapping, Long> {

    List<AppUserMerchantMapping> findByClientId(long clientId);

    // Returns records that have matching values in both tables. Pictorial representation is given below:
    @Query(nativeQuery = true, value = "SELECT msucm.appuser_id FROM m_appuser_merchant_mapping msucm INNER JOIN m_appuser ma ON msucm.appuser_id = ma.id WHERE msucm.client_id=client_id")
    List<Long> findAppUserByClientId(@Param("client_id") Long client_id);

}
