package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.service.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
@EnabledIf(expression = "#{environment.acceptsProfiles('integration-test')}")
public class FaceClassifierStorageLocalITest {

    private FaceClassifierStorageLocal storage;

    @MockBean
    private ApplicationContext applicationContext;

    @Autowired
    private ApplicationContext ac;

    @Autowired
    private FaceService faceService;

    private static final String APP_KEY = "app";
    private static final String MODEL_ID = "model_id";

    @BeforeEach
    void setUp() {
        storage = new FaceClassifierStorageLocal(applicationContext);
        storage.postConstruct();
    }

    @Test
    public void getFaceClassifier() {
        var faceClassifierProxy = new FaceClassifierProxy();
        when(applicationContext.getBean(FaceClassifierStorageLocal.FACE_CLASSIFIER_PROXY_NAME)).thenReturn(faceClassifierProxy);
        var faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_ID);

        assertEquals(faceClassifier, faceClassifierProxy);
    }

    @Test
    public void getFaceClassifierTwoTimes() {
        var faceClassifierProxy = new FaceClassifierProxy();
        when(applicationContext.getBean(FaceClassifierStorageLocal.FACE_CLASSIFIER_PROXY_NAME)).thenReturn(faceClassifierProxy);
        var faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_ID);
        var faceClassifier1 = storage.getFaceClassifier(APP_KEY, MODEL_ID);

        assertEquals(faceClassifier, faceClassifierProxy);
        assertEquals(faceClassifier1, faceClassifierProxy);
    }
}