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
package org.apache.fineract.portfolio.client.data.business;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable data object representing client data.
 */
@SuppressWarnings("unused")
@Data
@Builder
public final class ClientWalletSyncBusinessData implements Comparable<ClientWalletSyncBusinessData>, Serializable {

    private final Long clientId;
    private final Long savingsId;
    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String displayName;
    private final String emailAddress;
    private final String mobileNo;
    private final String tin;
    private final String incorpNo;
    private final String dateOfBirth;
    private final String countryOfRegistration;

    public static ClientWalletSyncBusinessData clientBusinessBasicInfo(Long clientId, Long savingsId, String displayName, String firstname,
            String middlename, String lastname, String dateOfBirth, String mobileNo, String emailAddress, String tin, String incorpNo, String countryOfRegistration) {

        return new ClientWalletSyncBusinessData(clientId, savingsId, firstname, middlename, lastname, displayName, emailAddress, mobileNo,
                tin, incorpNo, dateOfBirth, countryOfRegistration);
    }

    @Override
    public int compareTo(@NotNull ClientWalletSyncBusinessData o) {
        return 0;
    }
}
