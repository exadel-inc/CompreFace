package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class FaceClassifierLockManager {

    private Map<String, AtomicBoolean> locks;

    @Getter
    private volatile CountDownLatch countDownLatch;

    @PostConstruct
    public void postConstruct() {
        locks = new ConcurrentHashMap<>();
        countDownLatch = new CountDownLatch(0);
    }

    public synchronized void lock(String modelKey) {
        var lock = locks
                .computeIfAbsent(modelKey, stringStringPair -> new AtomicBoolean(false));
        if (lock.get()) {
            throw new ModelAlreadyLockedException("Previous retraining has not been finished yet");
        }

        lock.set(true);
        countDownLatch = new CountDownLatch((int) (countDownLatch.getCount() + 1));
        log.debug("Model {} locked, models locked : {}", modelKey,  countDownLatch.getCount());
    }

    public synchronized void unlock(final String modelKey) {
        val lock = locks
                .getOrDefault(modelKey, new AtomicBoolean(false));
        if (lock.get()) {
            for (val thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().equals(modelKey)) {
                    thread.interrupt();
                }
            }
        }
        lock.set(false);
        countDownLatch.countDown();
        log.debug("Model {} unlocked, models locked : {}", modelKey,  countDownLatch.getCount());
    }

    public boolean isLocked(final String modelKey) {
        return locks
                .getOrDefault(modelKey, new AtomicBoolean(false)).get();
    }

}
