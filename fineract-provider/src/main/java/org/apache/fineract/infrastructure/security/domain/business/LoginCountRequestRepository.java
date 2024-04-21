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
package org.apache.fineract.infrastructure.security.domain.business;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

@Repository
//@ConditionalOnProperty("fineract.security.2fa.enabled")
@SuppressWarnings({"MemberName"})
public class LoginCountRequestRepository {

    private final ConcurrentHashMap<String, Integer> loginRequest = new ConcurrentHashMap<>();

    public int getLoginRequestCountForUser(String username) {
        Assert.notNull(username, "User count must not be null");
        final Integer count = this.loginRequest.get(username);
        return count == null || count < 0 ? 0 : count;
    }

    public void addLoginRequestCount(String username, int count) {
        Assert.notNull(username, "User count must not be null");
        Assert.notNull(count, "Request count must not be null");
        this.loginRequest.put(username, count);
    }

    public void deleteLoginRequestCountForUser(String username) {
        this.loginRequest.remove(username);
    }
}
