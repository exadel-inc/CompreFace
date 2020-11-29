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

package com.exadel.frs.core.trainservice.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

class FaceClassifierPredictorTest {

    @Mock
    private Classifier classifier;

    @InjectMocks
    private FaceClassifierPredictor faceClassifierPredictor;

    private static final String MODEL_KEY = "modelKey";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void predict() {
        double[] input = new double[0];
        int resultCount = 1;
        val expected = List.of(Pair.of(1.0, ""));

        when(classifier.predict(input, MODEL_KEY, resultCount)).thenReturn(expected);

        val actual = faceClassifierPredictor.predict(MODEL_KEY, input, resultCount);

        assertThat(actual).isEqualTo(expected);

        verify(classifier).predict(input, MODEL_KEY, resultCount);
        verifyNoMoreInteractions(classifier);
    }

    @Test
    void verifyTest() {
        double[] input = new double[0];
        val imageId = "imageId";

        val expected = 1d;

        when(classifier.verify(input, MODEL_KEY, imageId)).thenReturn(expected);

        val actual = faceClassifierPredictor.verify(MODEL_KEY, input, imageId);

        assertThat(actual).isEqualTo(expected);

        Mockito.verify(classifier).verify(input, MODEL_KEY, imageId);
        verifyNoMoreInteractions(classifier);
    }
}