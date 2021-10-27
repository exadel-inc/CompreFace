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

import com.exadel.frs.commonservice.dto.ExecutionTimeDto;
import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.Subject;
import com.exadel.frs.commonservice.exception.IncorrectImageIdException;
import com.exadel.frs.commonservice.exception.TooManyFacesException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.exadel.frs.core.trainservice.cache.EmbeddingCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.component.classifiers.EuclideanDistanceClassifier;
import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.exadel.frs.core.trainservice.dto.ProcessImageParams;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeEmbedding;
import static com.exadel.frs.core.trainservice.service.SubjectService.MAX_FACES_TO_RECOGNIZE;
import static com.exadel.frs.core.trainservice.system.global.Constants.IMAGE_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class SubjectServiceTest {

    private static final String API_KEY = "apiKey";

    @Mock
    private SubjectDao subjectDao;

    @Mock
    private FacesApiClient facesApiClient;

    @Mock
    private EmbeddingCacheProvider embeddingCacheProvider;

    @Mock // required for constructor
    private FaceClassifierPredictor classifierPredictor;

    @Mock
    private EuclideanDistanceClassifier euclideanDistanceClassifier;

    @InjectMocks
    private SubjectService subjectService;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    void testDeleteSubjectByApiKey() {
        subjectService.deleteSubjectsByApiKey(API_KEY);

        // verify deleted from DB
        verify(subjectDao).deleteSubjectsByApiKey(API_KEY);
        // verify no more embedding collection in cache
        verify(embeddingCacheProvider).invalidate(API_KEY);
    }

    @Test
    void testRemoveAllSubjectEmbeddings() {
        var subjectName = "subject_name";

        when(subjectDao.removeAllSubjectEmbeddings(API_KEY, subjectName)).thenReturn(1);

        subjectService.removeAllSubjectEmbeddings(API_KEY, subjectName);

        // verify deleted from DB
        verify(subjectDao).removeAllSubjectEmbeddings(API_KEY, subjectName);
        // verify cache
        verify(embeddingCacheProvider).ifPresent(eq(API_KEY), any());
    }

    @Test
    void deleteSubjectByName() {
        var subjectName = "subject_name";
        subjectService.deleteSubjectByName(API_KEY, subjectName);

        // verify deleted from DB
        verify(subjectDao).deleteSubjectByName(API_KEY, subjectName);
        // verify cache
        verify(embeddingCacheProvider).ifPresent(eq(API_KEY), any());
    }

    @Test
    void testRemoveSubjectEmbedding() {
        var embeddingId = UUID.randomUUID();

        when(subjectDao.removeSubjectEmbedding(API_KEY, embeddingId)).thenReturn(new Embedding());

        subjectService.removeSubjectEmbedding(API_KEY, embeddingId);

        // verify deleted from DB
        verify(subjectDao).removeSubjectEmbedding(API_KEY, embeddingId);
        // verify cache update attempt
        verify(embeddingCacheProvider).ifPresent(eq(API_KEY), any());
    }

    static Stream<Arguments> subjectNamePairsFailed() {
        return Stream.of(
                Arguments.of("old", "old"),
                Arguments.of("old", ""),
                Arguments.of("old", null)
        );
    }

    @ParameterizedTest
    @MethodSource("subjectNamePairsFailed")
    void testUpdateSubjectNameFailed(String oldSubjectName, String newSubjectName) {
        final boolean updated = subjectService.updateSubjectName(API_KEY, oldSubjectName, newSubjectName);
        assertThat(updated).isFalse();
        verifyNoInteractions(subjectDao, embeddingCacheProvider);
    }

    static Stream<Arguments> subjectNamePairsSuccess() {
        return Stream.of(
                Arguments.of("old", "new"),
                Arguments.of("old", "oLd")
        );
    }

    @ParameterizedTest
    @MethodSource("subjectNamePairsSuccess")
    void testUpdateSubjectNameSuccess(String oldSubjectName, String newSubjectName) {
        when(subjectDao.updateSubjectName(API_KEY, oldSubjectName, newSubjectName)).thenReturn(true);

        final boolean updated = subjectService.updateSubjectName(API_KEY, oldSubjectName, newSubjectName);
        assertThat(updated).isTrue();

        // verify cache update attempt
        verify(embeddingCacheProvider).ifPresent(eq(API_KEY), any());
    }

    @Test
    void testSaveCalculatedEmbedding() throws IOException {
        var subjectName = "subject_name";
        var detProbThreshold = 0.7;
        MultipartFile file = new MockMultipartFile("anyname", new byte[]{0xA});

        when(facesApiClient.findFacesWithCalculator(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold, null))
                .thenReturn(findFacesResponse(1));
        when(euclideanDistanceClassifier.normalizeOne(any()))
                .thenReturn(new double[]{1.1, 2.2});
        when(subjectDao.addEmbedding(eq(API_KEY), eq(subjectName), any()))
                .thenReturn(Pair.of(new Subject(), new Embedding()));

        var pair = subjectService.saveCalculatedEmbedding(file, subjectName, detProbThreshold, API_KEY);

        assertThat(pair).isNotNull();
    }

    @Test
    void tooManyFacesFound() {
        var subjectName = "subject_name";
        var detProbThreshold = 0.7;
        MultipartFile file = new MockMultipartFile("anyname", new byte[]{0xA});

        when(facesApiClient.findFacesWithCalculator(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold, null))
                .thenReturn(findFacesResponse(3));

        assertThatThrownBy(() ->
                subjectService.saveCalculatedEmbedding(file, subjectName, detProbThreshold, API_KEY)
        ).isInstanceOf(TooManyFacesException.class);

        verifyNoInteractions(subjectDao);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testVerifyFaces(boolean status) {
        var detProbThreshold = 0.7;
        var randomUUId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("anyname", new byte[]{0xA});
        EmbeddingCollection embeddingCollection = EmbeddingCollection.from(Stream.of(
                makeEmbedding(randomUUId,"A", API_KEY),
                makeEmbedding("B", API_KEY)));

        when(facesApiClient.findFacesWithCalculator(any(), any(), any(), any()))
                .thenReturn(findFacesResponse(2));
        when(embeddingCacheProvider.getOrLoad(API_KEY))
                .thenReturn(embeddingCollection);
        when(classifierPredictor.verify(any(), any(), any()))
                .thenReturn(0.0);

        var result = subjectService.verifyFace(
                ProcessImageParams.builder()
                        .apiKey(API_KEY)
                        .file(file)
                        .limit(MAX_FACES_TO_RECOGNIZE)
                        .detProbThreshold(detProbThreshold)
                        .status(status)
                        .additionalParams(Map.of(IMAGE_ID, randomUUId))
                        .build()
        );

        var verifications = result.getLeft();
        assertThat(verifications).hasSize(2);

        if (status) {
            verifications.forEach(v -> assertThat(v.getExecutionTime()).isNotNull());
            assertThat(result.getRight()).isNotNull();
        } else {
            verifications.forEach(v -> assertThat(v.getExecutionTime()).isNull());
            assertThat(result.getRight()).isNull();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testInvalidImageIdException(boolean status){
        var detProbThreshold = 0.7;
        var randomUUId = UUID.randomUUID();
        MultipartFile file = new MockMultipartFile("anyname", new byte[]{0xA});
        EmbeddingCollection embeddingCollection = EmbeddingCollection.from(Stream.of(
                makeEmbedding("A", API_KEY),
                makeEmbedding("B", API_KEY)));

        when(facesApiClient.findFacesWithCalculator(any(), any(), any(), any()))
                .thenReturn(findFacesResponse(2));
        when(embeddingCacheProvider.getOrLoad(API_KEY))
                .thenReturn(embeddingCollection);
        when(classifierPredictor.verify(any(), any(), any()))
                .thenReturn(0.0);
        assertThrows(IncorrectImageIdException.class, ()->  subjectService.verifyFace(
                ProcessImageParams.builder()
                        .apiKey(API_KEY)
                        .file(file)
                        .limit(MAX_FACES_TO_RECOGNIZE)
                        .detProbThreshold(detProbThreshold)
                        .status(status)
                        .additionalParams(Map.of(IMAGE_ID, randomUUId))
                        .build()
        ));
    }

    private static FindFacesResponse findFacesResponse(int faceCount) {
        return FindFacesResponse.builder()
                .result(
                        IntStream.range(0, faceCount)
                                .mapToObj(i -> {
                                    final FacesBox facesBox = new FacesBox();
                                    facesBox.setProbability(1.0);

                                    ExecutionTimeDto executionTimeDto = new ExecutionTimeDto();
                                    executionTimeDto.setCalculator(111.1);

                                    return FindFacesResult.builder().embedding(new Double[]{1.1, 2.2}).box(facesBox).executionTime(executionTimeDto).build();
                                })
                                .collect(Collectors.toList())
                )
                .pluginsVersions(PluginsVersions.builder().calculator("calculator").build())
                .build();
    }
}