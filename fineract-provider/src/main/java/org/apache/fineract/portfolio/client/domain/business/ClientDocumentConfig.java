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
package org.apache.fineract.portfolio.client.domain.business;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.fineract.infrastructure.codes.domain.Code;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.springframework.stereotype.Component;

@Entity
@Component
@Table(name = "m_document_client_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"legal_form_id"}, name = "legal_form_id_UNIQUE"),
    @UniqueConstraint(columnNames = {"name"}, name = "name_UNIQUE")
})
public class ClientDocumentConfig extends AbstractAuditableWithUTCDateTimeCustom {

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "legal_form_id", nullable = false)
    private Integer legalFormId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "m_document_client_config_code", joinColumns = @JoinColumn(name = "m_document_client_config_id"), inverseJoinColumns = @JoinColumn(name = "code_id"))
    private Set<Code> codes = new HashSet<>();

    protected ClientDocumentConfig() {
    }

    public static ClientDocumentConfig instance(final String name, final Integer legalFormId, final String description, final boolean active) {
        return new ClientDocumentConfig(name, legalFormId, description, active);
    }

    public ClientDocumentConfig(final String name, final Integer legalFormId, final String description, final boolean active) {
        this.legalFormId = legalFormId;
        this.description = description;
        this.active = active;
        this.name = name;
    }

    public boolean updateCode(final Code code, final boolean isSelected) {
        boolean changed = false;
        if (isSelected) {
            changed = addCode(code);
        } else {
            changed = removeCode(code);
        }

        return changed;
    }

    private boolean addCode(final Code code) {
        return this.codes.add(code);
    }

    private boolean removeCode(final Code code) {
        return this.codes.remove(code);
    }

    public Set<Code> getCodes() {
        return this.codes;
    }

    public void setCodes(Set<Code> codes) {
        this.codes = codes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLegalFormId() {
        return legalFormId;
    }

    public void setLegalFormId(Integer legalFormId) {
        this.legalFormId = legalFormId;
    }

}
