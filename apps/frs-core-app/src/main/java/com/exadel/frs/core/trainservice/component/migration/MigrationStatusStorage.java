package com.exadel.frs.core.trainservice.component.migration;

import com.exadel.frs.core.trainservice.exception.MigrationAlreadyExecutingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationStatusStorage {

    private AtomicBoolean isMigrating = new AtomicBoolean(false);

    public void startMigration(){
        if (isMigrating.getAndSet(true)){
            throw new MigrationAlreadyExecutingException();
        }
        log.warn("Migration started");
    }

    public void finishMigration(){
        isMigrating.getAndSet(false);
        log.warn("Migration finished");
    }

    public boolean isMigrating(){
        return isMigrating.get();
    }

}
