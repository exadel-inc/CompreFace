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

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Image;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.repository.ImagesRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;

class FaceDaoTest {

    @Mock
    private FacesRepository facesRepository;

    @Mock
    private ImagesRepository imagesRepository;

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
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteFaceById() {
        val faceGuid = randomUUID().toString();
        val face = Optional.of(new Face());
        when(facesRepository.findById(faceGuid)).thenReturn(face);

        val actual = faceDao.deleteFaceById(faceGuid);

        assertThat(actual).isEqualTo(face.get());

        verify(facesRepository).findById(faceGuid);
        verify(facesRepository).delete(face.get());
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteNonexistentFaceById() {
        val faceGuid = randomUUID().toString();
        when(facesRepository.findById(faceGuid)).thenReturn(Optional.empty());

        val actual = faceDao.deleteFaceById(faceGuid);

        assertThat(actual).isNull();

        verify(facesRepository).findById(faceGuid);
        verifyNoMoreInteractions(facesRepository);
    }

    @Test
    void deleteFacesByApiKey() {
        val apiKey = randomAlphabetic(10);

        faceDao.deleteFacesByApiKey(apiKey);

        verify(facesRepository).deleteFacesByApiKey(apiKey);
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
        val embeddings = new Face.Embedding(embeddingNumbers, "1.0");
        val faceName = "faceName";
        val modelKey = "modelKey";
        val faceId = "507f1f77bcf86cd799439011";

        val mockFile = new MockMultipartFile("mockFile", faceId.getBytes());

        val actual = faceDao.addNewFace(embeddings, mockFile, faceName, modelKey);

        assertThat(actual).isNotNull();
        assertThat(actual.getFaceName()).isEqualTo(faceName);
        assertThat(actual.getApiKey()).isEqualTo(modelKey);
        assertThat(actual.getEmbedding().getEmbeddings()).isEqualTo(embeddingNumbers);

        verify(imagesRepository).save(any(Image.class));
        verifyNoMoreInteractions(imagesRepository);
    }
}