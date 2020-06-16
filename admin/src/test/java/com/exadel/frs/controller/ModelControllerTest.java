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

package com.exadel.frs.controller;

import static com.exadel.frs.utils.TestUtils.buildExceptionResponse;
import static com.exadel.frs.utils.TestUtils.buildUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.dto.ui.ModelCreateDto;
import com.exadel.frs.dto.ui.ModelShareDto;
import com.exadel.frs.dto.ui.ModelUpdateDto;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import com.exadel.frs.mapper.MlModelMapper;
import com.exadel.frs.service.ModelService;
import com.exadel.frs.system.security.JwtAuthenticationFilter;
import com.exadel.frs.system.security.config.AuthServerConfig;
import com.exadel.frs.system.security.config.ResourceServerConfig;
import com.exadel.frs.system.security.config.WebSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ModelController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, WebSecurityConfig.class, AuthServerConfig.class, ResourceServerConfig.class}
        )
)
@MockBeans({@MockBean(MlModelMapper.class)})
class ModelControllerTest {

    private static final String ORG_GUID = "org-guid";
    private static final String APP_GUID = "app-guid";
    private static final String MODEL_GUID = "model-guid";

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ModelService modelService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMessageAndCodeWhenModelNameIsMissingOnUpdate() throws Exception {
        val expectedContent = "{\"message\":\"Model name cannot be empty\",\"code\":26}";
        val bodyWithEmptyName = new ModelUpdateDto();
        bodyWithEmptyName.setName("");

        val bodyWithNoName = new ModelUpdateDto();

        val updateRequest = put("/org/" + ORG_GUID + "/app/" + APP_GUID + "/model/" + MODEL_GUID)
                .with(csrf())
                .with(user(buildUser()))
                .contentType(APPLICATION_JSON);

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));

        mockMvc.perform(updateRequest.content(mapper.writeValueAsString(bodyWithNoName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    void shareModelShouldReturnErrorMessageWhenRequestIdIsMissing() throws Exception {
        doCallRealMethod().when(modelService).share(any(), any(), any(), any());
        val expectedContent = mapper.writeValueAsString(buildExceptionResponse(new EmptyRequiredFieldException("requestId")));

        val url = "/org/" + ORG_GUID + "/app/" + APP_GUID + "/model/" + MODEL_GUID + "/share";
        val requestToShareModel = post(url)
                                    .with(csrf())
                                    .with(user(buildUser()))
                                    .contentType(APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(new ModelShareDto()));


        mockMvc.perform(requestToShareModel)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }

    @Test
    void shouldReturnErrorMessageWhenNameIsMissingOnCreateNewModel() throws Exception {
        val expectedContent = "{\"message\":\"Model name cannot be empty\",\"code\":26}";
        val bodyWithEmptyName = new ModelCreateDto();
        bodyWithEmptyName.setName("");

        val bodyWithNoName = new ModelCreateDto();

        val createNewModelRequest = post("/org/" + ORG_GUID + "/app/" + APP_GUID + "/model")
                .with(csrf())
                .with(user(buildUser()))
                .contentType(APPLICATION_JSON);

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithEmptyName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));

        mockMvc.perform(createNewModelRequest.content(mapper.writeValueAsString(bodyWithNoName)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedContent));
    }
}