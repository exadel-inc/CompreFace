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

import com.exadel.frs.commonservice.repository.FacesRepository;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.PluginsVersions;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.commonservice.system.global.Constants;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.cache.FaceCollection;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import com.exadel.frs.core.trainservice.service.FaceService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.exadel.frs.core.trainservice.ItemsBuilder.makeFace;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@IntegrationTest
@AutoConfigureMockMvc
public class FaceControllerTest extends EmbeddedPostgreSQLTest {

    public static final String INCORRECT_PART_NAME = "File";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacesRepository facesRepository;

    @SpyBean
    private FaceService faceService;

    @MockBean
    private ImageExtensionValidator imageValidator;

    @MockBean
    private FaceCacheProvider faceCacheProvider;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private FacesApiClient client;

    private static final String API_KEY = "model_key";

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void findAndSaveFaces() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        doReturn(new FaceResponseDto("id", "name"))
                .when(faceService)
                .findAndSaveFace(any(MultipartFile.class), any(), any(), any());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(faceService).findAndSaveFace(any(MultipartFile.class), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, faceService);
    }

    @Test
    void findAndSaveFacesBase64() throws Exception {
        doReturn(new FaceResponseDto("id", "name"))
                .when(faceService)
                .findAndSaveFace(any(String.class), any(), any(), any());

        Base64File request = new Base64File();
        request.setContent(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}));

        mockMvc.perform(
                post(API_V1 + "/recognition/faces")
                        .queryParam("subject", "subject")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validateBase64(any());
        verify(faceService).findAndSaveFace(any(String.class), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, faceService);
    }

    @Test
    void findAndSaveFacesForFirstItemWithEmptyRetrain() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        val faceCollection = FaceCollection.buildFromFaces(List.of(
                makeFace("A", API_KEY)
        ));

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);
        doReturn(new FaceResponseDto("id", "name"))
                .when(faceService)
                .findAndSaveFace(any(MultipartFile.class), any(), any(), any());

        mockMvc.perform(
                multipart(API_V1 + "/recognition/faces")
                        .file(mockFile)
                        .param("subject", "name")
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isCreated());

        verify(imageValidator).validate(any());
        verify(faceService).findAndSaveFace(any(MultipartFile.class), any(), any(), any());
        verifyNoMoreInteractions(imageValidator, faceService);
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

        mockMvc.perform(get(API_V1 + "/recognition/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedContent));
    }

    @Test
    public void findAllShouldReturnBadRequestWhenAppGuidIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(get(API_V1 + "/recognition/faces"))
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

        mockMvc.perform(delete(API_V1 + "/recognition/faces/" + face.getId())
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

        mockMvc.perform(delete(API_V1 + "/recognition/faces")
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

        mockMvc.perform(delete(API_V1 + "/recognition/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByModelFacesShouldReturnBadRequestWhenApiKeyIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(delete(API_V1 + "/recognition/faces"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void verifyFaces(boolean status) throws Exception {
        val faceA = makeFace("A", API_KEY);
        val faceB = makeFace("B", API_KEY);
        val faceC = makeFace("C", API_KEY);
        val faces = List.of(faceA, faceB, faceC);
        val faceCollection = FaceCollection.buildFromFaces(faces);

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .pluginsVersions(PluginsVersions.builder().calculator("fake_calc").detector("detector").build())
                .build();

        when(client.findFacesWithCalculator(any(), any(), any(), anyString())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any(), any())).thenReturn(eq(0.0));

        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        ResultActions result = mockMvc.perform(
                multipart(API_V1 + "/recognition/faces/" + faceA.getId() + "/verify")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
                        .param("status", Boolean.toString(status))
        ).andExpect(status().isOk());

        if (status) {
            result.andExpect(jsonPath("$.plugins_versions.calculator", is("fake_calc")));
        } else {
            result.andExpect(jsonPath("$.plugins_versions").doesNotExist());
        }

        verify(imageValidator).validate(any());
        verify(client).findFacesWithCalculator(any(), any(), any(), anyString());
    }

    @Test
    void verifyFacesBase64() throws Exception {
        val faceA = makeFace("A", API_KEY);
        val faceB = makeFace("B", API_KEY);
        val faceC = makeFace("C", API_KEY);
        val faces = List.of(faceA, faceB, faceC);
        val faceCollection = FaceCollection.buildFromFaces(faces);

        doReturn(faceCollection)
                .when(faceCacheProvider)
                .getOrLoad(API_KEY);

        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesBase64WithCalculator(any(), any(), any(), isNull())).thenReturn(findFacesResponse);
        when(predictor.verify(any(), any(), any())).thenReturn(eq(0.0));

        Base64File request = new Base64File();
        request.setContent(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA}));

        mockMvc.perform(
                post(API_V1 + "/recognition/faces/" + faceA.getId() + "/verify")
                        .queryParam("limit", "4")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .queryParam(Constants.FACE_PLUGINS, "faceplug")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());

        verify(imageValidator).validateBase64(any());
        verify(client).findFacesBase64WithCalculator(any(), any(), any(), anyString());

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
}