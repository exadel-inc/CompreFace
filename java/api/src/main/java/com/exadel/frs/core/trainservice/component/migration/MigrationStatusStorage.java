/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.component.migration;

import java.util.concurrent.atomic.AtomicBoolean;

import com.exadel.frs.commonservice.exception.MigrationAlreadyExecutingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationStatusStorage {

    private AtomicBoolean isMigrating = new AtomicBoolean(false);

    public void startMigration() {
        if (isMigrating.getAndSet(true)) {
            throw new MigrationAlreadyExecutingException();
        }
        log.warn("Migration started");
    }

    public void finishMigration() {
        isMigrating.getAndSet(false);
        log.warn("Migration finished");
    }

    public boolean isMigrating() {
        return isMigrating.get();
    }
}
