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
package org.apache.fineract.infrastructure.documentmanagement.api.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.client.data.ClientData;

@SuppressWarnings({ "HideUtilityClassConstructor" })
public class DocumentConfigApiConstants {

    public static final String entityTypeParam = "entityType";
    public static final String entityIdParam = "entityId";

    public static final String locationParam = "location";

    public static final String idParam = "id";
    public static final String formIdParam = "formId";
    public static final String typeParam = "type";
    public static final String avatarBase64Param = "avatarBase64";
    public static final String nameParam = "name";
    public static final String descriptionParam = "description";
    public static final String settingsParam = "settings";
    public static final String settingsCodeParam = "settingsCode";
    // public static final String productIdsParam = "productIds";
    public static final String resourceName = "DOCUMENT";

    public static final String savingProductOptionsParam = "savingProductOptions";
    public static final String loanProductDatasParam = "loanProductDatas";
    public static final String clientLegalFormOptionsParam = "clientLegalFormOptions";
    public static final String globalEntityTypeParam = "globalEntityType";

    /**
     * These parameters will match the class level parameters of {@link ClientData}. Where possible, we try to get
     * response parameters to match those of request parameters.
     */
    public static final Set<String> DOCUMENT_CONFIG_TEMPLATE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(loanProductDatasParam, clientLegalFormOptionsParam, savingProductOptionsParam, settingsCodeParam));
    public static final Set<String> DOCUMENT_CONFIG_CREATE_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            // productIdsParam,
            formIdParam, typeParam, nameParam, descriptionParam, settingsParam));
    public static final Set<String> DOCUMENT_CONFIG_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(
            // productIdsParam,
            idParam, formIdParam, typeParam, nameParam, descriptionParam, settingsParam, globalEntityTypeParam));

    public static final Set<String> DOCUMENT_CREATE_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(typeParam, locationParam, descriptionParam, nameParam));

    public static final Set<String> IMAGE_CREATE_RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(avatarBase64Param));

}
