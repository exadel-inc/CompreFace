package com.exadel.frs.core.trainservice.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FaceClassifierLockManagerTest {

    private FaceClassifierLockManager lockManager;

    private static final String APP_KEY = "app";
    private static final String MODEL_ID = "model_id";

    @BeforeEach
    public void beforeEach() {
        lockManager = new FaceClassifierLockManager();
        lockManager.postConstruct();
    }

    @Test
    public void lock() {
        lockManager.lock(APP_KEY, MODEL_ID);

        assertThrows(RuntimeException.class, () -> lockManager.lock(APP_KEY, MODEL_ID));
    }

    @Test
    public void unlock() {
        lockManager.lock(APP_KEY, MODEL_ID);
        lockManager.unlock(APP_KEY, MODEL_ID);

        assertFalse(lockManager.isLocked(APP_KEY, MODEL_ID));
    }

    @Test
    public void isLock() {
        assertFalse(lockManager.isLocked(APP_KEY, MODEL_ID));

        lockManager.lock(APP_KEY, MODEL_ID);
        assertTrue(lockManager.isLocked(APP_KEY, MODEL_ID));

        lockManager.unlock(APP_KEY, MODEL_ID);
        assertFalse(lockManager.isLocked(APP_KEY, MODEL_ID));
    }


}