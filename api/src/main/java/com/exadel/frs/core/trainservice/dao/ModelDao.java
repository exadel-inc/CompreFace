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

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.entity.mongo.Model;
import com.exadel.frs.core.trainservice.exception.ModelNotFoundException;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import com.exadel.frs.core.trainservice.repository.mongo.ModelRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bson.types.ObjectId;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModelDao {

    private final ModelRepository modelRepository;

    public List<Model> findAllWithoutClassifier() {
        return modelRepository.findAllWithoutClassifier();
    }

    public Model saveModel(
            final String modelKey,
            final FaceClassifier classifier,
            final String calculatorVersion
    ) {
        val model = Model.builder()
                         .id(UUID.randomUUID().toString())
                         .modelKey(modelKey)
                         .classifier(classifier)
                         .faces(classifier.getUsedFaceIds().stream()
                                          .map(ObjectId::new)
                                          .collect(toList()))
                         .calculatorVersion(calculatorVersion)
                         .build();

        return modelRepository.save(model);
    }

    public Model updateModelApiKey(
            final String oldModelKey,
            final String newModelKey
    ) {
        val model = modelRepository.findFirstByModelKey(oldModelKey).orElseThrow(ModelNotFoundException::new);
        model.setModelKey(newModelKey);

        return modelRepository.save(model);
    }

    public FaceClassifier getModel(final String modelKey) {
        return modelRepository.findById(modelKey)
                              .orElseThrow(ModelNotTrainedException::new)
                              .getClassifier();
    }

    public void deleteModel(final String modelKey) {
        try {
            modelRepository.deleteByModelKey(modelKey);
        } catch (EmptyResultDataAccessException e) {
            log.info("Model with id : {} not found", modelKey);
        }
    }
}