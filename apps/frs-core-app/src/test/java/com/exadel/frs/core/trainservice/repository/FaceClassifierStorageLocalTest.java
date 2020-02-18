package com.exadel.frs.core.trainservice.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

public class FaceClassifierStorageLocalTest {

  private FaceClassifierStorageLocal storage;

  @Mock
  private ApplicationContext applicationContext;

  private static final String APP_KEY = "app";
  private static final String MODEL_ID = "model_id";

  @BeforeEach
  public void beforeEach(){
    storage = new FaceClassifierStorageLocal(applicationContext);
    storage.postConstruct();
  }

  @Test
  public void lock() {
    storage.lock(APP_KEY, MODEL_ID);
    Assertions.assertThrows(RuntimeException.class, () -> storage.lock(APP_KEY, MODEL_ID));
  }

  @Test
  public void unlock() {
    storage.lock(APP_KEY, MODEL_ID);
    storage.unlock(APP_KEY, MODEL_ID);
    Assertions.assertFalse(storage.isLocked(APP_KEY, MODEL_ID));
  }

  @Test
  public void isLock() {
    Assertions.assertFalse(storage.isLocked(APP_KEY, MODEL_ID));
    storage.lock(APP_KEY, MODEL_ID);
    Assertions.assertTrue(storage.isLocked(APP_KEY, MODEL_ID));
    storage.unlock(APP_KEY, MODEL_ID);
    Assertions.assertFalse(storage.isLocked(APP_KEY, MODEL_ID));
  }
}