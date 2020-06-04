package com.exadel.frs.core.trainservice.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class FaceClassifierLockManagerTest {

    private FaceClassifierLockManager lockManager;

    private static final String MODEL_KEY = "model_key";

    @BeforeEach
    public void beforeEach() {
        lockManager = new FaceClassifierLockManager();
        lockManager.postConstruct();
    }

    @Test
    public void lock() {
        lockManager.lock(MODEL_KEY);

        assertThrows(RuntimeException.class, () -> lockManager.lock(MODEL_KEY));
    }

    @Test
    public void unlock() {
        lockManager.lock(MODEL_KEY);
        lockManager.unlock(MODEL_KEY);

        assertFalse(lockManager.isLocked(MODEL_KEY));
    }

    @Test
    public void isLock() {
        assertFalse(lockManager.isLocked(MODEL_KEY));

        lockManager.lock(MODEL_KEY);
        assertTrue(lockManager.isLocked(MODEL_KEY));

        lockManager.unlock(MODEL_KEY);
        assertFalse(lockManager.isLocked(MODEL_KEY));
    }

}