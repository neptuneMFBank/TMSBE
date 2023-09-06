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
package org.apache.fineract.portfolio.client.data.business;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.api.business.ClientBusinessApiConstants;

public class ClientIdentifierBusinessApiCollectionConstants extends ClientApiConstants {

    public static final String resourceIdParam = "resourceId";
    public static final String documentTypeIdParam = "documentTypeId";
    public static final String documentKeyParam = "documentKey";
    public static final String descriptionParam = "description";
    public static final String locationParam = "location";
    public static final String typeParam = "type";

    public static final Set<String> CLIENT_IDENTIFIER_BUSINESS_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(ClientBusinessApiConstants.clientIdParamName, documentTypeIdParam, documentKeyParam, descriptionParam, locationParam, typeParam));

    public static final String resourceNameForPermissions = "CLIENTIDENTIFIER";

}
