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
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.dto.ui.FaceResponseDto;
import com.exadel.frs.core.trainservice.entity.postgres.Face;
import com.exadel.frs.core.trainservice.entity.mongo.Model;
import com.exadel.frs.core.trainservice.repository.postgres.FacesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
public class FaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacesRepository facesRepository;

    @MockBean
    private ModelDao modelDao;

    private static final String API_KEY = "model_key";

    @Test
    public void findAllShouldReturnResponseAsExpected() throws Exception {
        val faces = List.of(
                makeFace(1L,"A", API_KEY),
                makeFace(2L,"B", API_KEY)
        );

        doReturn(faces)
                .when(facesRepository)
                .findByApiKey(API_KEY);

        val expectedFaces = faces.stream()
                                 .map(face -> FaceResponseDto.builder()
                                                             .id(face.getId())
                                                             .name(face.getFaceName())
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
        mockMvc.perform(get(API_V1 + "/faces"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void deleteFacesByIdShouldReturnResponseAsExpected() throws Exception {
        val faceId = 12345L;
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

        mockMvc.perform(delete(API_V1 + "/faces/name/" + faceName)
                .header(X_FRS_API_KEY_HEADER, API_KEY)
                .param("retrain", NO.name())
        ).andExpect(status().isOk());
    }

    @Test
    public void deleteFacesByModelShouldReturnResponseAsExpected() throws Exception {
        val response = List.of(new Face(), new Face(), new Face());
        doReturn(response)
                .when(facesRepository)
                .deleteFacesByApiKey(API_KEY);

        mockMvc.perform(delete(API_V1 + "/faces").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk())
               .andExpect(content().string(String.valueOf(response.size())));
    }

    @Test
    public void deleteFacesByModelFacesShouldReturnBadRequestWhenApiKeyIsMissing() throws Exception {
        mockMvc.perform(delete(API_V1 + "/faces"))
               .andExpect(status().isBadRequest());
    }

    @Test
    public void updateModelKeySuccess() throws Exception {
        val newModelKey = UUID.randomUUID().toString();

        doReturn(Model.builder().build())
                .when(modelDao).updateModelApiKey(any(), any());

        mockMvc.perform(put(API_V1 + "/models/api-key?new_model_api_key=" + newModelKey).header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }
}