package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.component.FaceClassifierProxy;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionExtendedClassifier;
import com.exadel.frs.core.trainservice.config.MongoTest;
import com.exadel.frs.core.trainservice.domain.Model;
import com.exadel.frs.core.trainservice.exception.ClassifierNotTrained;
import com.exadel.frs.core.trainservice.repository.ModelRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.util.HashMap;
import java.util.UUID;

@MongoTest
@Slf4j
@EnabledIf(expression = "#{environment.acceptsProfiles('integration-test')}")
public class ModelServiceTest {

    @Autowired
    private ModelRepository modelRepository;


    @BeforeEach
    public void before(){
        modelRepository.deleteAll();
    }

    @Test
    public void classifierSave() {
        Assertions.assertDoesNotThrow(this::saveTrainedModel);
    }

    @Test
    public void classifierGet() {
        String id = saveTrainedModel();
        Assertions.assertTrue(modelRepository.findById(id).isPresent());
    }

    @Test
    public void classifierGetNotFound() {
        String id = saveTrainedModel();
        Assertions.assertFalse(modelRepository.findById(id + "1").isPresent());
    }

    @Test
    public void delete() {
        String id = saveTrainedModel();
        Assertions.assertEquals(1L, modelRepository.count());
        Assertions.assertDoesNotThrow(() -> modelRepository.deleteById(id));
        Assertions.assertEquals(0L, modelRepository.count());
    }

    @Test
    public void deleteWrong() {
        String id = saveTrainedModel();
        Assertions.assertEquals(1L, modelRepository.count());
        Assertions.assertDoesNotThrow(() -> modelRepository.deleteById(id + "1"));
        Assertions.assertEquals(1L, modelRepository.count());
    }


    private String saveTrainedModel() {
        double[][] x = new double[2][2];
        x[0][0] = 2;
        x[0][1] = 2;
        x[1][0] = 3;
        x[1][1] = 2;
        int y[] = new int[2];
        y[0] = 1;
        y[1] = 2;

        HashMap<Integer, String> labelMap = new HashMap<>();
        labelMap.put(1, "firstLabel");
        labelMap.put(2, "secondLabel");
        LogisticRegressionExtendedClassifier classifier = new LogisticRegressionExtendedClassifier();
        classifier.train(x, y, labelMap);
        Model model = Model.builder()
                .classifier(classifier)
                .apiKey(UUID.randomUUID().toString())
                .id(UUID.randomUUID().toString())
                .classifierName(FaceClassifierProxy.CLASSIFIER_IMPLEMENTATION_BEAN_NAME)
                .build();
        Model save = modelRepository.save(model);
        return save.getId();
    }

}
