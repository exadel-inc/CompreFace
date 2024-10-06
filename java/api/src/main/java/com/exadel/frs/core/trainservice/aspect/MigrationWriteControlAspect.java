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

package com.exadel.frs.core.trainservice.aspect;

import com.exadel.frs.commonservice.exception.MigrationExecutionException;
import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationWriteControlAspect {

    private final MigrationStatusStorage migrationStatusStorage;

    @Pointcut("within(com.exadel.frs.core.trainservice.controller..*)")
    private void endpoint() {
    }

    @Pointcut("@annotation(com.exadel.frs.core.trainservice.aspect.WriteEndpoint)")
    private void write() {
    }

    @Around("endpoint() && write()")
    public Object writeEndpoint(final ProceedingJoinPoint pjp) throws Throwable {
        if (migrationStatusStorage.isMigrating()) {
            log.warn("All write endpoints temporary disabled during migration");
            throw new MigrationExecutionException();
        }

        return pjp.proceed();
    }
}
