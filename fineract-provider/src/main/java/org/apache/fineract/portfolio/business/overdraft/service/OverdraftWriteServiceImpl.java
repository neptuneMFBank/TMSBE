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
package org.apache.fineract.portfolio.business.overdraft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.account.service.AccountAssociationsReadPlatformService;
import org.apache.fineract.portfolio.business.metrics.data.MetricsDataValidator;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsHistoryRepositoryWrapper;
import org.apache.fineract.portfolio.business.metrics.domain.MetricsRepositoryWrapper;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanChargeRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdraftWriteServiceImpl implements OverdraftWriteService {

    private final MetricsDataValidator fromApiJsonDeserializer;
    private final FromJsonHelper fromApiJsonHelper;
    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final PlatformSecurityContext context;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final MetricsRepositoryWrapper metricsRepositoryWrapper;
    private final MetricsHistoryRepositoryWrapper metricsHistoryRepositoryWrapper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ReadWriteNonCoreDataService readWriteNonCoreDataService;
    private final LoanChargeRepository loanChargeRepository;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        log.warn("handleDataIntegrityIssues: {} and Exception: {}", realCause.getMessage(), dve.getMessage());
        // String[] cause = StringUtils.split(realCause.getMessage(), "'");

        // String getCause = StringUtils.defaultIfBlank(cause[3], realCause.getMessage());
        // if (getCause.contains("name")) {
        // final String name = command.stringValueOfParameterNamed(MetricsApiResourceConstants.NAME);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate", "Metrics with name `" + name + "`
        // already exists",
        // MetricsApiResourceConstants.NAME, name);
        // } else if (getCause.contains("external_id")) {
        // final String externalId = command.stringValueOfParameterNamed(MetricsApiResourceConstants.EXTERNALID);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate",
        // "Metrics with externalId `" + externalId + "` already exists", MetricsApiResourceConstants.EXTERNALID,
        // externalId);
        // } else if (getCause.contains("rc_number")) {
        // final String rcNumber = command.stringValueOfParameterNamed(MetricsApiResourceConstants.RCNUMBER);
        // throw new PlatformDataIntegrityException("error.msg.metrics.duplicate.mobileNo",
        // "Metrics with registration `" + rcNumber + "` already exists", MetricsApiResourceConstants.RCNUMBER,
        // rcNumber);
        // }
        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.metrics.unknown.data.integrity.issue", "One or more fields are in conflict.",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("MetricsErrorOccured: {}", dve);
    }

    @Override
    public CommandProcessingResult submitOverdraft(JsonCommand command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CommandProcessingResult modifyOverdraft(Long overdraftId, JsonCommand command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CommandProcessingResult deleteOverdraft(Long overdraftId) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CommandProcessingResult stopOverdraft(Long overdraftId, JsonCommand command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public CommandProcessingResult sendOverdraftForApproval(Long overdraftId, JsonCommand command) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
