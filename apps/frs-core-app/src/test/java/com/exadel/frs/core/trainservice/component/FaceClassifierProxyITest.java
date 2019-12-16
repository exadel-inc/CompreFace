package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.service.FaceService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
@EnabledIf(
    expression = "#{environment.acceptsProfiles('integration-test')}"
)
public class FaceClassifierProxyITest {

  @Autowired
  private FaceClassifierProxy faceClassifierProxy;

  @Autowired
  private FaceService service;

  private FaceClassifier faceClassifier;

  public static final double THRESHOLD = 0.95;

  private static final String APP_KEY = "app";
  private static final String MODEL_ID = "model_id";


  @BeforeEach
  public void beforeEach() {
    faceClassifier = faceClassifierProxy.getClassifier();
  }

  @Test
  public void train() {
    Map<String, List<List<Double>>> allFaceEmbeddings = service.findAllFaceEmbeddings();
    faceClassifierProxy.trainSync(allFaceEmbeddings, APP_KEY, MODEL_ID);
    double count1 = 0;
    double count2 = 0;
    for (String faceName : allFaceEmbeddings.keySet()) {
      Pair<Integer, String> predict = faceClassifierProxy
          .predict(allFaceEmbeddings.get(faceName).get(0).stream().mapToDouble(d -> d).toArray());
      if (predict.getSecond().equals(faceName)) {
        count2++;
      }
      count1++;
    }
    double accuracy = count2 / count1;
    log.info("Logger accuracy {} ", accuracy);
    Assertions.assertTrue(accuracy > THRESHOLD);
  }

}