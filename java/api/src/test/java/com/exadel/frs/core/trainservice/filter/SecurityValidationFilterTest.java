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

package com.exadel.frs.core.trainservice.filter;

import static com.exadel.frs.commonservice.enums.ValidationResult.FORBIDDEN;
import static com.exadel.frs.commonservice.enums.ValidationResult.OK;
import static com.exadel.frs.core.trainservice.system.global.Constants.API_V1;
import static com.exadel.frs.core.trainservice.system.global.Constants.RECOGNIZE;
import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.Collections.emptyEnumeration;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import com.exadel.frs.commonservice.enums.ModelType;
import com.exadel.frs.commonservice.exception.BadFormatModelKeyException;
import com.exadel.frs.commonservice.exception.ModelNotFoundException;
import com.exadel.frs.commonservice.handler.ResponseExceptionHandler;
import com.exadel.frs.core.trainservice.cache.ModelStatisticCacheProvider;
import com.exadel.frs.core.trainservice.dto.ModelValidationResult;
import com.exadel.frs.core.trainservice.service.ModelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

class SecurityValidationFilterTest {

    private static final String SHORT_API_KEY = "9892f9e2-1844-46f3-a710-72e";
    private static final String VALID_API_KEY = "11f4cb4b-ea5a-45d4-8da7-863fea07c40a";
    private static final String NOT_VALID_API_KEY = "11f4cb4bea5a45d48da7863fea07c40a11111";

    @Mock
    private ResponseExceptionHandler exceptionHandler;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private ModelService modelService;

    @Mock
    private ModelStatisticCacheProvider modelStatisticCacheProvider;

    @InjectMocks
    private SecurityValidationFilter securityValidationFilter;

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);

        httpServletRequest = mock(HttpServletRequest.class);
        httpServletResponse = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(httpServletRequest.getRequestURI()).thenReturn(API_V1 + RECOGNIZE);
    }

    @Test
    void testDoFilterWithShortApiKey() throws IOException, ServletException {
        when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(singletonList(X_FRS_API_KEY_HEADER)));
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(enumeration(singletonList(SHORT_API_KEY)));
        when(exceptionHandler.handleDefinedExceptions(any())).thenCallRealMethod();

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(
                exceptionHandler.handleDefinedExceptions(new BadFormatModelKeyException())
                                .getStatusCode()
                                .value()
        );
    }

    @Test
    void testDoFilterWithoutApiKey() throws IOException, ServletException {
        when(httpServletRequest.getHeaderNames()).thenReturn(emptyEnumeration());
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(emptyEnumeration());
        when(exceptionHandler.handleMissingRequestHeader(anyString())).thenCallRealMethod();

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(
                exceptionHandler.handleMissingRequestHeader(X_FRS_API_KEY_HEADER)
                                .getStatusCode()
                                .value()
        );
    }

    @Test
    void testDoFilterWithValidApiKey() throws IOException, ServletException {
        var validationResult = new ModelValidationResult(1L, OK);

        when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(singletonList(X_FRS_API_KEY_HEADER)));
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(enumeration(singletonList(VALID_API_KEY)));
        when(modelService.validateModelKey(anyString(), any(ModelType.class))).thenReturn(validationResult);

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    void testDoFilterWithNonExistentApiKey() throws IOException, ServletException {
        var validationResult = new ModelValidationResult(1L, FORBIDDEN);

        when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(singletonList(X_FRS_API_KEY_HEADER)));
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(enumeration(singletonList(VALID_API_KEY)));
        when(modelService.validateModelKey(anyString(), eq(ModelType.RECOGNITION))).thenReturn(validationResult);
        when(exceptionHandler.handleDefinedExceptions(any())).thenCallRealMethod();

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(
                exceptionHandler.handleDefinedExceptions(new ModelNotFoundException(
                                        VALID_API_KEY,
                                        StringUtils.capitalize(ModelType.RECOGNITION.name().toLowerCase())
                                ))
                                .getStatusCode()
                                .value()
        );
    }

    @Test
    void testDoFilterWithNotValidApiKey() throws IOException, ServletException {
        when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(singletonList(X_FRS_API_KEY_HEADER)));
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(enumeration(singletonList(NOT_VALID_API_KEY)));
        when(exceptionHandler.handleDefinedExceptions(any())).thenCallRealMethod();

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(
                exceptionHandler.handleDefinedExceptions(new BadFormatModelKeyException())
                                .getStatusCode()
                                .value()
        );
        verify(httpServletResponse).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
