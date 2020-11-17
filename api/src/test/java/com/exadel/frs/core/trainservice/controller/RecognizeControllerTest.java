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

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.component.FaceClassifierPredictor;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanBox;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResult;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class RecognizeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FaceClassifierPredictor predictor;

    @MockBean
    private FacesClient client;

    @MockBean
    private ImageExtensionValidator imageValidator;

    private static final String MODEL_KEY = "model_key";
    private static final String API_KEY = MODEL_KEY;

    @Test
    void recognize() throws Exception {
        val mockFile = new MockMultipartFile("file", "test data".getBytes());
        val scanResponse = new ScanResponse().setResult(
                List.of(new ScanResult()
                        .setEmbedding(List.of(1.0))
                        .setBox(new ScanBox().setProbability(1D))
                )
        );

        when(client.scanFaces(any(), any(), any())).thenReturn(scanResponse);
        when(predictor.predict(any(), any(), anyInt())).thenReturn(List.of(Pair.of(1.0, "")));

        mockMvc.perform(
                multipart(API_V1 + "/faces/recognize")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        ).andExpect(status().isOk());
    }
}