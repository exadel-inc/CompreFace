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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import com.exadel.frs.core.trainservice.entity.mongo.Model;
import com.exadel.frs.core.trainservice.repository.mongo.ModelRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ModelDaoTest {

    @Mock
    private ModelRepository modelRepository;

    @InjectMocks
    private ModelDao modelDao;

    public static final String MODEL_KEY = "model_key";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void findAllWithoutClassifier() {
        //todo
    }

    @Test
    void saveModel() {
        val classifier = mock(Classifier.class);
        val calculator = "1.0";

        when(modelRepository.findFirstByModelKey(MODEL_KEY)).thenReturn(Optional.empty());
        when(modelRepository.save(any())).then(returnsFirstArg());

        val actual = modelDao.saveModel(MODEL_KEY, classifier, calculator);

        assertThat(actual).isNotNull();
        assertThat(actual.getModelKey()).isEqualTo(MODEL_KEY);
        assertThat(actual.getClassifier()).isEqualTo(classifier);
        assertThat(actual.getCalculatorVersion()).isEqualTo(calculator);

        verify(modelRepository).findFirstByModelKey(MODEL_KEY);
        verify(modelRepository).save(actual);
        verifyNoMoreInteractions(modelRepository);
    }

    @Test
    void updateOldModel() {
        val classifier = mock(Classifier.class);
        val calculator = "1.0";
        val id = UUID.randomUUID().toString();
        val model = Model.builder()
                         .id(id)
                         .build();

        when(modelRepository.findFirstByModelKey(MODEL_KEY)).thenReturn(Optional.of(model));
        when(modelRepository.save(any())).then(returnsFirstArg());

        val actual = modelDao.saveModel(MODEL_KEY, classifier, calculator);

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getModelKey()).isEqualTo(MODEL_KEY);
        assertThat(actual.getClassifier()).isEqualTo(classifier);
        assertThat(actual.getCalculatorVersion()).isEqualTo(calculator);

        verify(modelRepository).findFirstByModelKey(MODEL_KEY);
        verify(modelRepository).save(actual);
        verifyNoMoreInteractions(modelRepository);
    }

    @Test
    void updateModelApiKey() {
        //todo
    }

    @Test
    void getModel() {
        //todo
    }

    @Test
    void deleteModel() {
        //todo
    }
}