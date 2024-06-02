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
package org.apache.fineract.portfolio.address.service.business;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.codes.data.business.CodeValueBusinessData;
import org.apache.fineract.infrastructure.codes.service.business.CodeValueBusinessReadPlatformService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.address.data.business.AddressBusinessData;
import org.apache.fineract.portfolio.address.exception.AddressNotFoundException;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AddressBusinessReadPlatformServiceImpl implements AddressBusinessReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final CodeValueBusinessReadPlatformService readService;

    @Autowired
    public AddressBusinessReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final CodeValueBusinessReadPlatformService readService) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.readService = readService;
    }

    @Override
    public AddressBusinessData retrieveOneAddress(long clientId, long id) {
        try {
            this.context.authenticatedUser();

            final AddMapper rm = new AddMapper();
            final String sql = "select " + rm.schema() + " where ca.client_id=? and addr.id=? ";

            return this.jdbcTemplate.queryForObject(sql, rm, clientId, id);

        } catch (final DataAccessException e) {
            throw new AddressNotFoundException(clientId, id);
        }
    }

    private static final class AddMapper implements RowMapper<AddressBusinessData> {

        public String schema() {
            return "cv2.code_value as addressType,ca.client_id as client_id,addr.id as id,ca.address_type_id as addresstyp,ca.is_active as is_active,addr.street as street,addr.address_line_1 as address_line_1,addr.address_line_2 as address_line_2,"
                    + "addr.address_line_3 as address_line_3,addr.town_village as town_village, addr.city as city,addr.county_district as county_district,"
                    + "addr.state_province_id as state_province_id,cv.code_value as state_name, addr.country_id as country_id,c.code_value as country_name,addr.postal_code as postal_code,addr.latitude as latitude,"
                    + "addr.longitude as longitude,addr.created_by as created_by,addr.created_on as created_on,addr.updated_by as updated_by,"
                    + "addr.updated_on as updated_on, mao.date_moved_in dateMovedIn, mao.residence_status_id as residentStatusId, cvv.code_value as residentStatus, mao.lga_id as lgaId, cvvv.code_value as lgaName"
                    + " from m_address addr left join m_code_value cv on addr.state_province_id=cv.id"
                    + " left join  m_code_value c on addr.country_id=c.id" + " join m_client_address ca on addr.id= ca.address_id"
                    + " left join m_code_value cv2 on ca.address_type_id=cv2.id"
                    + " left join m_address_other mao on mao.address_id=addr.id"
                    + " left join m_code_value cvv on mao.residence_status_id=cvv.id"
                    + " left join m_code_value cvvv on mao.lga_id=cvvv.id";

        }

        @Override
        public AddressBusinessData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long lgaId = rs.getLong("lgaId");
            CodeValueData lga = null;
            if (lgaId > 0) {
                final String lgaName = rs.getString("lgaName");
                lga = CodeValueData.instance(lgaId, lgaName);
            }
            final Long residentStatusId = rs.getLong("residentStatusId");
            CodeValueData residentStatus = null;
            if (residentStatusId > 0) {
                final String residentStatusName = rs.getString("residentStatus");
                residentStatus = CodeValueData.instance(residentStatusId, residentStatusName);
            }
            final Date dateMovedIn = rs.getDate("dateMovedIn");
            final LocalDate localDateMovedIn = dateMovedIn != null ? dateMovedIn.toLocalDate() : null;

            final String addressType = rs.getString("addressType");
            final long addressId = rs.getLong("id");

            final long client_id = rs.getLong("client_id");

            final String street = rs.getString("street");

            final long address_type_id = rs.getLong("addresstyp");

            final boolean is_active = rs.getBoolean("is_active");

            final String address_line_1 = rs.getString("address_line_1");

            final String address_line_2 = rs.getString("address_line_2");

            final String address_line_3 = rs.getString("address_line_3");

            final String town_village = rs.getString("town_village");

            final String city = rs.getString("city");

            final String county_district = rs.getString("county_district");

            final long state_province_id = rs.getLong("state_province_id");

            final long country_id = rs.getLong("country_id");

            final String country_name = rs.getString("country_name");

            final String state_name = rs.getString("state_name");

            final String postal_code = rs.getString("postal_code");

            final BigDecimal latitude = rs.getBigDecimal("latitude");

            final BigDecimal longitude = rs.getBigDecimal("longitude");

            final String created_by = rs.getString("created_by");

            final Date created_on = rs.getDate("created_on");

            final LocalDate created_on_local_date = created_on != null ? created_on.toLocalDate() : null;

            final String updated_by = rs.getString("updated_by");

            final Date updated_on = rs.getDate("updated_on");

            final LocalDate update_on_local_date = updated_on != null ? updated_on.toLocalDate() : null;

            return AddressBusinessData.instance(addressType, client_id, addressId, address_type_id, is_active, street, address_line_1,
                    address_line_2, address_line_3, town_village, city, county_district, state_province_id, country_id, state_name,
                    country_name, postal_code, latitude, longitude, created_by, created_on_local_date, updated_by, update_on_local_date,
                    residentStatus, localDateMovedIn, lga);
        }
    }

    @Override
    public Collection<AddressBusinessData> retrieveAllClientAddress(final long clientid) {
        this.context.authenticatedUser();
        final AddMapper rm = new AddMapper();
        final String sql = "select " + rm.schema() + " where ca.client_id=?";
        return this.jdbcTemplate.query(sql, rm, new Object[]{clientid}); // NOSONAR
    }

    @Override
    public Collection<AddressBusinessData> retrieveAddressbyType(final long clientid, final long typeid) {
        this.context.authenticatedUser();

        final AddMapper rm = new AddMapper();
        final String sql = "select " + rm.schema() + " where ca.client_id=? and ca.address_type_id=?";

        return this.jdbcTemplate.query(sql, rm, new Object[]{clientid, typeid}); // NOSONAR
    }

    @Override
    public Collection<AddressBusinessData> retrieveAddressbyTypeAndStatus(final long clientid, final long typeid, final String status) {
        this.context.authenticatedUser();
        boolean temp = Boolean.parseBoolean(status);

        final AddMapper rm = new AddMapper();
        final String sql = "select " + rm.schema() + " where ca.client_id=? and ca.address_type_id=? and ca.is_active=?";

        return this.jdbcTemplate.query(sql, rm, new Object[]{clientid, typeid, temp}); // NOSONAR
    }

    @Override
    public Collection<AddressBusinessData> retrieveAddressbyStatus(final long clientid, final String status) {
        this.context.authenticatedUser();
        boolean temp = Boolean.parseBoolean(status);

        final AddMapper rm = new AddMapper();
        final String sql = "select " + rm.schema() + " where ca.client_id=? and ca.is_active=?";

        return this.jdbcTemplate.query(sql, rm, new Object[]{clientid, temp}); // NOSONAR
    }

    @Override
    public AddressBusinessData retrieveTemplate() {
        final List<CodeValueBusinessData> countryoptions = new ArrayList<>(this.readService.retrieveCodeValuesByCode("COUNTRY"));

        final List<CodeValueBusinessData> StateOptions = new ArrayList<>(this.readService.retrieveCodeValuesByCode("STATE"));

        final List<CodeValueBusinessData> addressTypeOptions = new ArrayList<>(this.readService.retrieveCodeValuesByCode("ADDRESS_TYPE"));
        final List<CodeValueBusinessData> lgaOptions = new ArrayList<>(
                this.readService.retrieveCodeValuesByCode(ClientBusinessApiConstants.LGAPARAM));
        final List<CodeValueBusinessData> residentOptions = new ArrayList<>(
                this.readService.retrieveCodeValuesByCode(ClientBusinessApiConstants.ResidentPARAM));

        return AddressBusinessData.template(countryoptions, StateOptions, addressTypeOptions, lgaOptions, residentOptions);
    }
}
