package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;

public interface FaceClassifierStorage {

    FaceClassifierAdapter getFaceClassifier(final String appKey, final String modelKey);

    /**
     * Throws {@link ModelAlreadyLockedException} if model already locked
     */
    void lock(final String appKey, final String modelKey) throws ModelAlreadyLockedException;

    void unlock(final String appKey, final String modelKey);

    boolean isLocked(final String appKey, final String modelKey);

    void removeFaceClassifier(final String appKey, final String modelKey);
}