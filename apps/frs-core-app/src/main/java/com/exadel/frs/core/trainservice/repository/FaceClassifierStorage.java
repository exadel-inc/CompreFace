package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;

public interface FaceClassifierStorage {

    FaceClassifierProxy getFaceClassifier(String appKey, String modelKey);

    /**
     * Throws {@link ModelAlreadyLockedException} if model already locked
     */
    void lock(String appKey, String modelKey) throws ModelAlreadyLockedException;

    void unlock(String appKey, String modelKey);

    boolean isLocked(String appKey, String modelKey);

    void removeFaceClassifier(String appKey, String modelKey);
}