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

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionClassifier;
import com.exadel.frs.core.trainservice.entity.TrainedModel;
import java.util.List;
import lombok.val;
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
    public void classifierGet() {
        val id = saveTrainedModel();

        assertThat(trainedModelRepository.findById(id)).isPresent();
    }

    @Test
    public void classifierGetNotFound() {
        val id = saveTrainedModel();

        assertThat(trainedModelRepository.findById(id + 1)).isNotPresent();
    }

    @Test
    public void delete() {
        var id = saveTrainedModel();

        assertThat(trainedModelRepository.count()).isEqualTo(1L);

        trainedModelRepository.deleteById(id);

        assertThat(trainedModelRepository.count()).isEqualTo(0L);
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

        val faces = List.of("firstLabel", "secondLabel");

        val classifier = new LogisticRegressionClassifier(faces);
        classifier.train(x, y);

        val model = TrainedModel.builder()
                                .classifier(classifier)
                                .id(nextLong())
                                .build();

        return trainedModelRepository.save(model).getId();
    }
}