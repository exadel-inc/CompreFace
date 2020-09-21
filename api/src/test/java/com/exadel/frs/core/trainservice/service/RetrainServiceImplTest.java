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

package com.exadel.frs.core.trainservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RetrainServiceImplTest {

    @Mock
    private FaceClassifierManager manager;

    @InjectMocks
    private RetrainServiceImpl retrainService;

    private static final String MODEL_KEY = "model_key";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void startRetrain() {
        retrainService.startRetrain(MODEL_KEY);

        verify(manager).initNewClassifier(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }

    @Test
    void isTrainingRun() {
        when(manager.isTraining(MODEL_KEY)).thenReturn(true);

        val actual = retrainService.isTrainingRun(MODEL_KEY);

        assertThat(actual).isTrue();

        verify(manager).isTraining(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }

    @Test
    void abortTraining() {
        retrainService.abortTraining(MODEL_KEY);

        verify(manager).finishClassifierTraining(MODEL_KEY);
        verifyNoMoreInteractions(manager);
    }
}