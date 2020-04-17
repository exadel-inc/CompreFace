package com.exadel.frs.core.trainservice.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@Slf4j
public class FaceClassifierStorageLocalTestIT {

    private FaceClassifierStorageLocal storage;

    @MockBean
    private ApplicationContext applicationContext;

    private static final String APP_KEY = "app_key";
    private static final String MODEL_KEY = "model_key";

    @BeforeEach
    void setUp() {
        storage = new FaceClassifierStorageLocal(applicationContext);
    }

    @Test
    public void getFaceClassifier() {
        val classifier = new FaceClassifierAdapter(
                mock(ApplicationContext.class),
                mock(FaceClassifierStorage.class)
        );

        when(applicationContext.getBean(FaceClassifierStorageLocal.FACE_CLASSIFIER_ADAPTER_NAME)).thenReturn(classifier);
        val faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_KEY);

        assertEquals(faceClassifier, classifier);
    }

    @Test
    public void getFaceClassifierTwoTimes() {
        val classifier = new FaceClassifierAdapter(
                mock(ApplicationContext.class),
                mock(FaceClassifierStorage.class)
        );

        when(applicationContext.getBean(FaceClassifierStorageLocal.FACE_CLASSIFIER_ADAPTER_NAME)).thenReturn(classifier);
        val faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_KEY);
        val faceClassifier1 = storage.getFaceClassifier(APP_KEY, MODEL_KEY);

        assertEquals(faceClassifier, classifier);
        assertEquals(faceClassifier1, classifier);
    }
}