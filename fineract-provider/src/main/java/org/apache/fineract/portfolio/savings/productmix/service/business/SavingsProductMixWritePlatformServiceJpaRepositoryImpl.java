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
package org.apache.fineract.portfolio.savings.productmix.service.business;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.productmix.exception.ProductMixNotFoundException;
import org.apache.fineract.portfolio.loanproduct.productmix.serialization.ProductMixDataValidator;
import org.apache.fineract.portfolio.savings.domain.SavingsProduct;
import org.apache.fineract.portfolio.savings.domain.SavingsProductRepository;
import org.apache.fineract.portfolio.savings.exception.SavingsProductNotFoundException;
import org.apache.fineract.portfolio.savings.productmix.domain.business.SavingsProductMix;
import org.apache.fineract.portfolio.savings.productmix.domain.business.SavingsProductMixRepository;
import org.apache.fineract.portfolio.savings.productmix.exception.business.SavingsProductMixNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.NonTransientDataAccessException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class SavingsProductMixWritePlatformServiceJpaRepositoryImpl implements SavingsProductMixWritePlatformService {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsProductMixWritePlatformServiceJpaRepositoryImpl.class);
    private final PlatformSecurityContext context;
    private final ProductMixDataValidator fromApiJsonDeserializer;
    private final SavingsProductMixRepository savingsProductMixRepository;
    private final SavingsProductRepository savingsProductRepository;

    public SavingsProductMixWritePlatformServiceJpaRepositoryImpl(PlatformSecurityContext context,
            ProductMixDataValidator fromApiJsonDeserializer, SavingsProductMixRepository savingsProductMixRepository,
            SavingsProductRepository savingsProductRepository) {
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.savingsProductMixRepository = savingsProductMixRepository;
        this.savingsProductRepository = savingsProductRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createProductMix(Long productId, JsonCommand command) {
        try {

            this.context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // remove the existed restriction if it is not exists in
            // restrictedIds.
            final List<Long> removedRestrictions = updateRestrictionsForProduct(productId, restrictedIds);
            final Map<Long, SavingsProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            final List<SavingsProductMix> productMixes = new ArrayList<>();

            createNewProductMix(restrictedProductsAsMap, productId, productMixes);

            this.savingsProductMixRepository.saveAll(productMixes);

            final Map<String, Object> changes = new LinkedHashMap<>();
            changes.put("restrictedProductsForMix", restrictedProductsAsMap.keySet());
            changes.put("removedProductsForMix", removedRestrictions);
            return new CommandProcessingResultBuilder().withProductId(productId).with(changes).withCommandId(command.commandId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private List<Long> updateRestrictionsForProduct(final Long productId, final Set<String> restrictedIds) {

        final List<Long> removedRestrictions = new ArrayList<>();
        final List<SavingsProductMix> mixesToRemove = new ArrayList<>();

        final List<SavingsProductMix> existedProductMixes = this.savingsProductMixRepository.findRestrictedProducts(productId);
        for (final SavingsProductMix productMix : existedProductMixes) {
            if (!restrictedIds.contains(productMix.getProductId().toString())) {
                mixesToRemove.add(productMix);
                removedRestrictions.add(productMix.getId());
            }
        }
        if (!CollectionUtils.isEmpty(mixesToRemove)) {
            this.savingsProductMixRepository.deleteAll(mixesToRemove);
        }
        return removedRestrictions;
    }

    private void createNewProductMix(final Map<Long, SavingsProduct> restrictedProductsAsMap, final Long productId,
            final List<SavingsProductMix> productMixes) {

        final SavingsProduct productMixInstance = findByProductIdIfProvided(productId);
        for (final SavingsProduct restrictedProduct : restrictedProductsAsMap.values()) {
            final SavingsProductMix productMix = SavingsProductMix.createNew(productMixInstance, restrictedProduct);
            productMixes.add(productMix);
        }
    }

    @Override
    public CommandProcessingResult updateProductMix(Long productId, JsonCommand command) {
        try {
            this.context.authenticatedUser();
            this.fromApiJsonDeserializer.validateForUpdate(command.json());
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<SavingsProductMix> existedProductMixes = new ArrayList<>(
                    this.savingsProductMixRepository.findByProductId(productId));
            if (CollectionUtils.isEmpty(existedProductMixes)) {
                throw new ProductMixNotFoundException(productId);
            }
            final Set<String> restrictedIds = new HashSet<>(Arrays.asList(command.arrayValueOfParameterNamed("restrictedProducts")));

            // updating with empty array means deleting the existed records.
            if (restrictedIds.isEmpty()) {
                final List<Long> removedRestrictedProductIds = this.savingsProductMixRepository
                        .findRestrictedProductIdsByProductId(productId);
                this.savingsProductMixRepository.deleteAll(existedProductMixes);
                changes.put("removedProductsForMix", removedRestrictedProductIds);
                return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId())
                        .build();
            }

            /*
             * if restrictedProducts array is not empty delete the duplicate ids which are already exists and update
             * existedProductMixes
             */
            final List<SavingsProductMix> productMixesToRemove = updateRestrictedIds(restrictedIds, existedProductMixes);
            final Map<Long, SavingsProduct> restrictedProductsAsMap = getRestrictedProducts(restrictedIds);
            createNewProductMix(restrictedProductsAsMap, productId, existedProductMixes);

            this.savingsProductMixRepository.saveAll(existedProductMixes);
            changes.put("restrictedProductsForMix", getProductIdsFromCollection(existedProductMixes));

            if (!CollectionUtils.isEmpty(productMixesToRemove)) {
                this.savingsProductMixRepository.deleteAll(productMixesToRemove);
                changes.put("removedProductsForMix", getProductIdsFromCollection(productMixesToRemove));
            }
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).withCommandId(command.commandId()).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {

            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private SavingsProduct findByProductIdIfProvided(final Long productId) {
        return this.savingsProductRepository.findById(productId).orElseThrow(() -> new SavingsProductNotFoundException(productId));
    }

    private Map<Long, SavingsProduct> getRestrictedProducts(final Set<String> restrictedIds) {

        final Map<Long, SavingsProduct> restricrtedProducts = new HashMap<>();

        for (final String restrictedId : restrictedIds) {
            final Long restrictedIdAsLong = Long.valueOf(restrictedId);
            final SavingsProduct restrictedProduct = findByProductIdIfProvided(Long.valueOf(restrictedId));
            restricrtedProducts.put(restrictedIdAsLong, restrictedProduct);
        }
        return restricrtedProducts;
    }

    private void handleDataIntegrityIssues(final NonTransientDataAccessException dve) {
        LOG.error("Error occured.", dve);
        throw new PlatformDataIntegrityException("error.msg.product.savings.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private List<SavingsProductMix> updateRestrictedIds(final Set<String> restrictedIds,
            final List<SavingsProductMix> existedProductMixes) {

        final List<SavingsProductMix> productMixesToRemove = new ArrayList<>();
        for (final SavingsProductMix productMix : existedProductMixes) {
            final String currentMixId = productMix.getRestrictedProductId().toString();
            if (restrictedIds.contains(currentMixId)) {
                restrictedIds.remove(currentMixId);
            } else {
                productMixesToRemove.add(productMix);
            }
        }
        existedProductMixes.removeAll(productMixesToRemove);
        return productMixesToRemove;
    }

    @Override
    public CommandProcessingResult deleteProductMix(Long productId) {
        try {
            this.context.authenticatedUser();
            final Map<String, Object> changes = new LinkedHashMap<>();

            final List<SavingsProductMix> existedProductMixes = this.savingsProductMixRepository.findByProductId(productId);
            if (CollectionUtils.isEmpty(existedProductMixes)) {
                throw new SavingsProductMixNotFoundException(productId);
            }
            this.savingsProductMixRepository.deleteAll(existedProductMixes);
            changes.put("removedProductsForMix", getProductIdsFromCollection(existedProductMixes));
            return new CommandProcessingResultBuilder().with(changes).withProductId(productId).build();
        } catch (final JpaSystemException | DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(dve);
            return CommandProcessingResult.empty();
        }
    }

    private List<Long> getProductIdsFromCollection(final List<SavingsProductMix> collection) {
        final List<Long> productIds = new ArrayList<>();
        for (final SavingsProductMix productMix : collection) {
            productIds.add(productMix.getRestrictedProductId());
        }
        return productIds;
    }
}
