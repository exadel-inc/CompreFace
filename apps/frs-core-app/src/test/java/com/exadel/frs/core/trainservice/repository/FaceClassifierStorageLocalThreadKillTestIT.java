package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@Slf4j
public class FaceClassifierStorageLocalThreadKillTestIT {

    private FaceClassifierStorageLocal storage;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FaceDao faceDao;

    private static final String APP_KEY = "app_key";
    private static final String MODEL_KEY = "model_key";

    @BeforeEach
    void setUp() {
        storage = new FaceClassifierStorageLocal(applicationContext);
    }

    @Test
    public void unlock() {
        val faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_KEY);
        storage.lock(APP_KEY, MODEL_KEY);
        faceClassifier.train(faceDao.findAllFaceEmbeddings(), APP_KEY, MODEL_KEY);
        storage.unlock(APP_KEY, MODEL_KEY);
    }
}