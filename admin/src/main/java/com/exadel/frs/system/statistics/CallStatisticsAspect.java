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

package com.exadel.frs.system.statistics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Aspect
@Configuration
@RequiredArgsConstructor
public class CallStatisticsAspect {
    private final CallStatisticsRepository repository;

    @AfterReturning(pointcut = "@annotation(CallStatistics)", returning = "result")
    public void afterReturningSingle(JoinPoint joinPoint, IStatistics result) {
        repository.updateCallStatistics(result.getObjectType().toString(),
                UUID.randomUUID().toString(),
                result.getGuid());
    }
}
