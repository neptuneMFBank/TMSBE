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
import lombok.Data;

/**
 * Immutable data object representing client data.
 */
@SuppressWarnings("unused")
@Data
public final class KycBusinessData implements Serializable {

    private final Long clientId;
    private final Boolean personal;
    private final Boolean residential;
    private final Boolean employment;
    private final Boolean nextOfKin;
    private final Boolean bankDetail;
    private final Boolean identification;
    private final Boolean agreement;
    private final Boolean directors;

    public static KycBusinessData instance(Long clientId, Boolean personal, Boolean residential, Boolean employment, Boolean nextOfKin,
            Boolean bankDetail, Boolean identification, Boolean agreement, Boolean directors) {
        return new KycBusinessData(clientId, personal, residential, employment, nextOfKin, bankDetail, identification, agreement,
                directors);
    }

}
