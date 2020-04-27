package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.val;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class FaceClassifierLockManager {

    private Map<Pair<String, String>, AtomicBoolean> locks;

    @PostConstruct
    public void postConstruct() {
        locks = new ConcurrentHashMap<>();
    }

    public void lock(String appKey, String modelId) {
        var lock = locks
                .computeIfAbsent(Pair.of(appKey, modelId), stringStringPair -> new AtomicBoolean(false));
        if (lock.get()) {
            throw new ModelAlreadyLockedException("Previous retraining has not been finished yet");
        }

        lock.set(true);
    }

    public void unlock(final String appKey, final String modelId) {
        val lock = locks
                .getOrDefault(Pair.of(appKey, modelId), new AtomicBoolean(false));
        if (lock.get()) {
            for (val thread : Thread.getAllStackTraces().keySet()) {
                if (thread.getName().equals(appKey + modelId)) {
                    thread.interrupt();
                }
            }
        }
        lock.set(false);
    }

    public boolean isLocked(final String appKey, final String modelId) {
        return locks
                .getOrDefault(Pair.of(appKey, modelId), new AtomicBoolean(false)).get();
    }

}
