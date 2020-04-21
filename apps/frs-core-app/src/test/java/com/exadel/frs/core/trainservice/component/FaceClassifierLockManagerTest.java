package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.FaceClassifierLockManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.component.FaceClassifierProxy;
import com.exadel.frs.core.trainservice.service.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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