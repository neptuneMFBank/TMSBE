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
package org.apache.fineract.useradministration.domain.business;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.useradministration.domain.AppUser;

@Entity
@Table(name = "m_appuser_extension")
public class AppUserExtension extends AbstractPersistableCustom {

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "appuser_id", nullable = false)
    private AppUser appUser;

    @Column(name = "is_merchant", nullable = false)
    private Boolean isMerchant;

    public AppUserExtension() {

    }

    public AppUserExtension(AppUser appUser, Boolean isMerchant) {
        this.appUser = appUser;
        this.isMerchant = isMerchant;
    }

    public Boolean isMerchant() {
        return isMerchant;
    }

    @Override
    public boolean equals(Object obj) {

        if (null == obj) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AppUserExtension)) {
            return false;
        }

        AppUserExtension that = (AppUserExtension) obj;

        return null == this.appUser.getId() ? false : this.appUser.getId().equals(that.appUser.getId());
    }

    @Override
    public int hashCode() {

        int hashCode = 17;

        hashCode += null == this.appUser ? 0 : this.appUser.getId().hashCode() * 31;

        return hashCode;
    }

}
