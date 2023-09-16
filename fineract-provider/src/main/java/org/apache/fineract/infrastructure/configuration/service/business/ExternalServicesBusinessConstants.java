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
package org.apache.fineract.infrastructure.configuration.service.business;

import java.util.HashSet;
import java.util.Set;

public final class ExternalServicesBusinessConstants {

    private ExternalServicesBusinessConstants() {

    }

    public static final String AZURE_SERVICE_NAME = "Azure";
    public static final String AZURE_ACCOUNT_KEY = "accountKey";
    public static final String AZURE_ACCOUNT_NAME = "accountName";
    public static final String AZURE_ENDPOINT_SUFFIX = "endpointSuffix";
    public static final String AZURE_CONTAINER_NAME = "containerName";

    public enum AzureJSONinputParams {

        AZURE_ACCOUNT_KEY("accountKey"), AZURE_ACCOUNT_NAME("accountName"), AZURE_ENDPOINT_SUFFIX("endpointSuffix"), AZURE_CONTAINER_NAME("containerName");

        private final String value;

        AzureJSONinputParams(final String value) {
            this.value = value;
        }

        private static final Set<String> values = new HashSet<>();

        static {
            for (final AzureJSONinputParams type : AzureJSONinputParams.values()) {
                values.add(type.value);
            }
        }

        public static Set<String> getAllValues() {
            return values;
        }

        @Override
        public String toString() {
            return name().replaceAll("_", " ");
        }

        public String getValue() {
            return this.value;
        }
    }
}
