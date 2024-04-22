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
package org.apache.fineract.portfolio.business.merchant.inventory.domain;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.business.merchant.inventory.data.InventoryValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.simplifytech.data.GeneralConstants;

@Entity
@Table(name = "m_inventory")
public class Inventory extends AbstractAuditableCustom {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "discount_rate", nullable = false)
    private BigDecimal discountRate;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "link", nullable = false)
    private String link;

    @ManyToOne(optional = true)
    @JoinColumn(name = "client_id", nullable = true)
    private Client client;

    public Inventory() {
    }

    private Inventory(final String name, final String description, final BigDecimal price,
            final BigDecimal discountRate, final String skuCode, final String link, final Client client) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountRate = discountRate;
        this.skuCode = skuCode;
        this.client = client;
        this.link = link;
    }

    public static Inventory instance(final JsonCommand command, Client client) {

        final String name = command.stringValueOfParameterNamed(InventoryValidator.nameParamName);
        final String description = command.stringValueOfParameterNamed(InventoryValidator.descriptionParamName);
        final BigDecimal price = command.bigDecimalValueOfParameterNamed(InventoryValidator.priceParamName);
        final String skuCode = command.stringValueOfParameterNamed(InventoryValidator.skuCodeParamName);
        BigDecimal discountRate = null;
        if (command.parameterExists(InventoryValidator.discountRateParamName)) {
            discountRate = command.bigDecimalValueOfParameterNamed(InventoryValidator.discountRateParamName);
        }
        String link = GeneralConstants.generateUniqueId();
        return new Inventory(name, description, price, discountRate, skuCode, link, client);
    }

    public Map<String, Object> update(JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(5);

        if (command.isChangeInStringParameterNamed(InventoryValidator.descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(InventoryValidator.descriptionParamName);
            actualChanges.put(InventoryValidator.descriptionParamName, newValue);
            this.description = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInStringParameterNamed(InventoryValidator.skuCodeParamName, this.skuCode)) {
            final String newValue = command.stringValueOfParameterNamed(InventoryValidator.skuCodeParamName);
            actualChanges.put(InventoryValidator.skuCodeParamName, newValue);
            this.skuCode = StringUtils.defaultIfEmpty(newValue, null);
        }
        if (command.isChangeInBigDecimalParameterNamed(InventoryValidator.priceParamName, this.price)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InventoryValidator.priceParamName);
            actualChanges.put(InventoryValidator.skuCodeParamName, newValue);
            this.price = newValue;
        }
        if (command.isChangeInBigDecimalParameterNamed(InventoryValidator.discountRateParamName, this.discountRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(InventoryValidator.discountRateParamName);
            actualChanges.put(InventoryValidator.discountRateParamName, newValue);
            this.discountRate = newValue;
        }
        if (command.parameterExists(InventoryValidator.clientIdParamName)) {
            final String newValue = command.stringValueOfParameterNamed(InventoryValidator.clientIdParamName);
            actualChanges.put(InventoryValidator.clientIdParamName, newValue);
        }

        return actualChanges;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
