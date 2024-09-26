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
package org.apache.fineract.portfolio.business.kyc.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class KycConfigApiConstants {

    public static final String KYC_CONFIG_RESOURCE_NAME = "kycConfig";
    public static final String idParamName = "id";
    public static final String kycTierParamName = "kycTier";
    public static final String KycConfigCodeValueIdParamName = "kycConfigCodeValueId";
    public static final String KycParamCodeValueIdsParamName = "kycParamCodeValueIds";
    public static final String descriptionParamName = "description";
    public static final String kycCodeParamName = "kycParam";
    public static final String kycLevelParamName = "ClientType";
    public static final String kycParamName = "kycParam";
    public static final String kycParamOptions = "kycParamOptions";
    public static final String kycLevelOptions = "kycLevelOptions";

    protected static final Set<String> KYC_CONFIG_CREATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(KycConfigCodeValueIdParamName, descriptionParamName, KycParamCodeValueIdsParamName));
    protected static final Set<String> KYC_CONFIG_UPDATE_REQUEST_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(KycParamCodeValueIdsParamName, descriptionParamName));
    public static final Set<String> KYC_CONFIG_RESPONSE_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList(kycCodeParamName, descriptionParamName, kycTierParamName, idParamName, kycParamOptions, kycLevelOptions));
}
