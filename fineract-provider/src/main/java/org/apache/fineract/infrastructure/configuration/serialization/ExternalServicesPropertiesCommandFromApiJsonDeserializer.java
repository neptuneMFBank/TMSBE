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
package org.apache.fineract.infrastructure.configuration.serialization;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.NotificationJSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.S3JSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMSJSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.ExternalServicesConstants.SMTPJSONinputParams;
import org.apache.fineract.infrastructure.configuration.service.business.ExternalServicesBusinessConstants.AzureJSONinputParams;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExternalServicesPropertiesCommandFromApiJsonDeserializer {

    private final Set<String> azureSupportedParameters = AzureJSONinputParams.getAllValues();
    private final Set<String> s3SupportedParameters = S3JSONinputParams.getAllValues();
    private final Set<String> smtpSupportedParameters = SMTPJSONinputParams.getAllValues();
    private final Set<String> smsSupportedParameters = SMSJSONinputParams.getAllValues();
    private final Set<String> notificationSupportedParameters = NotificationJSONinputParams.getAllValues();
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ExternalServicesPropertiesCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForUpdate(final String json, final String externalServiceName) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        switch (externalServiceName) {
            case "Azure":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.azureSupportedParameters);
            break;

            case "S3":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.s3SupportedParameters);
            break;

            case "SMTP":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.smtpSupportedParameters);
            break;

            case "SMS":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.smsSupportedParameters);
            break;

            case "NOTIFICATION":
                this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.notificationSupportedParameters);
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(externalServiceName);
        }

    }

    public Set<String> getNameKeys(final String json) {
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, String> jsonMap = this.fromApiJsonHelper.extractDataMap(typeOfMap, json);
        Set<String> keyNames = jsonMap.keySet();
        return keyNames;
    }
}
