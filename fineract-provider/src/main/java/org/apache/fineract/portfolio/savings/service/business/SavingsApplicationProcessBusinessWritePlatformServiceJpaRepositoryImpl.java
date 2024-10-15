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
package org.apache.fineract.portfolio.savings.service.business;

import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.portfolio.accountdetails.domain.AccountType;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.exception.ClientNotActiveException;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.apache.fineract.portfolio.group.exception.CenterNotActiveException;
import org.apache.fineract.portfolio.group.exception.ClientNotInGroupException;
import org.apache.fineract.portfolio.group.exception.GroupNotActiveException;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountStatusType;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.domain.business.SavingsAccountRepositoryWrapperBusinsess;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SavingsApplicationProcessBusinessWritePlatformServiceJpaRepositoryImpl
        implements SavingsApplicationProcessBusinessWritePlatformService {

    private final SavingsProductBusinessReadPlatformService savingsProductBusinessReadPlatformService;
    private final SavingsAccountRepositoryWrapperBusinsess savingsAccountRepositoryWrapperBusinsess;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepositoryWrapper groupRepository;
    private final SavingsProductRepository savingProductRepository;

    @Override
    public void checkForProductMixRestrictions(final Long clientId, Long productId, Long groupId) {

        final List<Long> activeSavingsSavingsProductIds;

        final SavingsProduct product = this.savingProductRepository.findById(productId)
                .orElseThrow(() -> new SavingsProductNotFoundException(productId));

        Client client = null;
        Group group = null;
        AccountType accountType = AccountType.INVALID;

        if (clientId != null) {
            client = this.clientRepository.findOneWithNotFoundDetection(clientId);
            accountType = AccountType.INDIVIDUAL;
            if (client.isNotActive()) {
                throw new ClientNotActiveException(clientId);
            }
        }

        if (groupId != null) {
            group = this.groupRepository.findOneWithNotFoundDetection(groupId);
            accountType = AccountType.GROUP;
            if (group.isNotActive()) {
                if (group.isCenter()) {
                    throw new CenterNotActiveException(groupId);
                }
                throw new GroupNotActiveException(groupId);
            }
        }

        if (group != null && client != null) {
            if (!group.hasClientAsMember(client)) {
                throw new ClientNotInGroupException(clientId, groupId);
            }
            accountType = AccountType.JLG;
        }

        if (AccountType.fromInt(accountType.getValue()).isGroupAccount()) {
            activeSavingsSavingsProductIds = this.savingsAccountRepositoryWrapperBusinsess
                    .findActiveSavingsSavingsProductIdsByGroup(groupId, SavingsAccountStatusType.ACTIVE.getValue());
        } else {
            activeSavingsSavingsProductIds = this.savingsAccountRepositoryWrapperBusinsess
                    .findActiveSavingsSavingsProductIdsByClient(clientId, SavingsAccountStatusType.ACTIVE.getValue());
        }
        checkForProductMixRestrictions(activeSavingsSavingsProductIds, productId, product.getName());
    }

    private void checkForProductMixRestrictions(final List<Long> activeSavingsSavingsProductIds, final Long productId,
            final String productName) {

        if (!CollectionUtils.isEmpty(activeSavingsSavingsProductIds)) {
            final Collection<SavingsProductData> restrictedPrdouctsList = this.savingsProductBusinessReadPlatformService
                    .retrieveRestrictedProductsForMix(productId);
            for (final SavingsProductData restrictedProduct : restrictedPrdouctsList) {
                if (activeSavingsSavingsProductIds.contains(restrictedProduct.getId())) {
                    throw new GeneralPlatformDomainRuleException(
                            "error.msg.savings.applied.or.to.be.disbursed.can.not.co-exist.with.the.savings.already.active.to.this.client",
                            "This savings could not be applied/disbursed as the savings (" + StringUtils.defaultIfBlank(productName, "")
                                    + ") and `" + StringUtils.defaultIfBlank(restrictedProduct.getName(), "")
                                    + "` are not allowed to co-exist");
                }
            }
        }
    }

}
