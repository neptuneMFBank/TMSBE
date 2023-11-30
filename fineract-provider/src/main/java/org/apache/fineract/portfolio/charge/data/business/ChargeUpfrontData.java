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
package org.apache.fineract.portfolio.charge.data.business;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.MonthDay;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object for charge data.
 */
public final class ChargeUpfrontData implements Comparable<ChargeUpfrontData>, Serializable {

    private final Long id;
    private final String name;
    private final boolean active;
    private final CurrencyData currency;
    private final BigDecimal amount;
    private final EnumOptionData chargeAppliesTo;
    private final EnumOptionData chargeCalculationType;

    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> chargeCalculationTypeOptions;//
    private final List<EnumOptionData> chargeAppliesToOptions;//

    private final List<EnumOptionData> loanChargeCalculationTypeOptions;

    public static ChargeUpfrontData template(final Collection<CurrencyData> currencyOptions,
            final List<EnumOptionData> chargeCalculationTypeOptions, final List<EnumOptionData> chargeAppliesToOptions
    ) {

        return new ChargeUpfrontData(null, null, null, null, null, null, null, null, false, false, false, null, null, null, false, null,
                currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions
        );
    }

    public static ChargeUpfrontData withTemplate(final ChargeUpfrontData charge, final ChargeUpfrontData template) {
        return new ChargeUpfrontData(charge.id, charge.name, charge.amount, charge.currency, charge.chargeTimeType, charge.chargeAppliesTo,
                charge.chargeCalculationType, charge.chargePaymentMode, charge.penalty, charge.active, charge.freeWithdrawal,
                charge.freeWithdrawalChargeFrequency, charge.restartFrequency, charge.restartFrequencyEnum, charge.isPaymentType,
                charge.paymentTypeOptions, charge.taxGroup, template.currencyOptions, template.chargeCalculationTypeOptions,
                template.chargeAppliesToOptions, template.chargeTimeTypeOptions, template.chargePaymetModeOptions,
                template.loanChargeCalculationTypeOptions, template.loanChargeTimeTypeOptions, template.savingsChargeCalculationTypeOptions,
                template.savingsChargeTimeTypeOptions, template.clientChargeCalculationTypeOptions, template.clientChargeTimeTypeOptions,
                charge.feeOnMonthDay, charge.feeInterval, charge.minCap, charge.maxCap, charge.feeFrequency, template.feeFrequencyOptions,
                charge.incomeOrLiabilityAccount, template.incomeOrLiabilityAccountOptions, template.taxGroupOptions,
                template.shareChargeCalculationTypeOptions, template.shareChargeTimeTypeOptions, template.accountMappingForChargeConfig,
                template.expenseAccountOptions, template.assetAccountOptions);
    }

    public static ChargeUpfrontData instance(final Long id, final String name, final BigDecimal amount, final CurrencyData currency,
            final EnumOptionData chargeTimeType, final EnumOptionData chargeAppliesTo, final EnumOptionData chargeCalculationType,
            final EnumOptionData chargePaymentMode, final MonthDay feeOnMonthDay, final Integer feeInterval, final boolean penalty,
            final boolean active, final boolean freeWithdrawal, final Integer freeWithdrawalChargeFrequency, final Integer restartFrequency,
            final Integer restartFrequencyEnum, final boolean isPaymentType, final PaymentTypeData paymentTypeOptions,
            final BigDecimal minCap, final BigDecimal maxCap, final EnumOptionData feeFrequency, final GLAccountData accountData,
            TaxGroupData taxGroupData) {

        final Collection<CurrencyData> currencyOptions = null;
        final List<EnumOptionData> chargeCalculationTypeOptions = null;
        final List<EnumOptionData> chargeAppliesToOptions = null;
        final List<EnumOptionData> chargeTimeTypeOptions = null;
        final List<EnumOptionData> chargePaymentModeOptions = null;
        final List<EnumOptionData> loansChargeCalculationTypeOptions = null;
        final List<EnumOptionData> loansChargeTimeTypeOptions = null;
        final List<EnumOptionData> savingsChargeCalculationTypeOptions = null;
        final List<EnumOptionData> savingsChargeTimeTypeOptions = null;
        final List<EnumOptionData> feeFrequencyOptions = null;
        final List<EnumOptionData> clientChargeCalculationTypeOptions = null;
        final List<EnumOptionData> clientChargeTimeTypeOptions = null;
        final Map<String, List<GLAccountData>> incomeOrLiabilityAccountOptions = null;
        final List<EnumOptionData> shareChargeCalculationTypeOptions = null;
        final List<EnumOptionData> shareChargeTimeTypeOptions = null;
        final Collection<TaxGroupData> taxGroupOptions = null;
        final String accountMappingForChargeConfig = null;
        final List<GLAccountData> expenseAccountOptions = null;
        final List<GLAccountData> assetAccountOptions = null;
        return new ChargeUpfrontData(id, name, active, currency, amount, chargeAppliesTo, chargeCalculationType, currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions, loansChargeCalculationTypeOptions);
    }

    public static ChargeUpfrontData lookup(final Long id, final String name) {
        final BigDecimal amount = null;
        final CurrencyData currency = null;
        final EnumOptionData chargeAppliesTo = null;
        final EnumOptionData chargeCalculationType = null;
        final Collection<CurrencyData> currencyOptions = null;
        final List<EnumOptionData> chargeCalculationTypeOptions = null;
        final List<EnumOptionData> chargeAppliesToOptions = null;
        final List<EnumOptionData> loansChargeCalculationTypeOptions = null;
        return new ChargeUpfrontData(id, name, true, currency, amount, chargeAppliesTo, chargeCalculationType, currencyOptions, chargeCalculationTypeOptions, chargeAppliesToOptions, loansChargeCalculationTypeOptions);
    }

    private ChargeUpfrontData(Long id, String name, boolean active, CurrencyData currency, BigDecimal amount, EnumOptionData chargeAppliesTo, EnumOptionData chargeCalculationType, Collection<CurrencyData> currencyOptions, List<EnumOptionData> chargeCalculationTypeOptions, List<EnumOptionData> chargeAppliesToOptions, List<EnumOptionData> loanChargeCalculationTypeOptions) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.currency = currency;
        this.amount = amount;
        this.chargeAppliesTo = chargeAppliesTo;
        this.chargeCalculationType = chargeCalculationType;
        this.currencyOptions = currencyOptions;
        this.chargeCalculationTypeOptions = chargeCalculationTypeOptions;
        this.chargeAppliesToOptions = chargeAppliesToOptions;
        this.loanChargeCalculationTypeOptions = loanChargeCalculationTypeOptions;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof ChargeUpfrontData)) {
            return false;
        }
        final ChargeUpfrontData chargeData = (ChargeUpfrontData) obj;
        return this.id.equals(chargeData.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public int compareTo(final ChargeUpfrontData obj) {
        if (obj == null) {
            return -1;
        }

        return obj.id.compareTo(this.id);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EnumOptionData getChargeCalculationType() {
        return chargeCalculationType;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public CurrencyData getCurrency() {
        return currency;
    }
}
