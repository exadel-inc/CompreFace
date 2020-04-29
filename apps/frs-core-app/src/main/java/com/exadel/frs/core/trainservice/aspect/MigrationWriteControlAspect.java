package com.exadel.frs.core.trainservice.aspect;

import com.exadel.frs.core.trainservice.component.migration.MigrationStatusStorage;
import com.exadel.frs.core.trainservice.exception.MigrationExecutionException;
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
    private void endpoint(){}

    @Pointcut("@annotation(com.exadel.frs.core.trainservice.aspect.WriteEndpoint)")
    private void write(){}

    @Around("endpoint() && write()")
    public Object writeEndpoint(ProceedingJoinPoint pjp) throws Throwable {
        if (migrationStatusStorage.isMigrating()){
            log.warn("All write endpoints temporary disabled during migration");
            throw new MigrationExecutionException();
        }
        return pjp.proceed();
    }
}
