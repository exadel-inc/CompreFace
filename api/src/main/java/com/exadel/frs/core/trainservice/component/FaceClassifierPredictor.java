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

import com.exadel.frs.core.trainservice.dao.ModelDao;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FaceClassifierPredictor {

    private final ModelDao modelDao;
    private final ApplicationContext context;

    public Pair<Integer, String> predict(final String modelKey, final double[] input) {
        val model = modelDao.getModel(modelKey);
        val faceClassifier = context.getBean(FaceClassifierAdapter.class);
        faceClassifier.setClassifier(model);

        return faceClassifier.predict(input);
    }
}