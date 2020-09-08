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

import static com.exadel.frs.core.trainservice.enums.RetrainOption.NO;
import static com.exadel.frs.core.trainservice.repository.FacesRepositoryTest.makeFace;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dao.TrainedModelDao;
import com.exadel.frs.core.trainservice.dto.ui.FaceResponseDto;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.service.ScanService;
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
    private TrainedModelDao trainedModelDao;

    @MockBean
    private ScanService scanService;

    @MockBean
    private ImageExtensionValidator imageValidator;

    private static final String API_KEY = "model_key";

    @Test
    void scanFaces() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());

        mockMvc.perform(
                multipart(API_V1 + "/faces")
                        .file(mockFile)
                        .param("retrain", NO.name())
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

        doReturn(1)
                .when(facesRepository)
                .countByApiKey(API_KEY);

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

        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(API_KEY);

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
        val faceId = randomUUID().toString();
        val response = Optional.of(new Face());

        doReturn(response)
                .when(facesRepository)
                .findById(faceId);

        mockMvc.perform(delete(API_V1 + "/faces/" + faceId)
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .param("retrain", NO.name())
        ).andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByNameShouldReturnResponseAsExpected() throws Exception {
        val faceName = "faceName";
        val response = List.of(new Face(), new Face(), new Face());

        doReturn(response)
                .when(facesRepository)
                .deleteByApiKeyAndFaceName(API_KEY, faceName);

        mockMvc.perform(delete(API_V1 + "/faces")
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .param("retrain", NO.name())
                .param("subject", faceName)
        ).andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByModelShouldReturnResponseAsExpected() throws Exception {
        val faces = List.of(
                makeFace("A", API_KEY),
                makeFace("B", API_KEY),
                makeFace("C", API_KEY)
        );
        doReturn(faces)
                .when(facesRepository)
                .deleteFacesByApiKey(API_KEY);
        val expectedFaces = faces.stream()
                                 .map(face -> FaceResponseDto.builder()
                                                             .image_id(face.getId())
                                                             .subject(face.getFaceName())
                                                             .build()
                                 )
                                 .collect(toList());
        val expectedContent = new ObjectMapper().writeValueAsString(expectedFaces);
        mockMvc.perform(delete(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedContent));
    }

    @Test
    public void deleteFacesByModelFacesShouldReturnBadRequestWhenApiKeyIsMissing() throws Exception {
        val expectedContent = "{\"message\":\"" + String.format("Missing header: %s", X_FRS_API_KEY_HEADER) + "\",\"code\":20}";

        mockMvc.perform(delete(API_V1 + "/faces"))
               .andExpect(status().isBadRequest())
               .andExpect(content().string(expectedContent));
    }
}