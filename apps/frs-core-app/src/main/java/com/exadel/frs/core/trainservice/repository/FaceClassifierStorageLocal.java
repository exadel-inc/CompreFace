package com.exadel.frs.core.trainservice.repository;

import static java.lang.Thread.getAllStackTraces;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;

public class FaceClassifierStorageLocal implements FaceClassifierStorage {

    private final Map<Pair<String, String>, FaceClassifierAdapter> classifierTrainerMap = new ConcurrentHashMap();
    private final Map<Pair<String, String>, AtomicBoolean> locks = new ConcurrentHashMap();
    public static final String FACE_CLASSIFIER_ADAPTER_NAME = "faceClassifierAdapter";

    private final ApplicationContext context;

    public FaceClassifierStorageLocal(@NonNull final ApplicationContext context) {
        this.context = context;
    }

    @Override
    public FaceClassifierAdapter getFaceClassifier(final String appKey, final String modelKey) {
        return classifierTrainerMap.computeIfAbsent(Pair.of(appKey, modelKey), stringStringPair ->
                (FaceClassifierAdapter) context.getBean(FACE_CLASSIFIER_ADAPTER_NAME));
    }

    @Override
    public void lock(final String appKey, final String modelKey) {
        val lock = locks
                .computeIfAbsent(Pair.of(appKey, modelKey), stringStringPair -> new AtomicBoolean(false));
        if (lock.get()) {
            throw new ModelAlreadyLockedException("Previous retraining has not been finished yet");
        }

        lock.set(true);
    }

    @Override
    public void unlock(final String appKey, final String modelKey) {
        val lock = locks
                .getOrDefault(Pair.of(appKey, modelKey), new AtomicBoolean(false));
        if (lock.get()) {
            getAllStackTraces().keySet().stream()
                               .filter(thread -> thread.getName().equals(appKey + modelKey))
                               .findAny()
                               .ifPresent(thread -> thread.interrupt());
        }

        lock.set(false);
    }

    @Override
    public boolean isLocked(final String appKey, final String modelKey) {
        return locks
                .getOrDefault(Pair.of(appKey, modelKey), new AtomicBoolean(false)).get();
    }

    @Override
    public void removeFaceClassifier(final String appKey, final String modelKey) {
        unlock(appKey, modelKey);
        classifierTrainerMap.remove(Pair.of(appKey, modelKey));
    }
}