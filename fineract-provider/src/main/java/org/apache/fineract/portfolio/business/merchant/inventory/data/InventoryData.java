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
package org.apache.fineract.portfolio.business.merchant.inventory.data;

import java.math.BigDecimal;

@SuppressWarnings("unused")
public class InventoryData {

    private Long id;
    private final String name;
    private final String description;
    private final String skuCode;
    private final BigDecimal price;
    private final BigDecimal discountRate;

    private InventoryData(final Long id, final String name, final String description, final String skuCode, final BigDecimal price,
            final BigDecimal discountRate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.skuCode = skuCode;
        this.price = price;
        this.discountRate = discountRate;
    }

    public static InventoryData instance(final Long id, final String name, final String description, final String skuCode, final BigDecimal price,
            final BigDecimal discountRate) {
        return new InventoryData(id, name, description, skuCode, price, discountRate);
    }
}
