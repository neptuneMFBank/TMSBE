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
package org.apache.fineract.portfolio.savings.data.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountStatusEnumData;

/**
 * Immutable data object representing abstract for Fixed and Recurring Deposit Accounts Accounts.
 */
public class DepositAccountBusinessData {

    protected final Long id;
    protected final String accountNo;
    protected final EnumOptionData depositType;
    protected final SavingsAccountStatusEnumData status;

    protected final Long clientId;
    protected final String clientName;

    protected final Long officeId;
    protected final String officeName;

    protected final Long depositProductId;
    protected final String depositProductName;
    protected final BigDecimal availableBalance;
    protected final BigDecimal ledgerBalance;

    protected final LocalDate createdOn;
    protected final LocalDate activatedOn;
    protected final LocalDate lastTransactionOn;

    protected final String externalId;

    public static DepositAccountBusinessData retrieveName(final DepositAccountBusinessData depositAccountBusinessData) {
        Long id = depositAccountBusinessData.getId();
        String accountNo = depositAccountBusinessData.getAccountNo();
        EnumOptionData depositType = depositAccountBusinessData.getDepositType();
        SavingsAccountStatusEnumData status = depositAccountBusinessData.getStatus();
        Long clientId = depositAccountBusinessData.getClientId();
        String clientName = depositAccountBusinessData.getClientName();
        Long depositProductId = null;
        String depositProductName = null;
        BigDecimal availableBalance = null;
        BigDecimal ledgerBalance = null;
        LocalDate createdOn = null;
        LocalDate activatedOn = null;
        LocalDate lastTransactionOn = null;
        String externalId = null;
        Long officeId = null;
        String officeName = null;
        return new DepositAccountBusinessData(id, accountNo, depositType, status, clientId, clientName, depositProductId,
                depositProductName, availableBalance, ledgerBalance, createdOn, activatedOn, lastTransactionOn, externalId, officeId,
                officeName);
    }

    public static DepositAccountBusinessData retrieveBalance(final DepositAccountBusinessData depositAccountBusinessData) {
        Long id = depositAccountBusinessData.getId();
        String accountNo = depositAccountBusinessData.getAccountNo();
        EnumOptionData depositType = depositAccountBusinessData.getDepositType();
        SavingsAccountStatusEnumData status = depositAccountBusinessData.getStatus();
        Long clientId = depositAccountBusinessData.getClientId();
        String clientName = depositAccountBusinessData.getClientName();
        BigDecimal availableBalance = depositAccountBusinessData.getAvailableBalance();
        BigDecimal ledgerBalance = depositAccountBusinessData.getLedgerBalance();
        Long depositProductId = null;
        String depositProductName = null;
        LocalDate createdOn = null;
        LocalDate activatedOn = null;
        LocalDate lastTransactionOn = null;
        String externalId = null;
        Long officeId = null;
        String officeName = null;
        return new DepositAccountBusinessData(id, accountNo, depositType, status, clientId, clientName, depositProductId,
                depositProductName, availableBalance, ledgerBalance, createdOn, activatedOn, lastTransactionOn, externalId, officeId,
                officeName);
    }

    public static DepositAccountBusinessData lookUp(Long id, String accountNo, EnumOptionData depositType,
            SavingsAccountStatusEnumData status, Long clientId, String clientName, Long depositProductId, String depositProductName,
            BigDecimal availableBalance, BigDecimal ledgerBalance, LocalDate createdOn, LocalDate activatedOn, LocalDate lastTransactionOn,
            String externalId, Long officeId, String officeName) {
        return new DepositAccountBusinessData(id, accountNo, depositType, status, clientId, clientName, depositProductId,
                depositProductName, availableBalance, ledgerBalance, createdOn, activatedOn, lastTransactionOn, externalId, officeId,
                officeName);
    }

    private DepositAccountBusinessData(Long id, String accountNo, EnumOptionData depositType, SavingsAccountStatusEnumData status,
            Long clientId, String clientName, Long depositProductId, String depositProductName, BigDecimal availableBalance,
            BigDecimal ledgerBalance, LocalDate createdOn, LocalDate activatedOn, LocalDate lastTransactionOn, String externalId,
            Long officeId, String officeName) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.accountNo = accountNo;
        this.depositType = depositType;
        this.status = status;
        this.clientId = clientId;
        this.clientName = clientName;
        this.depositProductId = depositProductId;
        this.depositProductName = depositProductName;
        this.availableBalance = availableBalance;
        this.ledgerBalance = ledgerBalance;
        this.createdOn = createdOn;
        this.activatedOn = activatedOn;
        this.lastTransactionOn = lastTransactionOn;
        this.externalId = externalId;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public EnumOptionData getDepositType() {
        return depositType;
    }

    public SavingsAccountStatusEnumData getStatus() {
        return status;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public Long getDepositProductId() {
        return depositProductId;
    }

    public String getDepositProductName() {
        return depositProductName;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getLedgerBalance() {
        return ledgerBalance;
    }

    public LocalDate getCreatedOn() {
        return createdOn;
    }

    public LocalDate getActivatedOn() {
        return activatedOn;
    }

    public LocalDate getLastTransactionOn() {
        return lastTransactionOn;
    }

    public String getExternalId() {
        return externalId;
    }

}
