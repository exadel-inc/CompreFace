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

import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.ui.FaceResponseDto;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.service.ScanService;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanBox;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
public class FaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacesRepository facesRepository;

    @MockBean
    private ScanService scanService;

    @MockBean
    private ImageExtensionValidator imageValidator;

    @MockBean
    private FaceCacheProvider faceCacheProvider;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private FacesClient client;

    private static final String API_KEY = "model_key";

    @Test
    void scanFaces() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(scanService).scanAndSaveFace(any(), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, scanService);
    }

    @Test
    void scanFacesForFirstItemWithEmptyRetrain() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        val faceCollection = FaceCollection.buildFromFaces(List.of(
                makeFace("A", API_KEY)
        ));

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        mockMvc.perform(
                multipart(API_V1 + "/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(scanService).scanAndSaveFace(any(), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, scanService);
    }

    @Test
    public void findAllShouldReturnResponseAsExpected() throws Exception {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY)
        );

        val faceCollection = FaceCollection.buildFromFaces(faces);

        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(API_KEY);
        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        val expectedFaces = faces.stream()
                                 .map(face -> FaceResponseDto.builder()
                                                             .image_id(face.getId())
                                                             .subject(face.getFaceName())
                                                             .build()
                                 )
                                 .collect(toList());

        val expectedContent = new ObjectMapper().writeValueAsString(Map.of("faces", expectedFaces));

        mockMvc.perform(get(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedContent));
    }

    @Test
    public void findAllShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(get(API_V1 + "/faces"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    public void deleteFacesByIdShouldReturnResponseAsExpected() throws Exception {
        val face = makeFace("A", API_KEY);
        val faceCollection = FaceCollection.buildFromFaces(List.of(face));

        doReturn(Optional.of(face))
                .when(facesRepository)
                .findById(face.getId());
        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        mockMvc.perform(delete(API_V1 + "/faces/" + face.getId())
                .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByNameShouldReturnResponseAsExpected() throws Exception {
        val faceName = "faceName";
        val face = makeFace(faceName, API_KEY);
        val faceCollection = FaceCollection.buildFromFaces(List.of(face));

        doReturn(List.of(face))
                .when(facesRepository)
                .deleteByApiKeyAndFaceName(API_KEY, faceName);
        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        val expectedFaces = List.of(FaceResponseDto.builder()
                                                   .image_id(face.getId())
                                                   .subject(face.getFaceName())
                                                   .build());
        val expectedContent = new ObjectMapper().writeValueAsString(expectedFaces);

        mockMvc.perform(delete(API_V1 + "/faces")
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .param("subject", faceName)
        )
               .andExpect(status().isOk())
               .andExpect(content().json(expectedContent));
    }

    @Test
    public void deleteFacesByModelShouldReturnResponseAsExpected() throws Exception {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );
        val faceCollection = FaceCollection.buildFromFaces(faces);

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        mockMvc.perform(delete(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByModelFacesShouldReturnBadRequestWhenApiKeyIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(delete(API_V1 + "/faces"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }

    @Test
    void verifyFaces() throws Exception {
        val faceA = makeFace("A", API_KEY);
        val faceB = makeFace("B", API_KEY);
        val faceC = makeFace("C", API_KEY);
        val faces = List.of(faceA, faceB, faceC);
        val faceCollection = FaceCollection.buildFromFaces(faces);

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        val scanResponse = new ScanResponse().setResult(
                List.of(new ScanResult()
                        .setEmbedding(List.of(1.0))
                        .setBox(new ScanBox().setProbability(1D))
                )
        );

        when(client.scanFaces(any(), any(), any())).thenReturn(scanResponse);
        when(predictor.verify(any(), any(), any())).thenReturn(1.0);

        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/faces/" + faceA.getId() + "/verify")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(imageValidator).validate(any());
        verify(client).scanFaces(any(), any(), any());
        verify(predictor).verify(any(), any(), any());
        verifyNoMoreInteractions(imageValidator, client, predictor);
    }
}