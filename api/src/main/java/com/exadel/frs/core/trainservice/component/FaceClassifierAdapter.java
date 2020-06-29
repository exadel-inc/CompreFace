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

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionClassifier;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Setter
@Slf4j
@Scope(value = "prototype")
@RequiredArgsConstructor
public class FaceClassifierAdapter {

    private final FaceClassifierManager manager;
    private Classifier classifier;

    @Async
    public void train(
            final EmbeddingFaceList embeddingFaceList,
            final String modelKey
    ) {
        try {
            currentThread().setName(modelKey);

            val x = new ArrayList<double[]>();
            val y = new ArrayList<Integer>();
            val labelMap = new ArrayList<Pair<String, String>>();

            val faceNameEmbeddingsMap = embeddingFaceList.getFaceEmbeddings();
            if (faceNameEmbeddingsMap.isEmpty()) {
                throw new ModelNotTrainedException();
            }

            for (val faceNameId : faceNameEmbeddingsMap.keySet()) {
                val lists = faceNameEmbeddingsMap.get(faceNameId).stream()
                                              .filter(ObjectUtils::isNotEmpty)
                                              .collect(toList());

                if (isNotEmpty(lists)) {
                    labelMap.add(faceNameId);

                    lists.forEach(embeddings -> {
                        x.add(embeddings.stream().mapToDouble(d -> d).toArray());
                        y.add(labelMap.indexOf(faceNameId));
                    });
                }
            }

            classifier = new LogisticRegressionClassifier(labelMap);
            classifier.train(
                    x.toArray(double[][]::new),
                    y.stream().mapToInt(integer -> integer).toArray()
            );

            manager.saveClassifier(modelKey, classifier, embeddingFaceList.getCalculatorVersion());
        } catch (ModelNotTrainedException e) {
            log.error("Model {} hasn't enough data to train", modelKey);
        } finally {
            manager.finishClassifierTraining(modelKey);
        }
    }

    public void trainSync(
            final EmbeddingFaceList embeddingFaceList,
            final String modelKey
    ) {
        this.train(embeddingFaceList, modelKey);
    }

    public Pair<Double, String> predict(final double[] x) {
        return classifier.predict(x);
    }
}