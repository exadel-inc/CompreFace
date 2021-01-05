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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.exadel.frs.core.trainservice.config.IntegrationTest;
import com.exadel.frs.core.trainservice.exception.BasicException;
import com.exadel.frs.core.trainservice.exception.FileExtensionException;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.core.trainservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.core.trainservice.validation.ImageExtensionValidator;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@IntegrationTest
@AutoConfigureMockMvc
class DetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageExtensionValidator validator;

    @MockBean
    private FacesApiClient client;

    private static final String API_KEY = "api-key";

    @Test
    void testDetectFileExtensionException() throws Exception {
        // given
        String fileName = "file";
        val mockFile = new MockMultipartFile(fileName, "test data".getBytes());
        doThrow(new FileExtensionException(fileName)).when(validator).validate(mockFile);

        // when
        mockMvc.perform(
                multipart(API_V1 + "/faces/detection")
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
        when(client.findFaces(any(), any(), any(), any())).thenThrow(exception);

        // when
        mockMvc.perform(
                multipart(API_V1 + "/faces/detection")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
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
        when(client.findFaces(any(), any(), any(), any())).thenReturn(findResponse);

        // when
        mockMvc.perform(
                multipart(API_V1 + "/faces/detection")
                        .file(mockFile)
                        .header(X_FRS_API_KEY_HEADER, API_KEY)
        )
               // then
               .andExpect(status().isOk());
    }
}