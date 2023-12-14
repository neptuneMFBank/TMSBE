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
package org.apache.fineract.portfolio.business.employer.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class EmployerApiResourceConstants {

    private EmployerApiResourceConstants() {

    }

    public static final String RESOURCENAME = "EMPLOYER";
    public static final String ADDRESS_OPTIONS = "addressOptions";
    public static final String CLIENT_CLASSIFICATION_ID = "clientClassificationId";
    public static final String CLIENT_CLASSIFICATION = "clientClassification";
    public static final String CLIENT_CLASSIFICATION_OPTIONS = "clientClassificationOptions";
    public static final String INDUSTRY_OPTIONS = "industryOptions";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SLUG = "slug";
    public static final String RCNUMBER = "rcNumber";
    public static final String STATE = "state";
    public static final String STATEID = "stateId";
    public static final String COUNTRY = "country";
    public static final String COUNTRYID = "countryId";
    public static final String EXTERNALID = "externalId";
    public static final String BUSINESSID = "businessId";
    public static final String BUSINESS = "business";
    public static final String INDUSTRY = "industry";
    public static final String INDUSTRYID = "industryId";
    public static final String LGA = "lga";
    public static final String LGAID = "lgaId";
    public static final String OFFICEADDRESS = "officeAddress";
    public static final String NEARESTLANDMARK = "nearestLandMark";
    public static final String ACTIVE = "active";

    public static final String MOBILE_NO = "mobileNo";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String EMAIL_EXTENSION = "emailExtension";
    public static final String CONTACT_PERSON = "contactPerson";

    public static final String STAFF_ID = "staffId";
    public static final String STAFF_DATA = "staffData";
    public static final String SUPERVISOR_STAFF_DATA = "supervisorStaffData";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ID, NAME, SLUG, RCNUMBER, STATE, COUNTRY, LGA,
            OFFICEADDRESS, NEARESTLANDMARK, ACTIVE, MOBILE_NO, EMAIL_ADDRESS, EMAIL_EXTENSION, CONTACT_PERSON, INDUSTRY,
            CLIENT_CLASSIFICATION, BUSINESS, EXTERNALID, STAFF_DATA, SUPERVISOR_STAFF_DATA));

    public static final Set<String> REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, SLUG, RCNUMBER, STATEID, COUNTRYID, LGAID, OFFICEADDRESS, NEARESTLANDMARK, ACTIVE, MOBILE_NO, EMAIL_ADDRESS,
                    EMAIL_EXTENSION, CONTACT_PERSON, INDUSTRYID, CLIENT_CLASSIFICATION_ID, BUSINESSID, EXTERNALID, STAFF_ID));

    public static final Set<String> REQUEST_UPDATE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(NAME, SLUG, RCNUMBER, STATEID, COUNTRYID, LGAID, OFFICEADDRESS, NEARESTLANDMARK, MOBILE_NO, EMAIL_ADDRESS,
                    EMAIL_EXTENSION, CONTACT_PERSON, INDUSTRYID, CLIENT_CLASSIFICATION_ID, BUSINESSID, EXTERNALID, STAFF_ID));

    public static final Set<String> RESPONSE_TEMPLATE_PARAMETERS = new HashSet<>(
            Arrays.asList(ADDRESS_OPTIONS, CLIENT_CLASSIFICATION_OPTIONS, INDUSTRY_OPTIONS));

}
