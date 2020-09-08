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

import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.component.classifiers.Classifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.TrainedModelDao;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelHasNotEnoughFacesException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

public class ClassifierManagerTest {

    @Mock
    private TrainedModelDao trainedModelDao;

    @Mock
    private FaceDao faceDao;

    @Mock
    private FaceClassifierLockManager lockManager;

    @Mock
    private ApplicationContext context;

    @InjectMocks
    private FaceClassifierManager manager;

    private static final String MODEL_KEY = "modelKey";

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void saveClassifier() {
        val classifier = mock(Classifier.class);

        manager.saveClassifier(MODEL_KEY, classifier, "1.0");

        val inOrder = inOrder(trainedModelDao, lockManager);
        inOrder.verify(trainedModelDao).saveModel(MODEL_KEY, classifier, "1.0");
        inOrder.verify(lockManager).unlock(MODEL_KEY);
        verifyNoMoreInteractions(trainedModelDao, lockManager);
    }

    @Test
    void removeFaceClassifier() {
        manager.removeFaceClassifier(MODEL_KEY);

        val inOrder = inOrder(trainedModelDao, lockManager);
        inOrder.verify(lockManager).unlock(MODEL_KEY);
        inOrder.verify(trainedModelDao).deleteModel(MODEL_KEY);
        verifyNoMoreInteractions(trainedModelDao, lockManager);
    }

    @Test
    void initNewClassifierWithFaces() {
        val adapterMock = mock(FaceClassifierAdapter.class);
        List<String> faces = List.of();
        EmbeddingFaceList embeddingFaceList = new EmbeddingFaceList();

        when(context.getBean(FaceClassifierAdapter.class)).thenReturn(adapterMock);
        when(faceDao.findAllFacesIn(faces)).thenReturn(embeddingFaceList);

        manager.initNewClassifier(MODEL_KEY, faces);

        val inOrder = inOrder(lockManager, context, faceDao, adapterMock);
        inOrder.verify(lockManager).lock(MODEL_KEY);
        inOrder.verify(context).getBean(FaceClassifierAdapter.class);
        inOrder.verify(faceDao).findAllFacesIn(faces);
        inOrder.verify(adapterMock).train(embeddingFaceList, MODEL_KEY);

        verifyNoMoreInteractions(lockManager, context, faceDao, adapterMock);
        verifyNoInteractions(trainedModelDao);
    }

    @Test
    void initNewClassifier() {
        val adapterMock = mock(FaceClassifierAdapter.class);
        val faceList = mock(EmbeddingFaceList.class);

        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(nextInt());//error
        when(faceDao.findAllFaceEmbeddingsByApiKey(MODEL_KEY)).thenReturn(faceList);
        when(context.getBean(FaceClassifierAdapter.class)).thenReturn(adapterMock);

        manager.initNewClassifier(MODEL_KEY);

        val inOrder = inOrder(faceDao, lockManager, context, adapterMock);
        inOrder.verify(faceDao).countFacesInModel(MODEL_KEY);
        inOrder.verify(lockManager).lock(MODEL_KEY);
        inOrder.verify(context).getBean(FaceClassifierAdapter.class);
        inOrder.verify(faceDao).findAllFaceEmbeddingsByApiKey(MODEL_KEY);
        inOrder.verify(adapterMock).train(faceList, MODEL_KEY);

        verifyNoMoreInteractions(faceDao, lockManager, context, adapterMock);
        verifyNoInteractions(trainedModelDao);
    }

    @Test
    void initNewClassifierIfNotEnoughFaces() {
        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(1);

        assertThatThrownBy(() -> {
            manager.initNewClassifier(MODEL_KEY);
        }).isInstanceOf(ModelHasNotEnoughFacesException.class);
    }

    @Test
    void abortClassifierTraining() {
        manager.finishClassifierTraining(MODEL_KEY);

        verify(lockManager).unlock(MODEL_KEY);
        verifyNoMoreInteractions(lockManager);
    }

    @Test
    void isTraining() {
        when(lockManager.isLocked(MODEL_KEY)).thenReturn(true);

        assertThat(manager.isTraining(MODEL_KEY)).isTrue();

        verify(lockManager).isLocked(MODEL_KEY);
        verifyNoMoreInteractions(lockManager);
    }
}