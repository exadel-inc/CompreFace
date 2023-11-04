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

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesBox;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResult;
import com.exadel.frs.commonservice.system.global.Constants;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.service.NotificationReceiverService;
import com.exadel.frs.core.trainservice.service.NotificationSenderService;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.List;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class RecognizeControllerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private ImageExtensionValidator validator;

    @MockBean
    private FacesApiClient client;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MODEL_KEY = "model_key";
    private static final String API_KEY = MODEL_KEY;

    @Test
    void recognize() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());
        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesWithCalculator(any(), any(), any(), isNull(), any())).thenReturn(findFacesResponse);
        when(predictor.predict(any(), any(), anyInt())).thenReturn(List.of(Pair.of(1.0, "")));
        doNothing().when(validator).validate(mockFile);

        mockMvc.perform(
                multipart(API_V1 + "/recognition/recognize")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());
    }

    @Test
    void recognizeBase64() throws Exception {
        val findFacesResponse = FindFacesResponse.builder()
                .result(List.of(FindFacesResult.builder()
                        .embedding(new Double[]{1.0})
                        .box(new FacesBox().setProbability(1D))
                        .build()
                ))
                .build();

        when(client.findFacesBase64WithCalculator(any(), any(), any(), isNull(), any())).thenReturn(findFacesResponse);
        when(predictor.predict(any(), any(), anyInt())).thenReturn(List.of(Pair.of(1.0, "")));
        doNothing().when(validator).validateBase64(any());

        Base64File request = new Base64File();
        request.setContent(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA}));

        mockMvc.perform(
                post(API_V1 + "/recognition/recognize")
                        .queryParam("limit", "4")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .queryParam(Constants.FACE_PLUGINS, "faceplug")
                        .queryParam("status", "true")
                        .queryParam("prediction_count", "44")
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());
    }
}