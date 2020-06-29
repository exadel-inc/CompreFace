/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClassifierLockManagerTest {

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