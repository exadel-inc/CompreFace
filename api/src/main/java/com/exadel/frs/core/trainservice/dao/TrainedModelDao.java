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

package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import com.exadel.frs.core.trainservice.entity.TrainedModel;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import com.exadel.frs.core.trainservice.repository.TrainedModelRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrainedModelDao {

    private final TrainedModelRepository trainedModelRepository;

    public List<TrainedModel> findAllWithoutClassifier() {
        return trainedModelRepository.findByClassifierIsNull();
    }

    public TrainedModel saveModel(
            final String modelKey,
            final Classifier classifier,
            final String calculatorVersion
    ) {
        val oldModel = trainedModelRepository.findFirstByModelKey(modelKey);
        val model = TrainedModel.builder()
                                .id(oldModel.isPresent()
                                        ? oldModel.get().getId()
                                        : null
                                )
                                .modelKey(modelKey)
                                .classifier(classifier)
                                .calculatorVersion(calculatorVersion)
                                .build();

        return trainedModelRepository.save(model);
    }

    public Classifier getModel(final String modelKey) {
        return trainedModelRepository.findFirstByModelKey(modelKey)
                                     .orElseThrow(ModelNotTrainedException::new)
                                     .getClassifier();
    }

    public void deleteModel(final String modelKey) {
        try {
            trainedModelRepository.deleteByModelKey(modelKey);
        } catch (EmptyResultDataAccessException e) {
            log.info("Model with id : {} not found", modelKey);
        }
    }
}