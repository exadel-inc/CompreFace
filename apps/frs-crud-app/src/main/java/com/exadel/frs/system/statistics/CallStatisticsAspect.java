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
