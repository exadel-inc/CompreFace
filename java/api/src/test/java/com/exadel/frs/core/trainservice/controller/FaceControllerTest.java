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

package com.exadel.frs.core.trainservice.controller;

import com.exadel.frs.commonservice.entity.App;
import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.entity.Model;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.handler.CommonExceptionCode;
import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.repository.ModelRepository;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.repository.AppRepository;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;
import java.util.stream.Collectors;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeFace;
import static com.exadel.frs.core.trainservice.ItemsBuilder.makeModel;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FaceControllerTest extends EmbeddedPostgreSQLTest {

    private static final String INCORRECT_PART_NAME = "File";
    private static final String API_KEY = "model_key";

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ModelRepository modelRepository;

    @Autowired
    AppRepository appRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    FacesRepository facesRepository;

    @Autowired
    FaceCacheProvider faceCacheProvider;

    @SpyBean
    FaceService faceService;

    @MockBean
    ImageExtensionValidator imageValidator;

    @MockBean
    FaceClassifierPredictor predictor;

    @MockBean
    FacesApiClient client;

    private App app;
    private Model model;

    @BeforeAll
    public void setUpAppAndModel() {
        app = App.builder()
                .name("App" + System.currentTimeMillis())
                .guid(UUID.randomUUID().toString())
                .apiKey(API_KEY)
                .build();
        appRepository.save(app);

        model = makeModel(API_KEY, ModelType.RECOGNITION, app);
        modelRepository.save(model);
    }

    @AfterAll
    public void clearAppAndModel() {
        appRepository.delete(app);
        modelRepository.delete(model);
    }

    @BeforeEach
    public void beforeEach() {
        // clear data after each test
        facesRepository.deleteFacesByApiKey(API_KEY);
        faceCacheProvider.invalidate(API_KEY);
    }

    @Test
    void findAndSaveFaces() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        doReturn(new FaceResponseDto("id", "name"))
                .when(faceService)
                .findAndSaveFace(any(), any(), any(), any());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(faceService).findAndSaveFace(any(), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, faceService);
    }

    @Test
    void findAndSaveFacesForFirstItemWithEmptyRetrain() throws Exception {
        doReturn(new FaceResponseDto("id", "name"))
                .when(faceService)
                .findAndSaveFace(any(), any(), any(), any());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces")
                        .file(new MockMultipartFile("file", "test data".getBytes()))
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(faceService).findAndSaveFace(any(), any(), any(), any());

        verifyNoMoreInteractions(imageValidator, faceService);
    }

    @Test
    void subjectUpdateHappyPath() throws Exception {
        final String oldSubject = "name1";
        final String newSubject = "new_subj";

        saveFaces(
                makeFace(oldSubject, API_KEY),
                makeFace(oldSubject, API_KEY),
                makeFace("name2", API_KEY)
        );

        mockMvc.perform(put(API_V1 + "/recognition/faces")
                .param("subject", oldSubject)
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("subject", newSubject)))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated", Matchers.is(2)));

        // make sure cache has been updated (invalidated?)
        assertThat(
                faceCacheProvider
                        .getOrLoad(API_KEY)
                        .getFaces()
                        .stream()
                        .filter(f -> newSubject.equals(f.getName()))
                        .count(),
                Matchers.is(2L)
        );
    }

    @Test
    void subjectUpdateErrorWhenUpdatingWithEmptySubj() throws Exception {
        mockMvc.perform(put(API_V1 + "/recognition/faces")
                .param("subject", "any")
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("subject", ""))) // empty new subj
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", Matchers.is(CommonExceptionCode.VALIDATION_CONSTRAINT_VIOLATION.getCode())));
    }

    @Test
    void subjectUpdateErrorWhenNoQuerySubj() throws Exception {
        mockMvc.perform(put(API_V1 + "/recognition/faces")
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("subject", "any")))
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", Matchers.is(CommonExceptionCode.MISSING_REQUEST_PARAMETER.getCode())));
    }

    @Test
    void subjectUpdateErrorWhenUpdatingNonExistingSubject() throws Exception {
        final String nameToUpdate = "name1";

        saveFaces(
                makeFace("B", API_KEY),
                makeFace("B", API_KEY)
        );

        mockMvc.perform(put(API_V1 + "/recognition/faces")
                .param("subject", nameToUpdate)
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("subject", "new_name")))
        )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", Matchers.is(CommonExceptionCode.SUBJECT_NOT_FOUND.getCode())));
    }

    @Test
    void findAllShouldReturnResponseAsExpected() throws Exception {
        val expected = saveFaces(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY)
        )
                .stream()
                .map(face -> new FaceResponseDto(face.getId(), face.getFaceName()))
                .collect(Collectors.toList());

        mockMvc.perform(get(API_V1 + "/recognition/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(Map.of("faces", expected))));
    }

    @Test
    void findAllShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(get(API_V1 + "/recognition/faces"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    void deleteFacesByIdShouldReturnResponseAsExpected() throws Exception {
        List<Face> faces = saveFaces(makeFace("A", API_KEY));

        mockMvc.perform(delete(API_V1 + "/recognition/faces/" + faces.iterator().next().getId())
                .header(X_FRS_API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFacesByNameShouldReturnResponseAsExpected() throws Exception {
        Face face = saveFaces(makeFace("face_name", API_KEY)).iterator().next();

        val expectedFaces = Collections.singletonList(new FaceResponseDto(face.getId(), face.getFaceName()));

        mockMvc.perform(delete(API_V1 + "/recognition/faces").header(X_FRS_API_KEY_HEADER, API_KEY).param("subject", face.getFaceName()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedFaces)));
    }

    @Test
    void deleteFacesByModelShouldReturnResponseAsExpected() throws Exception {
        saveFaces(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );

        mockMvc.perform(delete(API_V1 + "/recognition/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void deleteFacesByModelFacesShouldReturnBadRequestWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(delete(API_V1 + "/recognition/faces"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", Matchers.is(String.format("Missing header: %s", X_FRS_API_KEY_HEADER))))
                .andExpect(jsonPath("$.code", Matchers.is(CommonExceptionCode.MISSING_REQUEST_HEADER.getCode())));
    }

    @Test
    void verifyFaces() throws Exception {
        val faceA = makeFace("A", API_KEY);

        saveFaces(
                faceA,
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );

        val findFacesResponse = FindFacesResponse
                .builder()
                .result(List.of(
                        FindFacesResult
                                .builder()
                                .embedding(new Double[]{1.0})
                                .box(new FacesBox().setProbability(1D))
                                .build()
                ))
                .build();

        when(client.findFacesWithCalculator(any(), any(), any(), isNull())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any(), any())).thenReturn(eq(0.0));

        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces/" + faceA.getId() + "/verify")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(imageValidator).validate(any());
        verify(client).findFacesWithCalculator(any(), any(), any(), anyString());
        verifyNoMoreInteractions(imageValidator, client, predictor);
    }

    @Test
    void verifyFacesWithIncorrectPartName() throws Exception {
        val faceA = makeFace("A", API_KEY);

        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        MvcResult mvcResult = mockMvc.perform(
                multipart(API_V1 + "/recognition/faces/" + faceA.getId() + "/verify")
                        .file(INCORRECT_PART_NAME, mockFile.getBytes())
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isBadRequest()).andReturn();

        Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains("Required part "));
    }

    private List<Face> saveFaces(Face... faces) {
        return facesRepository.saveAll(Arrays.asList(faces));
    }
}