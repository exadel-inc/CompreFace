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

import static com.exadel.frs.core.trainservice.system.global.Constants.X_FRS_API_KEY_HEADER;
import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import com.exadel.frs.core.trainservice.dto.RetrainResponse;
import com.exadel.frs.core.trainservice.exception.BadFormatModelKeyException;
import com.exadel.frs.core.trainservice.handler.ResponseExceptionHandler;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class SecurityValidationFilterTest {

    private static final String SHORT_API_KEY = "9892f9e2-1844-46f3-a710-72e";

    @Mock
    private ResponseExceptionHandler exceptionHandler;

    @InjectMocks
    private SecurityValidationFilter securityValidationFilter;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    public void testDoFilterWithShortApiKey() throws IOException, ServletException {
        val httpServletRequest = mock(HttpServletRequest.class);
        val httpServletResponse = mock(HttpServletResponse.class);
        val filterChain = mock(FilterChain.class);

        when(httpServletRequest.getHeaderNames()).thenReturn(enumeration(singletonList(X_FRS_API_KEY_HEADER)));
        when(httpServletRequest.getHeaders(X_FRS_API_KEY_HEADER)).thenReturn(enumeration(singletonList(SHORT_API_KEY)));
        when(httpServletResponse.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
        when(exceptionHandler.handleBadFormatModelKeyException(new BadFormatModelKeyException()))
                .thenReturn(ResponseEntity.status(BAD_REQUEST)
                        .body(new RetrainResponse(new BadFormatModelKeyException().getMessage())));

        securityValidationFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(exceptionHandler
                .handleBadFormatModelKeyException(new BadFormatModelKeyException())
                .getStatusCode()
                .value());
        verify(httpServletResponse).setContentType("application/json");
    }
}
