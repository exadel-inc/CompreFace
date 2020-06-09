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

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.exception.ModelHasNoFacesException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceClassifierManager {

    private final ModelDao modelDao;
    private final FaceDao faceDao;
    private final FaceClassifierLockManager lockManager;
    private final ApplicationContext context;

    public void saveClassifier(String modelKey, FaceClassifier classifier, String calculatorVersion) {
        try {
            modelDao.saveModel(modelKey, classifier, calculatorVersion);
        } finally {
            lockManager.unlock(modelKey);
        }
    }

    public void removeFaceClassifier(final String modelKey) {
        lockManager.unlock(modelKey);
        modelDao.deleteModel(modelKey);
    }

    public void initNewClassifier(final String modelKey, final List<String> faces) {
        lockManager.lock(modelKey);
        val faceClassifier = context.getBean(FaceClassifierAdapter.class);
        faceClassifier.train(faceDao.findAllFacesIn(faces), modelKey);
    }

    public void initNewClassifier(final String modelKey) {
        if (faceDao.countFacesInModel(modelKey) < 1) {
            throw new ModelHasNoFacesException();
        }

        lockManager.lock(modelKey);
        val faceClassifier = context.getBean(FaceClassifierAdapter.class);
        faceClassifier.train(faceDao.findAllFaceEmbeddingsByApiKey(modelKey), modelKey);
    }

    public void finishClassifierTraining(final String modelKey) {
        lockManager.unlock(modelKey);
    }

    public boolean isTraining(final String modelKey) {
        return lockManager.isLocked(modelKey);
    }
}