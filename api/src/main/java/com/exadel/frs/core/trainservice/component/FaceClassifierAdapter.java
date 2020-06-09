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
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionExtendedClassifier;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.ArrayList;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    private FaceClassifier classifier;

    @Async
    public void train(
            final EmbeddingFaceList embeddingFaceList,
            final String modelKey
    ) {
        try {
            currentThread().setName(modelKey);

            var faceId = 0;
            val x = new ArrayList<double[]>();
            val y = new ArrayList<Integer>();
            val labelMap = new HashMap<Integer, Pair<String, String>>();

            val faceNameEmbeddings = embeddingFaceList.getFaceEmbeddings();
            if (faceNameEmbeddings.isEmpty()) {
                throw new ModelNotTrainedException();
            }

            for (val faceNameId : faceNameEmbeddings.keySet()) {
                labelMap.put(faceId, faceNameId);
                val lists = faceNameEmbeddings.get(faceNameId).stream()
                                              .filter(list -> isNotEmpty(list))
                                              .collect(toList());
                for (val list : lists) {
                    x.add(list.stream().mapToDouble(d -> d).toArray());
                    y.add(faceId);
                }
                faceId++;
            }

            classifier = new LogisticRegressionExtendedClassifier(labelMap);
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

    public Pair<Integer, String> predict(final double[] x) {
        return classifier.predict(x);
    }
}