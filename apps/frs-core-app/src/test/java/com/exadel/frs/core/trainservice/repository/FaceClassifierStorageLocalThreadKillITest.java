package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.component.FaceClassifierProxy;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionExtendedClassifier;
import com.exadel.frs.core.trainservice.service.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.EnabledIf;


@SpringBootTest
@Slf4j
@EnabledIf(
    expression = "#{environment.acceptsProfiles('integration-test')}"
)
public class FaceClassifierStorageLocalThreadKillITest {

  private FaceClassifierStorageLocal storage;

  @Autowired
  private ApplicationContext applicationContext;

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
  public void unlock() throws InterruptedException {
    FaceClassifierProxy faceClassifier = storage.getFaceClassifier(APP_KEY, MODEL_ID);
    faceClassifier.setStorage(storage);
    faceClassifier.setClassifier(new LogisticRegressionExtendedClassifier());
    storage.lock(APP_KEY, MODEL_ID);
    faceClassifier.train(faceService.findAllFaceEmbeddings(),APP_KEY,MODEL_ID);
    storage.unlock(APP_KEY, MODEL_ID);
    //Thread.sleep(10000L);

  }

}