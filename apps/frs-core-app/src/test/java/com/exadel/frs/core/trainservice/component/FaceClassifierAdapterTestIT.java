package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Slf4j
@EnabledIf(expression = "#{environment.acceptsProfiles('integration-test')}")
public class FaceClassifierAdapterTestIT {

    @Autowired
    private FaceClassifierAdapter faceClassifierAdapter;

    @Autowired
    private FaceDao service;

    public static final double THRESHOLD = 0.95;

    private static final String APP_KEY = "app";
    private static final String MODEL_ID = "model_id";

    @Test
    public void train() {
        var allFaceEmbeddings = service.findAllFaceEmbeddings();
        faceClassifierAdapter.trainSync(allFaceEmbeddings, APP_KEY, MODEL_ID);
        var count1 = 0;
        var count2 = 0;

        for (var faceName : allFaceEmbeddings.keySet()) {
            Pair<Integer, String> predict = faceClassifierAdapter
                    .predict(allFaceEmbeddings.get(faceName).get(0).stream().mapToDouble(d -> d).toArray());
            if (predict.getSecond().equals(faceName)) {
                count2++;
            }
            count1++;
        }
        double accuracy = count2 / count1;
        log.info("Logger accuracy {} ", accuracy);

        assertTrue(accuracy > THRESHOLD);
    }
}