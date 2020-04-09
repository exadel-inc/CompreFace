package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;

public class FaceClassifierStorageLocal implements FaceClassifierStorage {

    private ApplicationContext context;
    private Map<Pair<String, String>, FaceClassifierProxy> classifierTrainerMap;
    private Map<Pair<String, String>, AtomicBoolean> locks;
    public static final String FACE_CLASSIFIER_PROXY_NAME = "faceClassifierProxy";

    @Autowired
    public FaceClassifierStorageLocal(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void postConstruct() {
        classifierTrainerMap = new ConcurrentHashMap<>();
        locks = new ConcurrentHashMap<>();
    }

    public FaceClassifierProxy getFaceClassifier(final String appKey, final String modelId) {
        return classifierTrainerMap.computeIfAbsent(Pair.of(appKey, modelId), stringStringPair ->
                (FaceClassifierProxy) context.getBean(FACE_CLASSIFIER_PROXY_NAME));
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

    @Override
    public void removeFaceClassifier(final String appKey, final String modelId) {
        unlock(appKey, modelId);
        classifierTrainerMap.remove(Pair.of(appKey, modelId));
    }
}