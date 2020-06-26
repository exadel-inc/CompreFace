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

package com.exadel.frs.core.trainservice.component.classifiers;

import static java.lang.Math.round;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

public class LogisticRegressionExtendedClassifierTestIT {

    @Test
    void train() {
        val faceName = "faceName";
        val labelMap = Map.of(0, Pair.of("faceId", faceName), 1, Pair.of("faceId1", faceName));
        val classifier = new LogisticRegressionExtendedClassifier(labelMap);
        val xorMatrix = new double[][]{{0, 0}, {1, 0}};
        val xorResults = new int[]{0, 1};

        classifier.train(xorMatrix, xorResults);

        assertThat(classifier.isTrained()).isTrue();
    }

    @Test
    void trainModelWithWrongParamSizeFailed() {
        val faceName = "faceName";
        val labelMap = Map.of(0, Pair.of("faceId", faceName), 1, Pair.of("faceId1", faceName));
        val classifier = new LogisticRegressionExtendedClassifier(labelMap);
        val xorMatrix = new double[][]{{0, 0}, {1, 0}};
        val xorResults = new int[]{0};

        assertThatThrownBy(() -> {
            classifier.train(xorMatrix, xorResults);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void predict() {
        val faceName = "faceName";
        val predictResult = 1;
        val labelMap = Map.of(0, Pair.of("faceId", faceName), 1, Pair.of("faceId1", faceName));
        val classifier = new LogisticRegressionExtendedClassifier(labelMap);
        val xorMatrix = new double[][]{{0, 0}, {1, 0}};
        val xorResults = new int[]{0, 1};

        classifier.train(xorMatrix, xorResults);

        val actual = classifier.predict(new double[]{0, 0});

        assertThat(actual).isNotNull();
        assertThat(round(actual.getLeft())).isEqualTo(predictResult);
    }

    @Test
    void predictModelNotTrainedException() {
        assertThatThrownBy(() -> {
            new LogisticRegressionExtendedClassifier(null).predict(null);
        }).isInstanceOf(ModelNotTrainedException.class);
    }

    @Test
    void isTrained() {
        val faceName = "faceName";
        val labelMap = Map.of(0, Pair.of("faceId", faceName), 1, Pair.of("faceId1", faceName));
        val classifier = new LogisticRegressionExtendedClassifier(labelMap);

        assertThat(classifier.isTrained()).isFalse();

        val xorMatrix = new double[][]{{0, 0}, {1, 0}};
        val xorResults = new int[]{0, 1};

        classifier.train(xorMatrix, xorResults);

        assertThat(classifier.isTrained()).isTrue();
    }

    @Test
    void getUsedFaceIds() {
        val faceName = "faceName";
        val labelMap = Map.of(0, Pair.of("faceId", faceName), 1, Pair.of("faceId1", faceName));

        val facesIds = new LogisticRegressionExtendedClassifier(labelMap).getUsedFaceIds();

        assertThat(facesIds).size().isEqualTo(2);
        assertThat(facesIds).contains("faceId");
        assertThat(facesIds).contains("faceId1");
    }
}