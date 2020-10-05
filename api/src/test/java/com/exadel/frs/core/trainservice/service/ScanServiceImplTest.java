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

import static com.exadel.frs.core.trainservice.service.ScanServiceImpl.MAX_FACES_TO_RECOGNIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Face.Embedding;
import com.exadel.frs.core.trainservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResult;
import java.io.IOException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

class ScanServiceImplTest {

    @Mock
    private FacesClient scanFacesClient;

    @Mock
    private FaceDao faceDao;

    @Mock
    private MockMultipartFile mockFile;

    @Mock
    private FaceCacheProvider faceCacheProvider;

    @InjectMocks
    private ScanServiceImpl scanService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    private static final String FACE_NAME = "faceName";
    private static final String MODEL_KEY = "modelKey";
    private static final double THRESHOLD = 1.0;
    private static final double EMBEDDING = 100500;
    private static final ScanResult SCAN_RESULT = new ScanResult().setEmbedding(List.of(EMBEDDING));

    @Test
    void scanAndSaveFace() throws IOException {
        val scanResponse = new ScanResponse().setResult(List.of(SCAN_RESULT));
        val embeddings = new Embedding(List.of(EMBEDDING), null);
        val face = new Face();
        face.setEmbedding(embeddings);

        when(scanFacesClient.scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD))
                .thenReturn(scanResponse);

        when(faceDao.addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY)).thenReturn(face);

        when(faceCacheProvider.getOrLoad(MODEL_KEY)).thenReturn(FaceCollection.buildFromFaces(List.of(face)));

        val actual = scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(new FaceBO(face.getFaceName(), face.getId()));

        verify(scanFacesClient).scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD);
        verify(faceDao).addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY);
        verifyNoMoreInteractions(scanFacesClient, faceDao);
    }

    @Test
    void tooManyFacesScan() {
        val scanResponse = new ScanResponse().setResult(List.of(SCAN_RESULT, SCAN_RESULT));

        when(scanFacesClient.scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD))
                .thenReturn(scanResponse);

        assertThatThrownBy(() ->
                scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY)
        ).isInstanceOf(TooManyFacesException.class);

        verify(scanFacesClient).scanFaces(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD);
        verifyNoInteractions(faceDao);
        verifyNoMoreInteractions(scanFacesClient);
    }
}