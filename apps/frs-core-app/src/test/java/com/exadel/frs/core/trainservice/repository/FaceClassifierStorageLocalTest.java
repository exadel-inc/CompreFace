package com.exadel.frs.core.trainservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.exception.ModelAlreadyLockedException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

public class FaceClassifierStorageLocalTest {

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private FaceClassifierStorageLocal storage;

    private static final String APP_KEY = "app_key";
    private static final String MODEL_KEY = "model_key";

    @BeforeEach
    public void beforeEach() {
        initMocks(this);
    }

    @Test
    public void lock() {
        storage.lock(APP_KEY, MODEL_KEY);

        assertThrows(ModelAlreadyLockedException.class, () -> storage.lock(APP_KEY, MODEL_KEY));
    }

    @Test
    public void unlock() {
        storage.lock(APP_KEY, MODEL_KEY);
        storage.unlock(APP_KEY, MODEL_KEY);

        val actual = storage.isLocked(APP_KEY, MODEL_KEY);

        assertThat(actual).isFalse();
    }

    @Test
    public void isLock() {
        assertThat(storage.isLocked(APP_KEY, MODEL_KEY)).isFalse();

        storage.lock(APP_KEY, MODEL_KEY);
        assertThat(storage.isLocked(APP_KEY, MODEL_KEY)).isTrue();

        storage.unlock(APP_KEY, MODEL_KEY);
        assertThat(storage.isLocked(APP_KEY, MODEL_KEY)).isFalse();
    }

    @Test
    public void getFaceClassifier() {
        val classifier = new FaceClassifierAdapter(
                mock(ApplicationContext.class),
                mock(FaceClassifierStorage.class)
        );

        when(applicationContext.getBean(anyString())).thenReturn(classifier);

        val actual = storage.getFaceClassifier(APP_KEY, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(classifier);
    }

    @Test
    public void removeFaceClassifier() {
        storage.lock(APP_KEY, MODEL_KEY);
        assertThat(storage.isLocked(APP_KEY, MODEL_KEY)).isTrue();

        storage.removeFaceClassifier(APP_KEY, MODEL_KEY);
        assertThat(storage.isLocked(APP_KEY, MODEL_KEY)).isFalse();
    }
}