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

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Slf4j
public class FaceClassifierAdapterTestIT {

    @Autowired
    private FaceClassifierAdapter faceClassifierAdapter;

    @Autowired
    private FaceDao service;

    public static final double THRESHOLD = 0.95;

    private static final String MODEL_KEY = "model_key";

    @SneakyThrows
    @Test
    @Transactional
    public void train() {
        val date = System.currentTimeMillis();
        val allFaceEmbeddings = service.findAllFaceEmbeddings();
        faceClassifierAdapter.trainSync(allFaceEmbeddings, MODEL_KEY);
        var allPredictions = 0;
        var truePredictions = 0;

        val embeddings = allFaceEmbeddings.getFaceEmbeddings();
        var score = 0;

        for (val key : embeddings.keySet()) {
            val faceName = key.getRight();
            val lists = embeddings.get(key).stream()
                                  .filter(ObjectUtils::isNotEmpty)
                                  .collect(toList());

            if (isNotEmpty(lists)) {
                continue;
            }

            for (val el : lists) {
                val predict = faceClassifierAdapter
                        .predict(el.stream().mapToDouble(d -> d).toArray());

                if (predict.getRight().equals(faceName)) {
                    truePredictions++;
                }
                allPredictions++;

                log.info("{} prob : {}", predict.getRight().equals(faceName) ? "TRUE" : "FALSE", predict.getLeft());

                if (predict.getRight().equals(faceName)) {
                    score += predict.getLeft();
                }
            }
        }
        val accuracy = (double) truePredictions / allPredictions;

        log.info("Accuracy {} ", accuracy);
        log.info("Score {} ", score);
        log.info("Finished in {} ms", (System.currentTimeMillis() - date));

        assertTrue(accuracy > THRESHOLD);
    }
}