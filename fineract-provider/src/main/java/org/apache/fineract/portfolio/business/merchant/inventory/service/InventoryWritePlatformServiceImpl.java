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
package org.apache.fineract.portfolio.business.merchant.inventory.service;

import java.util.Map;
import javax.persistence.PersistenceException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.business.merchant.inventory.data.InventoryValidator;
import org.apache.fineract.portfolio.business.merchant.inventory.domain.Inventory;
import org.apache.fineract.portfolio.business.merchant.inventory.domain.InventoryRepository;
import org.apache.fineract.portfolio.business.merchant.inventory.exception.InventoryNotFound;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryWritePlatformServiceImpl implements InventoryWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(InventoryWritePlatformServiceImpl.class);

    private final PlatformSecurityContext context;
    private final InventoryValidator inventoryValidator;
    private final ClientRepositoryWrapper clientRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryWritePlatformServiceImpl(final PlatformSecurityContext context,
            final InventoryValidator inventoryValidator, final ClientRepositoryWrapper clientRepository,
            final InventoryRepository inventoryRepository) {
        this.context = context;
        this.inventoryValidator = inventoryValidator;
        this.clientRepository = clientRepository;
        this.inventoryRepository = inventoryRepository;

    }

    @Transactional 
    @Override
    public CommandProcessingResult create(JsonCommand command) {
        try {
            final AppUser currentUser = this.context.authenticatedUser();
            this.inventoryValidator.validateCreate(command.json());
            final Long clientId = command.longValueOfParameterNamed(InventoryValidator.clientIdParamName);

            Client client = null;

            if (clientId != null) {
                client = this.clientRepository.findOneWithNotFoundDetection(clientId);
                if (client.isNotActive()) {
                    throw new ClientNotActiveException(clientId);
                }
            }
            Inventory inventory = Inventory.instance(command, client);

            this.inventoryRepository.saveAndFlush(inventory);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(inventory.getId()) //
                    .build();

        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }
    }
    
    

    @Transactional
    @Override
    public CommandProcessingResult update(final Long resourceId, final JsonCommand command) {
        try {
            this.context.authenticatedUser();

            this.inventoryValidator.validateForUpdate(command.json());
            final Inventory inventory = this.inventoryRepository.findById(resourceId)
                    .orElseThrow(() -> new InventoryNotFound(resourceId));

            final Map<String, Object> changes = inventory.update(command);

            if (changes.containsKey(InventoryValidator.clientIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(InventoryValidator.clientIdParamName);
                final Client client = this.clientRepository.findOneWithNotFoundDetection(newValue);
                inventory.setClient(client);
            }

            if (!changes.isEmpty()) {
                this.inventoryRepository.saveAndFlush(inventory);
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(resourceId) //
                    .with(changes) //
                    .build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve.getMostSpecificCause(), dve);
            return CommandProcessingResult.empty();
        } catch (final PersistenceException dve) {
            Throwable throwable = ExceptionUtils.getRootCause(dve.getCause());
            handleDataIntegrityIssues(command, throwable, dve);
            return CommandProcessingResult.empty();
        }

    }

    @Transactional
    @Override
    public CommandProcessingResult delete(final Long resourceId) {
        this.context.authenticatedUser();
        final Inventory inventory = this.inventoryRepository.findById(resourceId)
                .orElseThrow(() -> new InventoryNotFound(resourceId));
        this.inventoryRepository.delete(inventory);

        return new CommandProcessingResultBuilder() //
                .withEntityId(inventory.getId()) //
                .build();

    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        LOG.error("Error occured.", dve);
    }

    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {
        if (realCause.getMessage().contains("inventory_name_UNIQUE")) {
            final String name = command.stringValueOfParameterNamed(InventoryValidator.nameParamName);
            throw new PlatformDataIntegrityException("error.msg.inventory.duplicate.name",
                    "Inventory with name`" + name + "` already exists", InventoryValidator.nameParamName, name);

        }
        if (realCause.getMessage().contains("client_inventory_skucode_UNIQUE")) {
            final String skuCode = command.stringValueOfParameterNamed(InventoryValidator.skuCodeParamName);
            throw new PlatformDataIntegrityException("error.msg.inventory.duplicate.skuCode",
                    "client has Inventory with skuCode`" + skuCode + "` already exists", InventoryValidator.nameParamName, skuCode);

        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.inventory.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

  
}
