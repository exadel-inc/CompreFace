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
package com.exadel.frs.core.trainservice.sdk.faces.service;

import static com.exadel.frs.core.trainservice.system.global.Constants.CALCULATOR_PLUGIN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.exadel.frs.commonservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.commonservice.sdk.faces.exception.NoFacesFoundException;
import com.exadel.frs.commonservice.sdk.faces.feign.FacesFeignClient;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FindFacesResponse;
import com.exadel.frs.commonservice.sdk.faces.service.FacesRestApiClient;
import feign.FeignException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FacesRestApiClientTest {

    @Mock
    private FacesFeignClient feignClient;
    @InjectMocks
    private FacesRestApiClient restApiClient;

    static Stream<Arguments> verifyFindFacesExceptions() {
        return Stream.of(
                Arguments.of(FeignException.BadRequest.class, NoFacesFoundException.class),
                Arguments.of(FeignException.class, FacesServiceException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("verifyFindFacesExceptions")
    void testFindFacesWithException(Class<? extends Exception> caughtClass, Class<? extends Exception> thrownClass) {
        // given
        MultipartFile photo = mock(MultipartFile.class);
        Integer faceLimit = 1;
        Double thresholdC = 1.0;
        String facePlugins = "plugins";
        when(feignClient.findFaces(photo, faceLimit, thresholdC, facePlugins, true)).thenThrow(caughtClass);

        // when
        Executable action = () -> restApiClient.findFaces(photo, faceLimit, thresholdC, facePlugins, true);

        // then
        assertThrows(thrownClass, action);
    }

    @Test
    void testFindFaces() {
        // given
        FindFacesResponse expected = mock(FindFacesResponse.class);
        MultipartFile photo = mock(MultipartFile.class);
        Integer faceLimit = 1;
        Double thresholdC = 1.0;
        String facePlugins = "plugins";
        when(feignClient.findFaces(photo, faceLimit, thresholdC, facePlugins, true)).thenReturn(expected);

        // when
        FindFacesResponse actual = restApiClient.findFaces(photo, faceLimit, thresholdC, facePlugins, true);

        // then
        assertThat(actual, is(expected));
    }

    static Stream<Arguments> verifyFindFacesWithCalculator() {
        return Stream.of(
                Arguments.of(null, CALCULATOR_PLUGIN),
                Arguments.of("", CALCULATOR_PLUGIN),
                Arguments.of("age,gender,pose", CALCULATOR_PLUGIN + ",age,gender,pose"),
                Arguments.of(CALCULATOR_PLUGIN + ",age,gender,pose", CALCULATOR_PLUGIN + ",age,gender,pose")
        );
    }

    @ParameterizedTest
    @MethodSource("verifyFindFacesWithCalculator")
    void testFindFacesWithCalculator(String inPlugins, String outPlugins) {
        // given
        FindFacesResponse expected = mock(FindFacesResponse.class);
        MultipartFile photo = mock(MultipartFile.class);
        Integer faceLimit = 1;
        Double thresholdC = 1.0;
        when(feignClient.findFaces(photo, faceLimit, thresholdC, outPlugins, true)).thenReturn(expected);

        // when
        FindFacesResponse actual = restApiClient.findFacesWithCalculator(photo, faceLimit, thresholdC, inPlugins, true);

        // then
        assertThat(actual, is(expected));
    }

    @ParameterizedTest
    @MethodSource("verifyFindFacesExceptions")
    void testFindFacesWithCalculatorWithException(Class<? extends Exception> caughtClass, Class<? extends Exception> thrownClass) {
        // given
        MultipartFile photo = mock(MultipartFile.class);
        Integer faceLimit = 1;
        Double thresholdC = 1.0;
        when(feignClient.findFaces(photo, faceLimit, thresholdC, CALCULATOR_PLUGIN, true)).thenThrow(caughtClass);

        // when
        Executable action = () -> restApiClient.findFacesWithCalculator(photo, faceLimit, thresholdC, null, true);

        // then
        assertThrows(thrownClass, action);
    }

    @Test
    void testGetStatusWithException() {
        // given
        when(feignClient.getStatus()).thenThrow(FeignException.class);

        // when
        Executable action = () -> restApiClient.getStatus();

        // then
        assertThrows(FacesServiceException.class, action);
    }

    @Test
    void testGetStatus() {
        // given
        FacesStatusResponse expected = mock(FacesStatusResponse.class);
        when(feignClient.getStatus()).thenReturn(expected);

        // when
        FacesStatusResponse actual = restApiClient.getStatus();

        // then
        assertThat(actual, is(expected));
    }
}