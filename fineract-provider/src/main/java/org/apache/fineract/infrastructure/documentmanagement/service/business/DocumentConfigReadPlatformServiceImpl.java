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
package org.apache.fineract.infrastructure.documentmanagement.service.business;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.codes.data.CodeData;
import org.apache.fineract.infrastructure.codes.data.business.CodeBusinessData;
import org.apache.fineract.infrastructure.codes.service.business.CodeDocumentReadPlatformService;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.PaginationHelper;
import org.apache.fineract.infrastructure.core.service.business.SearchParametersBusiness;
import org.apache.fineract.infrastructure.core.service.database.DatabaseSpecificSQLGenerator;
import org.apache.fineract.infrastructure.documentmanagement.data.business.DocumentConfigData;
import org.apache.fineract.infrastructure.documentmanagement.exception.business.DocumentConfigNotFoundException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.infrastructure.security.utils.ColumnValidator;
import org.apache.fineract.portfolio.client.domain.ClientEnumerations;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.data.SavingsProductData;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentConfigReadPlatformServiceImpl implements DocumentConfigReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final SavingsProductReadPlatformService savingsProductReadPlatformService;
    private final CodeDocumentReadPlatformService codeReadPlatformService;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    // data mappers
    private final PaginationHelper paginationHelper;
    private final DatabaseSpecificSQLGenerator sqlGenerator;
    private final ClientDocumentMapper clientDocumentMapper = new ClientDocumentMapper();
    private final CodeMapper codeMapper = new CodeMapper();

    private final ColumnValidator columnValidator;

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @Override
    public Page<DocumentConfigData> retrieveAll(SearchParametersBusiness searchParameters) {
        this.context.authenticatedUser();
        List<Object> paramList = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder(200);
        sqlBuilder.append("select ");
        sqlBuilder.append(sqlGenerator.calcFoundRows());

        if (searchParameters != null) {
            final String typeParam = searchParameters.getType();
            if (is(typeParam, "client")) {
                sqlBuilder.append(this.clientDocumentMapper.schema());
                final String extraCriteria = buildSqlStringFromClientCriteria(searchParameters, paramList);
                if (StringUtils.isNotBlank(extraCriteria)) {
                    sqlBuilder.append(" WHERE (").append(extraCriteria).append(")");
                }
            } // else if (is(typeParam, "loans")) {
              // }
            else {
                throw new UnrecognizedQueryParamException("typeRead", typeParam);
            }

            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" ");
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit(), searchParameters.getOffset()));
                } else {
                    sqlBuilder.append(sqlGenerator.limit(searchParameters.getLimit()));
                }
            }
            return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlBuilder.toString(), paramList.toArray(),
                    this.clientDocumentMapper);
        }
        return new Page<>(new ArrayList<>(), 0);
    }

    @Override
    public DocumentConfigData retrieveDocumentConfigViaClientLegalForm(Integer formId) {
        this.context.authenticatedUser();
        try {
            final String sql = "select " + this.clientDocumentMapper.schema() + " where mdc.legal_form_id = ?";
            final DocumentConfigData documentConfigData = this.jdbcTemplate.queryForObject(sql, this.clientDocumentMapper, // NOSONAR
                    formId);
            if (documentConfigData != null) {
                Collection<CodeBusinessData> codeBusinessDatas = retrieveAllCodesForClientDocument(documentConfigData.getId());
                documentConfigData.setSettings(codeBusinessDatas);
            }
            return documentConfigData;
        } catch (final EmptyResultDataAccessException e) {
            log.warn("retrieveDocumentConfigViaClientLegalForm: {}", e);
            return null;
        }
    }

    @Override
    public DocumentConfigData retrieveOne(Long documentId, String type) {
        this.context.authenticatedUser();
        try {

            DocumentConfigData documentConfigData;
            if (is(type, "client")) {
                final String sql = "select " + this.clientDocumentMapper.schema() + " where mdc.id = ?";
                documentConfigData = this.jdbcTemplate.queryForObject(sql, this.clientDocumentMapper, // NOSONAR
                        documentId);
                if (documentConfigData != null) {
                    Collection<CodeBusinessData> codeBusinessDatas = retrieveAllCodesForClientDocument(documentId);
                    documentConfigData.setSettings(codeBusinessDatas);
                }
            } // else if (is(typeParam, "loans")) {
              // }
            else {
                throw new UnrecognizedQueryParamException("typeRetrieveOne", type);
            }

            return documentConfigData;
        } catch (final EmptyResultDataAccessException e) {
            throw new DocumentConfigNotFoundException(type, documentId);
        }
    }

    public Collection<CodeBusinessData> retrieveAllCodesForClientDocument(final Long clientDocumentId) {
        this.context.authenticatedUser();
        try {
            final CodeMapper rm = new CodeMapper();
            final String sql = "select " + codeMapper.codeClientDocumentSchema()
                    + " where mdca.code_allow=1 group by c.id order by c.code_name ";

            return this.jdbcTemplate.query(sql, rm, new Object[] { clientDocumentId });

        } catch (DataAccessException e) {
            log.warn("retrieveAllCodesForClientDocument: {}", e);
            return null;
        }
    }

    private static final class ClientDocumentMapper implements RowMapper<DocumentConfigData> {

        private final String schema;

        ClientDocumentMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(400);

            sqlBuilder.append(" mdc.id, mdc.name, mdc.legal_form_id as legalFormId, mdc.active, mdc.description ");
            sqlBuilder.append(" from m_document_client_config mdc ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

        @Override
        public DocumentConfigData mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            final Long id = JdbcSupport.getLongDefaultToNullIfZero(rs, "id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
            final Integer legalFormId = JdbcSupport.getIntegerDefaultToNullIfZero(rs, "legalFormId");
            final boolean active = rs.getBoolean("active");
            final DocumentConfigData configData = new DocumentConfigData(id, name, description, active);
            configData.setLegalFormId(legalFormId);
            return configData;
        }
    }

    private static final class CodeMapper implements RowMapper<CodeBusinessData> {

        public String codeClientDocumentSchema() {
            // for this isActive is use to check if value was selected for the configuration or not
            return " GROUP_CONCAT(cv.code_value) as concatCodeValues,   c.id as id, c.code_name as code_name, if(isnull(rp.m_document_client_config_id), false, true) as systemDefined "
                    + " from m_code as c join m_document_code_allow mdca ON mdca.code_id=c.id "
                    + " left join m_code_value cv on cv.code_id=c.id "
                    + " left join m_document_client_config_code rp on rp.code_id = c.id and rp.m_document_client_config_id=? "
            // + " group by c.id order by c.code_name"
            ;
        }

        // public String schema() {
        // return " c.id as id, c.code_name as code_name, c.is_system_defined as systemDefined from m_code c ";
        // }
        @Override
        public CodeBusinessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String code_name = rs.getString("code_name");
            final boolean systemDefined = rs.getBoolean("systemDefined");

            CodeBusinessData codeData = CodeBusinessData.instance(id, code_name, systemDefined);

            codeData.setValues(rs.getString("concatCodeValues"));
            System.out.println("info: {}" + rs.getString("concatCodeValues"));

            return codeData;
        }
    }

    @Override
    public DocumentConfigData retrieveTemplate() {
        this.context.authenticatedUser();
        final Collection<CodeData> codeDatas = this.codeReadPlatformService.retrieveAllCodesDocument();

        final Collection<SavingsProductData> savingsProductDatas = this.savingsProductReadPlatformService.retrieveAllForLookup();
        final Collection<LoanProductData> loanProductDatas = this.loanProductReadPlatformService.retrieveAllLoanProductsForLookup(true);
        final List<EnumOptionData> clientLegalFormOptions = ClientEnumerations.legalForm(LegalForm.values());
        return DocumentConfigData.template(codeDatas, clientLegalFormOptions, loanProductDatas, savingsProductDatas);
    }

    private String buildSqlStringFromClientCriteria(final SearchParametersBusiness searchParameters, List<Object> paramList) {

        final String displayName = searchParameters.getName();
        final Boolean active = searchParameters.isActive();

        String extraCriteria = "";

        // if (searchParameters.isFromDatePassed() || searchParameters.isToDatePassed()) {
        // final LocalDate startPeriod = searchParameters.getFromDate();
        // final LocalDate endPeriod = searchParameters.getToDate();
        // final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // if (startPeriod != null && endPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) BETWEEN ? AND ? ";
        // paramList.add(df.format(startPeriod));
        // paramList.add(df.format(endPeriod));
        // } else if (startPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) >= ? ";
        // paramList.add(df.format(startPeriod));
        // } else if (endPeriod != null) {
        // extraCriteria += " and CAST(mdc.submittedon_date AS DATE) <= ? ";
        // paramList.add(df.format(endPeriod));
        // }
        // }
        if (displayName != null) {
            paramList.add("%" + displayName + "%");
            extraCriteria += " and mdc.name like ? ";
        }

        if (searchParameters.isActivePassed()) {
            extraCriteria += " and mdc.active = ? ";
            paramList.add(active);
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }

}
