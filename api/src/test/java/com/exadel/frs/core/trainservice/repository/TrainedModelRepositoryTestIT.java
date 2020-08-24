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

package com.exadel.frs.core.trainservice.repository;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static smile.math.MathEx.randomLong;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionClassifier;
import com.exadel.frs.core.trainservice.entity.TrainedModel;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@DataJpaTest
@ExtendWith(SpringExtension.class)
public class TrainedModelRepositoryTestIT {

    @Autowired
    private TrainedModelRepository trainedModelRepository;

    @AfterEach
    public void after() {
        trainedModelRepository.deleteAll();
    }

    @Test
    public void classifierSave() {
        assertDoesNotThrow(this::saveTrainedModel);
    }

    @Test
    public void classifierGet() {
        val id = saveTrainedModel();

        assertTrue(trainedModelRepository.findById(id).isPresent());
    }

    @Test
    public void classifierGetNotFound() {
        val id = saveTrainedModel();

        assertFalse(trainedModelRepository.findById(id + 1).isPresent());
    }

    @Test
    public void delete() {
        var id = saveTrainedModel();

        assertEquals(1L, trainedModelRepository.count());
        assertDoesNotThrow(() -> trainedModelRepository.deleteById(id));
        assertEquals(0L, trainedModelRepository.count());
    }

    @Test
    public void deleteWrong() {
        var id = saveTrainedModel();

        assertEquals(1L, trainedModelRepository.count());
        assertDoesNotThrow(() -> trainedModelRepository.deleteById(id + 1));
        assertEquals(1L, trainedModelRepository.count());
    }

    private Long saveTrainedModel() {
        val x = new double[2][2];
        x[0][0] = 2;
        x[0][1] = 2;
        x[1][0] = 3;
        x[1][1] = 2;
        val y = new int[2];
        y[0] = 1;
        y[1] = 2;

        val faces = List.of(
                Pair.of(randomUUID().toString(), "firstLabel"),
                Pair.of(randomUUID().toString(), "secondLabel")
        );

        val classifier = new LogisticRegressionClassifier(faces);
        classifier.train(x, y);

        val model = TrainedModel.builder()
                                .classifier(classifier)
                                .id(randomLong())
                                .build();

        return trainedModelRepository.save(model).getId();
    }
}