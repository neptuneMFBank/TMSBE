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
package org.apache.fineract.portfolio.business.overdraft.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Data
@SuppressWarnings("unused")
public class OverdraftData implements Serializable {

    private final BigDecimal amount;

    private final BigDecimal nominalAnnualInterestRateOverdraft;

    private final LocalDate startDate;

    private final LocalDate expiryDate;
    private final Integer numberOfDays;

    private final String createdByUser;
    private final String modifiedByUser;
    private final LocalDate createdOn;
    private final LocalDate modifiedOn;

    private final Long id;
    private final Long savingsId;
    private final EnumOptionData status;

    public static OverdraftData instance(
            BigDecimal amount, BigDecimal nominalAnnualInterestRateOverdraft, LocalDate startDate, LocalDate expiryDate, String createdByUser, String modifiedByUser, LocalDate createdOn, LocalDate modifiedOn, Long id, Long savingsId, EnumOptionData status, final Integer numberOfDays) {
        return new OverdraftData(amount, nominalAnnualInterestRateOverdraft, startDate, expiryDate, createdByUser, modifiedByUser, createdOn, modifiedOn, id, savingsId, status, numberOfDays);
    }

    public OverdraftData(
            BigDecimal amount, BigDecimal nominalAnnualInterestRateOverdraft, LocalDate startDate, LocalDate expiryDate, String createdByUser, String modifiedByUser, LocalDate createdOn, LocalDate modifiedOn, Long id, Long savingsId, EnumOptionData status, final Integer numberOfDays) {
        this.amount = amount;
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.createdByUser = createdByUser;
        this.modifiedByUser = modifiedByUser;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.id = id;
        this.savingsId = savingsId;
        this.status = status;
        this.numberOfDays = numberOfDays;
    }

}
