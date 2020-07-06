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

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.List;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.tuple.Pair;
import smile.classification.LogisticRegression;
import smile.util.IntSet;

public class LogisticRegressionClassifier implements Classifier {

    private static final double LAMBDA = 0.0001;
    private static final double TOLERANCE = 0.0001;
    private static final int MAX_ITER = 100;

    private final List<Pair<String, String>> faces;
    private LogisticRegression logisticRegression;

    public LogisticRegressionClassifier(final List<Pair<String, String>> faces) {
        this.faces = faces;
    }

    @Override
    public void train(final double[][] x, final int[] y) {
        this.logisticRegression = LogisticRegression.fit(x, y, LAMBDA, TOLERANCE, MAX_ITER);
    }

    @Override
    public List<Pair<Double, String>> predict(final double[] input, int resultCount) {
        if (isTrained()) {
            val probs = new double[faces.size()];
            logisticRegression.predict(input, probs);

            return getPredictionList(probs, resultCount);
        }

        throw new ModelNotTrainedException();
    }

    @SneakyThrows
    private List<Pair<Double, String>> getPredictionList(final double[] probs, final int resultCount) {
        val labelsField = FieldUtils.getField(logisticRegression.getClass(), "labels", true);
        val labels = (IntSet) labelsField.get(logisticRegression);

        return IntStream.of(labels.values)
                        .boxed()
                        .map(i -> Pair.of(probs[i], faces.get(i).getRight()))
                        .sorted((o1, o2) -> o2.getLeft().compareTo(o1.getLeft()))
                        .limit(resultCount)
                        .collect(toList());
    }

    @Override
    public boolean isTrained() {
        return logisticRegression != null;
    }

    @Override
    public List<String> getUsedFaceIds() {
        if (faces == null) {
            return List.of();
        }

        return faces.stream()
                    .map(Pair::getLeft)
                    .collect(toList());
    }
}