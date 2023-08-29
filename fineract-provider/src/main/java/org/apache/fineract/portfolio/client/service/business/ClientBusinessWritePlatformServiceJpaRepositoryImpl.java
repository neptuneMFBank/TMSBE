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
package org.apache.fineract.portfolio.client.service.business;

import com.google.gson.JsonElement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import javax.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandProcessingService;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormat;
import org.apache.fineract.infrastructure.accountnumberformat.domain.AccountNumberFormatRepositoryWrapper;
import org.apache.fineract.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.configuration.data.GlobalConfigurationPropertyData;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.staff.domain.Staff;
import org.apache.fineract.organisation.staff.domain.StaffRepositoryWrapper;
import org.apache.fineract.portfolio.address.service.business.AddressBusinessWritePlatformService;
import org.apache.fineract.portfolio.businessevent.domain.client.ClientActivateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.domain.client.ClientCreateBusinessEvent;
import org.apache.fineract.portfolio.businessevent.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.business.ClientBusinessDataValidator;
import org.apache.fineract.portfolio.client.domain.AccountNumberGenerator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientNonPerson;
import org.apache.fineract.portfolio.client.domain.ClientNonPersonRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.client.exception.ClientActiveForUpdateException;
import org.apache.fineract.portfolio.client.service.ClientFamilyMembersWritePlatformService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepository;
import org.apache.fineract.portfolio.group.exception.GroupMemberCountNotInPermissibleRangeException;
import org.apache.fineract.portfolio.group.exception.GroupNotFoundException;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.note.domain.NoteRepository;
import org.apache.fineract.portfolio.savings.data.SavingsAccountDataDTO;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.portfolio.savings.service.SavingsApplicationProcessWritePlatformService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class ClientBusinessWritePlatformServiceJpaRepositoryImpl implements ClientBusinessWritePlatformService {

    private final PlatformSecurityContext context;
    private final ClientRepositoryWrapper clientRepository;
    private final ClientNonPersonRepositoryWrapper clientNonPersonRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final NoteRepository noteRepository;
    private final GroupRepository groupRepository;
    private final ClientBusinessDataValidator fromApiJsonDeserializer;
    private final AccountNumberGenerator accountNumberGenerator;
    private final StaffRepositoryWrapper staffRepository;
    private final CodeValueRepositoryWrapper codeValueRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final SavingsAccountRepositoryWrapper savingsRepositoryWrapper;
    private final SavingsProductRepository savingsProductRepository;
    private final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService;
    private final CommandProcessingService commandProcessingService;
    private final ConfigurationDomainService configurationDomainService;
    private final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ConfigurationReadPlatformService configurationReadPlatformService;
    private final AddressBusinessWritePlatformService addressWritePlatformService;
    private final ClientFamilyMembersWritePlatformService clientFamilyMembersWritePlatformService;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;

    @Autowired
    public ClientBusinessWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final ClientRepositoryWrapper clientRepository, final ClientNonPersonRepositoryWrapper clientNonPersonRepository,
            final OfficeRepositoryWrapper officeRepositoryWrapper, final NoteRepository noteRepository,
            final ClientBusinessDataValidator fromApiJsonDeserializer, final AccountNumberGenerator accountNumberGenerator,
            final GroupRepository groupRepository, final StaffRepositoryWrapper staffRepository,
            final CodeValueRepositoryWrapper codeValueRepository, final LoanRepositoryWrapper loanRepositoryWrapper,
            final SavingsAccountRepositoryWrapper savingsRepositoryWrapper, final SavingsProductRepository savingsProductRepository,
            final SavingsApplicationProcessWritePlatformService savingsApplicationProcessWritePlatformService,
            final CommandProcessingService commandProcessingService, final ConfigurationDomainService configurationDomainService,
            final AccountNumberFormatRepositoryWrapper accountNumberFormatRepository, final FromJsonHelper fromApiJsonHelper,
            final ConfigurationReadPlatformService configurationReadPlatformService,
            final AddressBusinessWritePlatformService addressWritePlatformService,
            final ClientFamilyMembersWritePlatformService clientFamilyMembersWritePlatformService,
            final BusinessEventNotifierService businessEventNotifierService,
            final EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientNonPersonRepository = clientNonPersonRepository;
        this.officeRepositoryWrapper = officeRepositoryWrapper;
        this.noteRepository = noteRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.accountNumberGenerator = accountNumberGenerator;
        this.groupRepository = groupRepository;
        this.staffRepository = staffRepository;
        this.codeValueRepository = codeValueRepository;
        this.loanRepositoryWrapper = loanRepositoryWrapper;
        this.savingsRepositoryWrapper = savingsRepositoryWrapper;
        this.savingsProductRepository = savingsProductRepository;
        this.savingsApplicationProcessWritePlatformService = savingsApplicationProcessWritePlatformService;
        this.commandProcessingService = commandProcessingService;
        this.configurationDomainService = configurationDomainService;
        this.accountNumberFormatRepository = accountNumberFormatRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.configurationReadPlatformService = configurationReadPlatformService;
        this.addressWritePlatformService = addressWritePlatformService;
        this.clientFamilyMembersWritePlatformService = clientFamilyMembersWritePlatformService;
        this.businessEventNotifierService = businessEventNotifierService;
        this.entityDatatableChecksWritePlatformService = entityDatatableChecksWritePlatformService;
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue is.
     */
    private void handleDataIntegrityIssues(final JsonCommand command, final Throwable realCause, final Exception dve) {

        if (realCause.getMessage().contains("external_id")) {

            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.externalId",
                    "Client with externalId `" + externalId + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("account_no_UNIQUE")) {
            final String accountNo = command.stringValueOfParameterNamed("accountNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.accountNo",
                    "Client with accountNo `" + accountNo + "` already exists", "accountNo", accountNo);
        } else if (realCause.getMessage().contains("mobile_no")) {
            final String mobileNo = command.stringValueOfParameterNamed("mobileNo");
            throw new PlatformDataIntegrityException("error.msg.client.duplicate.mobileNo",
                    "Client with mobileNo `" + mobileNo + "` already exists", "mobileNo", mobileNo);
        }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public CommandProcessingResult createClient(final JsonCommand command) {

        try {
            final AppUser currentUser = this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final GlobalConfigurationPropertyData configuration = this.configurationReadPlatformService
                    .retrieveGlobalConfiguration("Enable-Address");

            final Boolean isAddressEnabled = configuration.isEnabled();

            final Boolean isStaff = command.booleanObjectValueOfParameterNamed(ClientApiConstants.isStaffParamName);

            final Long officeId = command.longValueOfParameterNamed(ClientApiConstants.officeIdParamName);

            final Office clientOffice = this.officeRepositoryWrapper.findOneWithNotFoundDetection(officeId);

            final Long groupId = command.longValueOfParameterNamed(ClientApiConstants.groupIdParamName);

            Group clientParentGroup = null;
            if (groupId != null) {
                clientParentGroup = this.groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
            }

            Staff staff = null;
            final Long staffId = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
            if (staffId != null) {
                staff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(staffId, clientOffice.getHierarchy());
            }

            CodeValue gender = null;
            final Long genderId = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
            if (genderId != null) {
                gender = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, genderId);
            }

            CodeValue clientType = null;
            final Long clientTypeId = command.longValueOfParameterNamed(ClientApiConstants.clientTypeIdParamName);
            if (clientTypeId != null) {
                clientType = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                        clientTypeId);
            }

            CodeValue clientClassification = null;
            final Long clientClassificationId = command.longValueOfParameterNamed(ClientApiConstants.clientClassificationIdParamName);
            if (clientClassificationId != null) {
                clientClassification = this.codeValueRepository
                        .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLASSIFICATION, clientClassificationId);
            }

            final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
            if (savingsProductId != null) {
                this.savingsProductRepository.findById(savingsProductId)
                        .orElseThrow(() -> new SavingsProductNotFoundException(savingsProductId));
            }

            final Integer legalFormParamValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
            boolean isEntity = false;
            Integer legalFormValue = null;
            if (legalFormParamValue != null) {
                LegalForm legalForm = LegalForm.fromInt(legalFormParamValue);
                if (legalForm != null) {
                    legalFormValue = legalForm.getValue();
                    isEntity = legalForm.isEntity();
                }
            }

            final Client newClient = Client.createNew(currentUser, clientOffice, clientParentGroup, staff, savingsProductId, gender,
                    clientType, clientClassification, legalFormValue, command);
            this.clientRepository.saveAndFlush(newClient);
            boolean rollbackTransaction = false;
            if (newClient.isActive()) {
                validateParentGroupRulesBeforeClientActivation(newClient);
                runEntityDatatableCheck(newClient.getId());
                final CommandWrapper commandWrapper = new CommandWrapperBuilder().activateClient(null).build();
                rollbackTransaction = this.commandProcessingService.validateCommand(commandWrapper, currentUser);
            }

            this.clientRepository.saveAndFlush(newClient);
            if (newClient.isActive()) {
                businessEventNotifierService.notifyPostBusinessEvent(new ClientActivateBusinessEvent(newClient));
            }
            if (newClient.isAccountNumberRequiresAutoGeneration()) {
                AccountNumberFormat accountNumberFormat = this.accountNumberFormatRepository.findByAccountType(EntityAccountType.CLIENT);
                newClient.updateAccountNo(accountNumberGenerator.generate(newClient, accountNumberFormat));
                this.clientRepository.saveAndFlush(newClient);
            }

            final Locale locale = command.extractLocale();
            final DateTimeFormatter fmt = DateTimeFormatter.ofPattern(command.dateFormat()).withLocale(locale);
            CommandProcessingResult result = openSavingsAccount(newClient, fmt);
            if (result.getSavingsId() != null) {
                this.clientRepository.saveAndFlush(newClient);

            }

            if (isEntity) {
                extractAndCreateClientNonPerson(newClient, command);
            }

            if (isAddressEnabled && this.fromApiJsonHelper.parameterExists(ClientApiConstants.address, command.parsedJson())) {
                this.addressWritePlatformService.addNewClientAddress(newClient, command);
            }

            if (command.arrayOfParameterNamed("familyMembers") != null) {
                this.clientFamilyMembersWritePlatformService.addClientFamilyMember(newClient, command);
            }

            if (command.parameterExists(ClientApiConstants.datatables)) {
                this.entityDatatableChecksWritePlatformService.saveDatatables(StatusEnum.CREATE.getCode().longValue(),
                        EntityTables.CLIENT.getName(), newClient.getId(), null,
                        command.arrayOfParameterNamed(ClientApiConstants.datatables));
            }

            businessEventNotifierService.notifyPostBusinessEvent(new ClientCreateBusinessEvent(newClient));

            entityDatatableChecksWritePlatformService.runTheCheck(newClient.getId(), EntityTables.CLIENT.getName(),
                    StatusEnum.CREATE.getCode().longValue(), EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable());
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientOffice.getId()) //
                    .withClientId(newClient.getId()) //
                    .withGroupId(groupId) //
                    .withEntityId(newClient.getId()) //
                    .withSavingsId(result.getSavingsId())//
                    .setRollbackTransaction(rollbackTransaction)//
                    .setRollbackTransaction(result.isRollbackTransaction())//
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

    /**
     * This method extracts ClientNonPerson details from Client command and creates a new ClientNonPerson record
     *
     * @param client
     * @param command
     */
    public void extractAndCreateClientNonPerson(Client client, JsonCommand command) {
        final JsonElement clientNonPersonElement = this.fromApiJsonHelper
                .parse(command.jsonFragment(ClientApiConstants.clientNonPersonDetailsParamName));

        if (clientNonPersonElement != null && !isEmpty(clientNonPersonElement)) {
            final String incorpNumber = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.incorpNumberParamName,
                    clientNonPersonElement);
            final String remarks = this.fromApiJsonHelper.extractStringNamed(ClientApiConstants.remarksParamName, clientNonPersonElement);
            final LocalDate incorpValidityTill = this.fromApiJsonHelper
                    .extractLocalDateNamed(ClientApiConstants.incorpValidityTillParamName, clientNonPersonElement);

            // JsonCommand clientNonPersonCommand =
            // JsonCommand.fromExistingCommand(command,
            // command.arrayOfParameterNamed(ClientApiConstants.clientNonPersonDetailsParamName).getAsJsonObject());
            CodeValue clientNonPersonConstitution = null;
            final Long clientNonPersonConstitutionId = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName,
                    clientNonPersonElement);
            if (clientNonPersonConstitutionId != null) {
                clientNonPersonConstitution = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION, clientNonPersonConstitutionId);
            }

            CodeValue clientNonPersonMainBusinessLine = null;
            final Long clientNonPersonMainBusinessLineId = this.fromApiJsonHelper
                    .extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName, clientNonPersonElement);
            if (clientNonPersonMainBusinessLineId != null) {
                clientNonPersonMainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                        ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE, clientNonPersonMainBusinessLineId);
            }

            final ClientNonPerson newClientNonPerson = ClientNonPerson.createNew(client, clientNonPersonConstitution,
                    clientNonPersonMainBusinessLine, incorpNumber, incorpValidityTill, remarks);

            this.clientNonPersonRepository.save(newClientNonPerson);
        }
    }

    public boolean isEmpty(final JsonElement element) {
        return element.toString().trim().length() < 4;
    }

    @Transactional
    @Override
    public CommandProcessingResult updateClient(final Long clientId, final JsonCommand command) {

        try {
            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            final Client clientForUpdate = this.clientRepository.findOneWithNotFoundDetection(clientId);
            final String clientHierarchy = clientForUpdate.getOffice().getHierarchy();

            this.context.validateAccessRights(clientHierarchy);

            final Map<String, Object> changes = clientForUpdate.update(command);

            if (changes.containsKey(ClientApiConstants.staffIdParamName)) {

                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.staffIdParamName);
                Staff newStaff = null;
                if (newValue != null) {
                    newStaff = this.staffRepository.findByOfficeHierarchyWithNotFoundDetection(newValue,
                            clientForUpdate.getOffice().getHierarchy());
                }
                clientForUpdate.updateStaff(newStaff);
            }

            if (changes.containsKey(ClientApiConstants.genderIdParamName)) {

                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
                CodeValue gender = null;
                if (newValue != null) {
                    gender = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, newValue);
                }
                clientForUpdate.updateGender(gender);
            }

            if (changes.containsKey(ClientApiConstants.savingsProductIdParamName)) {
                if (clientForUpdate.isActive()) {
                    throw new ClientActiveForUpdateException(clientId, ClientApiConstants.savingsProductIdParamName);
                }
                final Long savingsProductId = command.longValueOfParameterNamed(ClientApiConstants.savingsProductIdParamName);
                if (savingsProductId != null) {
                    this.savingsProductRepository.findById(savingsProductId)
                            .orElseThrow(() -> new SavingsProductNotFoundException(savingsProductId));
                }
                clientForUpdate.updateSavingsProduct(savingsProductId);
            }

            if (changes.containsKey(ClientApiConstants.genderIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.genderIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.GENDER, newValue);
                }
                clientForUpdate.updateGender(newCodeVal);
            }

            if (changes.containsKey(ClientApiConstants.clientTypeIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientTypeIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_TYPE,
                            newValue);
                }
                clientForUpdate.updateClientType(newCodeVal);
            }

            if (changes.containsKey(ClientApiConstants.clientClassificationIdParamName)) {
                final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientClassificationIdParamName);
                CodeValue newCodeVal = null;
                if (newValue != null) {
                    newCodeVal = this.codeValueRepository
                            .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLASSIFICATION, newValue);
                }
                clientForUpdate.updateClientClassification(newCodeVal);
            }

            if (!changes.isEmpty()) {
                this.clientRepository.saveAndFlush(clientForUpdate);
            }

            if (changes.containsKey(ClientApiConstants.legalFormIdParamName)) {
                Integer legalFormValue = clientForUpdate.getLegalForm();
                boolean isChangedToEntity = false;
                if (legalFormValue != null) {
                    LegalForm legalForm = LegalForm.fromInt(legalFormValue);
                    if (legalForm != null) {
                        isChangedToEntity = legalForm.isEntity();
                    }
                }

                if (isChangedToEntity) {
                    extractAndCreateClientNonPerson(clientForUpdate, command);
                } else {
                    final ClientNonPerson clientNonPerson = this.clientNonPersonRepository.findOneByClientId(clientForUpdate.getId());
                    if (clientNonPerson != null) {
                        this.clientNonPersonRepository.delete(clientNonPerson);
                    }
                }
            }

            final ClientNonPerson clientNonPersonForUpdate = this.clientNonPersonRepository.findOneByClientId(clientId);
            if (clientNonPersonForUpdate != null) {
                final JsonElement clientNonPersonElement = command.jsonElement(ClientApiConstants.clientNonPersonDetailsParamName);
                final Map<String, Object> clientNonPersonChanges = clientNonPersonForUpdate
                        .update(JsonCommand.fromExistingCommand(command, clientNonPersonElement));

                if (clientNonPersonChanges.containsKey(ClientApiConstants.constitutionIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.constitutionIdParamName,
                            clientNonPersonElement);
                    CodeValue constitution = null;
                    if (newValue != null) {
                        constitution = this.codeValueRepository
                                .findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_NON_PERSON_CONSTITUTION, newValue);
                    }
                    clientNonPersonForUpdate.updateConstitution(constitution);
                }

                if (clientNonPersonChanges.containsKey(ClientApiConstants.mainBusinessLineIdParamName)) {

                    final Long newValue = this.fromApiJsonHelper.extractLongNamed(ClientApiConstants.mainBusinessLineIdParamName,
                            clientNonPersonElement);
                    CodeValue mainBusinessLine = null;
                    if (newValue != null) {
                        mainBusinessLine = this.codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(
                                ClientApiConstants.CLIENT_NON_PERSON_MAIN_BUSINESS_LINE, newValue);
                    }
                    clientNonPersonForUpdate.updateMainBusinessLine(mainBusinessLine);
                }

                if (!clientNonPersonChanges.isEmpty()) {
                    this.clientNonPersonRepository.saveAndFlush(clientNonPersonForUpdate);
                }

                changes.putAll(clientNonPersonChanges);
            } else {
                final Integer legalFormParamValue = command.integerValueOfParameterNamed(ClientApiConstants.legalFormIdParamName);
                boolean isEntity = false;
                if (legalFormParamValue != null) {
                    final LegalForm legalForm = LegalForm.fromInt(legalFormParamValue);
                    if (legalForm != null) {
                        isEntity = legalForm.isEntity();
                    }
                }
                if (isEntity) {
                    extractAndCreateClientNonPerson(clientForUpdate, command);
                }
            }
            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withOfficeId(clientForUpdate.officeId()) //
                    .withClientId(clientId) //
                    .withEntityId(clientId) //
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

    private CommandProcessingResult openSavingsAccount(final Client client, final DateTimeFormatter fmt) {
        CommandProcessingResult commandProcessingResult = CommandProcessingResult.empty();
        if (client.isActive() && client.savingsProductId() != null) {
            SavingsAccountDataDTO savingsAccountDataDTO = new SavingsAccountDataDTO(client, null, client.savingsProductId(),
                    client.getActivationLocalDate(), client.activatedBy(), fmt);
            commandProcessingResult = this.savingsApplicationProcessWritePlatformService.createActiveApplication(savingsAccountDataDTO);
            if (commandProcessingResult.getSavingsId() != null) {
                this.savingsRepositoryWrapper.findOneWithNotFoundDetection(commandProcessingResult.getSavingsId());
                client.updateSavingsAccount(commandProcessingResult.getSavingsId());
                client.updateSavingsProduct(null);
            }
        }
        return commandProcessingResult;
    }

    private void logAsErrorUnexpectedDataIntegrityException(final Exception dve) {
        log.error("Error occured.", dve);
    }

    /*
     * To become a part of a group, group may have set of criteria to be m et before client can become member of it.
     */
    private void validateParentGroupRulesBeforeClientActivation(Client client) {
        Integer minNumberOfClients = configurationDomainService.retrieveMinAllowedClientsInGroup();
        Integer maxNumberOfClients = configurationDomainService.retrieveMaxAllowedClientsInGroup();
        if (client.getGroups() != null && maxNumberOfClients != null) {
            for (Group group : client.getGroups()) {
                /**
                 * Since this Client has not yet been associated with the group, reduce maxNumberOfClients by 1
                 *
                 */
                final boolean validationsuccess = group.isGroupsClientCountWithinMaxRange(maxNumberOfClients - 1);
                if (!validationsuccess) {
                    throw new GroupMemberCountNotInPermissibleRangeException(group.getId(), minNumberOfClients, maxNumberOfClients);
                }
            }
        }
    }

    private void runEntityDatatableCheck(final Long clientId) {
        entityDatatableChecksWritePlatformService.runTheCheck(clientId, EntityTables.CLIENT.getName(),
                StatusEnum.ACTIVATE.getCode().longValue(), EntityTables.CLIENT.getForeignKeyColumnNameOnDatatable());
    }

}
