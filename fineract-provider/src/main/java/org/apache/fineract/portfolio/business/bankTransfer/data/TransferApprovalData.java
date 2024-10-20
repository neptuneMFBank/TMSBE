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
package org.apache.fineract.portfolio.business.bankTransfer.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import lombok.Data;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

@Data
public class TransferApprovalData implements Serializable {

    private final Long id;
    private BigDecimal amount;
    private Integer status;
    private EnumOptionData transferType;
    private Collection<EnumOptionData> transferTypeOptions;
    private Integer holdTransactionId;
    private Integer releaseTransactionId;
    private Integer withdrawTransactionId;
    private Integer fromAccountId;
    private Integer fromAccountType;
    private String fromAccountNumber;
    private String fromAccountName;
    private Integer toAccountId;
    private Integer toAccountType;
    private String toAccountNumber;
    private CodeValueData activationChannel;
    private CodeValueData toBank;
    private String reason;
    private String note;
    private String createdByUsername;
    private String createdByFirstname;
    private String createdByLastname;
    private Long createdById;
    private LocalDate createdOn;

    private TransferApprovalData(Long id, BigDecimal amount, Integer status, EnumOptionData transferType, Integer holdTransactionId,
            Integer releaseTransactionId, Integer withdrawTransactionId, Integer fromAccountId, Integer fromAccountType,
            String fromAccountNumber, Integer toAccountId, Integer toAccountType, String toAccountNumber, CodeValueData activationChannel,
            CodeValueData toBank, String reason, String createdByUsername, String createdByFirstname, String createdByLastname,
            Long createdById, LocalDate createdOn, Collection<EnumOptionData> transferTypeOptions, String fromAccountName, String note) {
        this.id = id;
        this.amount = amount;
        this.status = status;
        this.transferType = transferType;
        this.holdTransactionId = holdTransactionId;
        this.releaseTransactionId = releaseTransactionId;
        this.withdrawTransactionId = withdrawTransactionId;
        this.fromAccountId = fromAccountId;
        this.fromAccountType = fromAccountType;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountType = toAccountType;
        this.toAccountNumber = toAccountNumber;
        this.activationChannel = activationChannel;
        this.toBank = toBank;
        this.reason = reason;
        this.createdByUsername = createdByUsername;
        this.createdByFirstname = createdByFirstname;
        this.createdByLastname = createdByLastname;
        this.createdById = createdById;
        this.createdOn = createdOn;
        this.transferTypeOptions = transferTypeOptions;
        this.fromAccountName = fromAccountName;
        this.note = note;
    }

    public static TransferApprovalData instance(Long id, BigDecimal amount, Integer status, EnumOptionData transferType,
            Integer holdTransactionId, Integer releaseTransactionId, Integer withdrawTransactionId, Integer fromAccountId,
            Integer fromAccountType, String fromAccountNumber, Integer toAccountId, Integer toAccountType, String toAccountNumber,
            CodeValueData activationChannel, CodeValueData toBank, String reason, String createdByUsername, String createdByFirstname,
            String createdByLastname, Long createdById, LocalDate createdOn, String fromAccountName, String note) {
        Collection<EnumOptionData> transferTypeOptions = null;
        return new TransferApprovalData(id, amount, status, transferType, holdTransactionId, releaseTransactionId, withdrawTransactionId,
                fromAccountId, fromAccountType, fromAccountNumber, toAccountId, toAccountType, toAccountNumber, activationChannel, toBank,
                reason, createdByUsername, createdByFirstname, createdByLastname, createdById, createdOn, transferTypeOptions,
                fromAccountName, note);

    }

    public static TransferApprovalData template(Collection<EnumOptionData> transferTypeOptions) {
        Long id = null;
        BigDecimal amount = null;
        Integer status = null;
        EnumOptionData transferType = null;
        Integer holdTransactionId = null;
        Integer releaseTransactionId = null;
        Integer withdrawTransactionId = null;
        Integer fromAccountId = null;
        Integer fromAccountType = null;
        String fromAccountNumber = null;
        Integer toAccountId = null;
        Integer toAccountType = null;
        String toAccountNumber = null;
        CodeValueData activationChannel = null;
        CodeValueData toBank = null;
        String reason = null;
        String createdByUsername = null;
        String createdByFirstname = null;
        String createdByLastname = null;
        Long createdById = null;
        LocalDate createdOn = null;
        String fromAccountName = null;
        String note = null;
        return new TransferApprovalData(id, amount, status, transferType, holdTransactionId, releaseTransactionId, withdrawTransactionId,
                fromAccountId, fromAccountType, fromAccountNumber, toAccountId, toAccountType, toAccountNumber, activationChannel, toBank,
                reason, createdByUsername, createdByFirstname, createdByLastname, createdById, createdOn, transferTypeOptions,
                fromAccountName, note);

    }
}
