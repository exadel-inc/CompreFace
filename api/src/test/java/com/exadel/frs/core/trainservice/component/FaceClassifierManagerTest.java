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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.verify;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelHasNoFacesException;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

class FaceClassifierManagerTest {

    @Mock
    private ModelDao modelDao;

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
        FaceClassifier classifier = mock(FaceClassifier.class);
        assertDoesNotThrow(() -> manager.saveClassifier(MODEL_KEY, classifier, "1.0"));

        val inOrder = inOrder(modelDao, lockManager);
        inOrder.verify(modelDao).saveModel(MODEL_KEY, classifier, "1.0");
        inOrder.verify(lockManager).unlock(MODEL_KEY);
        verifyNoMoreInteractions(modelDao, lockManager);
    }

    @Test
    void removeFaceClassifier() {
        assertDoesNotThrow(() -> manager.removeFaceClassifier(MODEL_KEY));

        val inOrder = inOrder(modelDao, lockManager);
        inOrder.verify(lockManager).unlock(MODEL_KEY);
        inOrder.verify(modelDao).deleteModel(MODEL_KEY);
        verifyNoMoreInteractions(modelDao, lockManager);
    }

    @Test
    void initNewClassifier() {
        val adapterMock = mock(FaceClassifierAdapter.class);
        val faceList = mock(EmbeddingFaceList.class);

        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(nextInt());
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
        verifyNoInteractions(modelDao);
    }

    @Test
    void initNewClassifierIfNoFaces() {
        when(faceDao.countFacesInModel(MODEL_KEY)).thenReturn(0);

        assertThrows(ModelHasNoFacesException.class, () -> manager.initNewClassifier(MODEL_KEY));
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
        val actual = manager.isTraining(MODEL_KEY);
        assertThat(actual).isTrue();

        verify(lockManager).isLocked(MODEL_KEY);
        verifyNoMoreInteractions(lockManager);
    }
}