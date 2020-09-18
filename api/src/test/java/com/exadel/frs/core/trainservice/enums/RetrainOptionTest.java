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

package com.exadel.frs.core.trainservice.enums;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.FORCE;
import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static com.exadel.frs.core.trainservice.enums.RetrainOption.YES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import com.exadel.frs.core.trainservice.exception.ClassifierIsAlreadyTrainingException;
import com.exadel.frs.core.trainservice.service.RetrainService;
import lombok.val;
import org.junit.jupiter.api.Test;

class RetrainOptionTest {

    private static final String MODEL_KEY = "model_key";

    @Test
    void runNO() {
        val retrainService = mock(RetrainService.class);

        NO.run(MODEL_KEY, retrainService);

        verifyNoInteractions(retrainService);
    }

    @Test
    void runYES() {
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(false);

        YES.run(MODEL_KEY, retrainService);

        verify(retrainService).isTrainingRun(MODEL_KEY);
        verify(retrainService).startRetrain(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void runYESException() {
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(true);

        assertThatThrownBy(() ->
                YES.run(MODEL_KEY, retrainService)
        ).isInstanceOf(ClassifierIsAlreadyTrainingException.class);

        verify(retrainService).isTrainingRun(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void runFORCE() {
        val retrainService = mock(RetrainService.class);
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(true);

        FORCE.run(MODEL_KEY, retrainService);

        verify(retrainService).abortTraining(MODEL_KEY);
        verify(retrainService).startRetrain(MODEL_KEY);
        verifyNoMoreInteractions(retrainService);
    }

    @Test
    void getTrainingOptionNO() {
        val actual = RetrainOption.getTrainingOption("no");

        assertThat(actual).isEqualTo(NO);
    }

    @Test
    void getTrainingOptionYES() {
        val actual = RetrainOption.getTrainingOption("yes");

        assertThat(actual).isEqualTo(YES);
    }

    @Test
    void getTrainingOptionFORCE() {
        val actual = RetrainOption.getTrainingOption("force");

        assertThat(actual).isEqualTo(FORCE);
    }

    @Test
    void getTrainingOptionDefault() {
        val actual = RetrainOption.getTrainingOption(null);

        assertThat(actual).isEqualTo(FORCE);
    }
}