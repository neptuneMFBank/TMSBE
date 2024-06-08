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
package org.apache.fineract.organisation.business.businesstime.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BusinessTimeApiResourceConstants {

    private BusinessTimeApiResourceConstants() {

    }

    public static final String RESOURCE_NAME = "BUSINESSTIME";
    public static final String RESOURCE = "businesstime";

    public static final String ROLE_ID = "roleId";
    public static final String ID = "id";
    public static final String WEEK_DAY_ID = "weekDayId";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";
    public static final String TIME_FORMAT = "timeFormat";
    public static final String LOCALE = "locale";

    public static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ROLE_ID, ID, WEEK_DAY_ID, START_TIME, END_TIME));

    public static final Set<String> RESQUEST_DATA_PARAMETERS = new HashSet<>(Arrays.asList(ROLE_ID, WEEK_DAY_ID, START_TIME, END_TIME, TIME_FORMAT, LOCALE));

}
