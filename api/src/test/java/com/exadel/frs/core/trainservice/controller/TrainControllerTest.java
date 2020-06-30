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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.service.RetrainService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
public class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RetrainService retrainService;

    private static final String MODEL_KEY = "model_key";
    private static final String API_KEY = MODEL_KEY;

    @Test
    void train() throws Exception {
        mockMvc.perform(post(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isAccepted());

        verify(retrainService).startRetrain(API_KEY);
    }

    @Test
    void getStatus() throws Exception {
        when(retrainService.isTrainingRun(MODEL_KEY)).thenReturn(false);

        mockMvc.perform(get(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isOk());
    }

    @Test
    void abortRetrain() throws Exception {

        mockMvc.perform(delete(API_V1 + "/retrain").header(X_FRS_API_KEY_HEADER, API_KEY))
               .andExpect(status().isAccepted());
    }
}