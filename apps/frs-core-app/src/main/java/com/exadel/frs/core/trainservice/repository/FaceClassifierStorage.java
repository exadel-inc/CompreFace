package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.component.FaceClassifierProxy;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;

public interface FaceClassifierStorage {

    FaceClassifierProxy getFaceClassifier(String appKey, String modelId);

    /**
     * Throws {@link ModelAlreadyLockedException} if model already locked
     */
    void lock(String appKey, String modelId) throws ModelAlreadyLockedException;

    void unlock(String appKey, String modelId);

    boolean isLocked(String appKey, String modelId);

    void removeFaceClassifier(String appKey, String modelId);
}