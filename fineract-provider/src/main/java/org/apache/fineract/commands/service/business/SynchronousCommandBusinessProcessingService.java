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
package org.apache.fineract.commands.service.business;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.commands.exception.CommandNotFoundException;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class SynchronousCommandBusinessProcessingService implements CommandBusinessProcessingService {

    private final CommandSourceRepository commandSourceRepository;


    @Override
    public void logCommandExisting(final Long commandId, final String existingJson) {
        final CommandSource commandSourceResult = this.commandSourceRepository.findById(commandId).orElseThrow(() -> new CommandNotFoundException(commandId));
        commandSourceResult.updateExistingJson(existingJson);
        this.commandSourceRepository.saveAndFlush(commandSourceResult);
    }
}
