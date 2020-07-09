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

package com.exadel.frs.core.trainservice.dao;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.mock.web.MockMultipartFile;

class FaceDaoTest {

    @Mock
    private FacesRepository facesRepository;

    @Mock
    private GridFsOperations gridFsOperations;

    @InjectMocks
    private FaceDao faceDao;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void deleteFaceByName() {
        val faceName = "faceName";
        val apiKey = randomAlphabetic(10);
        val faces = List.of(new Face());
        when(facesRepository.deleteByApiKeyAndFaceName(apiKey, faceName)).thenReturn(faces);

        val actual = faceDao.deleteFaceByName(faceName, apiKey);

        assertThat(actual).isEqualTo(faces);

        verify(facesRepository).deleteByApiKeyAndFaceName(apiKey, faceName);
        verify(gridFsOperations, times(2)).delete(any());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteFaceById() {
        val faceId = "faceId";
        val face = Optional.of(new Face());
        when(facesRepository.findById(faceId)).thenReturn(face);

        val actual = faceDao.deleteFaceById(faceId);

        assertThat(actual).isEqualTo(face.get());

        verify(facesRepository).findById(faceId);
        verify(facesRepository).delete(face.get());
        verify(gridFsOperations, times(2)).delete(any());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteNonexistentFaceById() {
        val faceId = "faceId";
        when(facesRepository.findById(faceId)).thenReturn(Optional.empty());

        val actual = faceDao.deleteFaceById(faceId);

        assertThat(actual).isNull();

        verify(facesRepository).findById(faceId);
        verifyNoMoreInteractions(facesRepository);
        verifyNoInteractions(gridFsOperations);
    }

    @Test
    void deleteFacesByApiKey() {
        val apiKey = randomAlphabetic(10);
        val faces = List.of(new Face());
        when(facesRepository.deleteFacesByApiKey(apiKey)).thenReturn(faces);

        val actual = faceDao.deleteFacesByApiKey(apiKey);

        assertThat(actual).isEqualTo(faces);

        verify(facesRepository).deleteFacesByApiKey(apiKey);
        verify(gridFsOperations, times(2)).delete(any());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void countFacesInModel() {
        val apiKey = randomAlphabetic(10);
        val facesCount = nextInt();

        when(facesRepository.countByApiKey(apiKey)).thenReturn(facesCount);

        val actual = faceDao.countFacesInModel(apiKey);

        assertThat(actual).isEqualTo(facesCount);

        verify(facesRepository).countByApiKey(apiKey);
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void addFaceFile() throws IOException {
        val embeddingNumbers = List.of(100500D);
        val embeddings = List.of(new Face.Embedding(embeddingNumbers, null));
        val faceName = "faceName";
        val modelKey = "modelKey";
        val faceId = new ObjectId("507f1f77bcf86cd799439011");

        val mockFile = new MockMultipartFile("mockFile", "".getBytes());

        when(gridFsOperations.store(any(InputStream.class), anyString())).thenReturn(faceId);

        val actual = faceDao.addNewFace(embeddings, mockFile, faceName, modelKey);

        assertThat(actual).isNotNull();
        assertThat(actual.getFaceName()).isEqualTo(faceName);
        assertThat(actual.getApiKey()).isEqualTo(modelKey);
        assertThat(actual.getFaceImgId()).isEqualTo(faceId);
        assertThat(actual.getRawImgId()).isEqualTo(faceId);
        assertThat(actual.getEmbeddings()).allSatisfy(
                embedding -> assertThat(embedding.getEmbedding()).isEqualTo(embeddingNumbers)
        );

        verify(gridFsOperations).store(any(InputStream.class), anyString());
        verify(facesRepository).save(any(Face.class));
        verifyNoMoreInteractions(gridFsOperations, facesRepository);
    }
}