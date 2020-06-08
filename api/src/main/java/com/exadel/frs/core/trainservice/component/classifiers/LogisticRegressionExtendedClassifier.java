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
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt;
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt.Trainer;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

public class LogisticRegressionExtendedClassifier implements FaceClassifier {

    private static final long serialVersionUID = 1966949081344084764L;

    private static final double LAMBDA = 0;
    private static final double TOLERANCE = 0.005;
    private static final int MAX_ITER = 50;

    private final Trainer trainer = new Trainer(LAMBDA, TOLERANCE, MAX_ITER);
    private final Map<Integer, Pair<String, String>> labelMap;
    private LogisticRegressionExt logisticRegression;

    public LogisticRegressionExtendedClassifier(final Map<Integer, Pair<String, String>> labelMap) {
        this.labelMap = labelMap;
    }

    @Override
    public void train(final double[][] x, final int[] y) {
        this.logisticRegression = this.trainer.train(x, y);
    }

    @Override
    public Pair<Integer, String> predict(final double[] input) {
        if (isTrained()) {
            val predict = logisticRegression.predict(input);

            return Pair.of(predict, labelMap.get(predict).getRight());
        }

        throw new ModelNotTrainedException();
    }

    @Override
    public boolean isTrained() {
        return logisticRegression != null;
    }

    @Override
    public List<String> getUsedFaceIds() {
        if (labelMap == null) {
            return List.of();
        }

        return labelMap.values().stream()
                       .map(Pair::getLeft)
                       .collect(toList());
    }
}