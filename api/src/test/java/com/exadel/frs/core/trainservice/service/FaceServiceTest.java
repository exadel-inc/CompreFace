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

package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class FaceServiceTest {

    @Mock
    private FaceDao faceDao;

    @Mock
    private FaceCacheProvider faceCacheProvider;

    @InjectMocks
    private FaceService faceService;

    private static final String MODEL_KEY = "model_key";
    private static final String API_KEY = MODEL_KEY;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void findAllFaceNames() {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );
        val faceCollection = FaceCollection.buildFromFaces(faces);

        when(faceCacheProvider.getOrLoad(API_KEY))
                .thenReturn(faceCollection);

        val actual = faceService.findFaces(API_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual.size()).isEqualTo(faces.size());

        verify(faceCacheProvider).getOrLoad(API_KEY);
        verifyNoMoreInteractions(faceDao);
    }

    @Test
    void deleteFaceByName() {
        val faceName = "face_name";

        faceService.deleteFaceByName(faceName, API_KEY);

        verify(faceDao).deleteFaceByName(faceName, API_KEY);
    }

    @Test
    void deleteFaceById() {
        val faceId = randomUUID().toString();

        faceService.deleteFaceById(faceId, API_KEY);

        verify(faceDao).deleteFaceById(faceId);
    }

    @Test
    void deleteFacesByModel() {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );
        val faceCollection = FaceCollection.buildFromFaces(faces);

        when(faceCacheProvider.getOrLoad(API_KEY))
                .thenReturn(faceCollection);

        faceService.deleteFacesByModel(API_KEY);

        verify(faceDao).deleteFacesByApiKey(API_KEY);
        verifyNoMoreInteractions(faceDao);
    }

    @Test
    void countFacesInModel() {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );
        val faceCollection = FaceCollection.buildFromFaces(faces);

        when(faceCacheProvider.getOrLoad(API_KEY))
                .thenReturn(faceCollection);

        val actual = faceService.countFacesInModel(MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(faces.size());

        verify(faceCacheProvider).getOrLoad(API_KEY);
        verifyNoMoreInteractions(faceCacheProvider);
    }
}