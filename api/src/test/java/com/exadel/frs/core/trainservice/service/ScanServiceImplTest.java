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
import static org.mockito.Mockito.mock;
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
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.PluginsVersions;
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
    private FacesApiClient facesApiClient;

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
    private static final FindFacesResult SCAN_RESULT = FindFacesResult.builder().embedding(new Double[]{EMBEDDING}).build();

    @Test
    void scanAndSaveFace() throws IOException {
        val findFacesResponse = FindFacesResponse.builder()
                                                 .result(List.of(SCAN_RESULT))
                                                 .pluginsVersions(PluginsVersions.builder().calculator("").build())
                                                 .build();
        val embeddings = new Embedding(List.of(EMBEDDING), "");
        val face = new Face();
        face.setEmbedding(embeddings);
        val expected = new FaceBO(face.getFaceName(), face.getId());
        val faceCollection = mock(FaceCollection.class);

        when(facesApiClient.findFacesWithCalculator(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null))
                .thenReturn(findFacesResponse);
        when(faceDao.addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY)).thenReturn(face);
        when(faceCacheProvider.getOrLoad(MODEL_KEY)).thenReturn(faceCollection);
        when(faceCollection.addFace(face)).thenReturn(expected);

        val actual = scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(facesApiClient).findFacesWithCalculator(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null);
        verify(faceDao).addNewFace(embeddings, mockFile, FACE_NAME, MODEL_KEY);
        verifyNoMoreInteractions(facesApiClient, faceDao);
    }

    @Test
    void tooManyFacesScan() {
        val findFacesResponse = FindFacesResponse.builder().result(List.of(SCAN_RESULT, SCAN_RESULT)).build();

        when(facesApiClient.findFacesWithCalculator(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null))
                .thenReturn(findFacesResponse);

        assertThatThrownBy(() ->
                scanService.scanAndSaveFace(mockFile, FACE_NAME, THRESHOLD, MODEL_KEY)
        ).isInstanceOf(TooManyFacesException.class);

        verify(facesApiClient).findFacesWithCalculator(mockFile, MAX_FACES_TO_RECOGNIZE, THRESHOLD, null);
        verifyNoInteractions(faceDao);
        verifyNoMoreInteractions(facesApiClient);
    }
}