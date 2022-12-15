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

import com.exadel.frs.commonservice.exception.BasicException;
import com.exadel.frs.commonservice.exception.FileExtensionException;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.commonservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.system.global.Constants;
import com.exadel.frs.core.trainservice.EmbeddedPostgreSQLTest;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.dto.Base64File;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Base64;
import java.util.stream.Stream;

import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
class DetectionControllerTest extends EmbeddedPostgreSQLTest {

    public static final String DETECT = "/detection/detect";
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageExtensionValidator validator;

    @MockBean
    private FacesApiClient client;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY = "api-key";

    @Test
    void testDetectFileExtensionException() throws Exception {
        // given
        String fileName = "file";
        val mockFile = new MockMultipartFile(fileName, "test data".getBytes());
        doThrow(new FileExtensionException(fileName)).when(validator).validate(mockFile);

        // when
        mockMvc.perform(
                multipart(API_V1 + DETECT)
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
                // then
                .andExpect(status().isBadRequest());
    }

    static Stream<Arguments> verifyClientReturnsException() {
        return Stream.of(
                Arguments.of(new NoFacesFoundException(), status().isBadRequest()),
                Arguments.of(new FacesServiceException("error"), status().isInternalServerError())
        );
    }

    @ParameterizedTest
    @MethodSource("verifyClientReturnsException")
    void testDetectFacesException(BasicException exception, ResultMatcher matcher) throws Exception {
        // given
        String fileName = "file";
        val mockFile = new MockMultipartFile(fileName, "test data".getBytes());
        doNothing().when(validator).validate(mockFile);
        when(client.findFaces(any(), any(), any(), any(), any())).thenThrow(exception);

        // when
        mockMvc.perform(
                multipart(API_V1 + DETECT)
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
                .andDo(MockMvcResultHandlers.print())
                // then
                .andExpect(matcher);
    }

    @Test
    void testDetect() throws Exception {
        // given
        String fileName = "file";
        val mockFile = new MockMultipartFile(fileName, "test data".getBytes());
        val findResponse = new FindFacesResponse();
        doNothing().when(validator).validate(mockFile);
        when(client.findFaces(any(), any(), any(), any(), any())).thenReturn(findResponse);

        // when
        mockMvc.perform(
                multipart(API_V1 + DETECT)
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
                // then
                .andExpect(status().isOk());
    }

    @Test
    void testDetectBase64() throws Exception {
        // given
        val findResponse = new FindFacesResponse();
        doNothing().when(validator).validateBase64(any());
        when(client.findFacesBase64(any(), any(), any(), any(), any())).thenReturn(findResponse);

        Base64File request = new Base64File();
        request.setContent(Base64.getEncoder().encodeToString(new byte[]{(byte) 0xCA}));

        // when
        mockMvc.perform(
                post(API_V1 + DETECT)
                        .queryParam("limit", "4")
                        .queryParam(Constants.DET_PROB_THRESHOLD, "0.7")
                        .queryParam(Constants.FACE_PLUGINS, "faceplug")
                        .queryParam("status", "true")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(request))
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
                // then
                .andExpect(status().isOk());
    }
}