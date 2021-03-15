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

package com.exadel.frs.handler;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.UNDEFINED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import com.exadel.frs.EmbeddedPostgreSQLTest;
import com.exadel.frs.commonservice.dto.ExceptionResponseDto;
import com.exadel.frs.commonservice.exception.BasicException;
import com.exadel.frs.commonservice.handler.ResponseExceptionHandler;
import com.exadel.frs.exception.AccessDeniedException;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

@SpringBootTest
class ResponseExceptionHandlerTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private ResponseExceptionHandler exceptionHandler;

    @ParameterizedTest
    @MethodSource("definedExceptions")
    void handleDefinedExceptions(final BasicException ex) {
        ResponseEntity<ExceptionResponseDto> response = exceptionHandler.handleDefinedExceptions(ex);

        val expectedResponseDto = ExceptionResponseDto.builder()
                                                      .code(ex.getExceptionCode().getCode())
                                                      .message(ex.getMessage())
                                                      .build();

        assertThat(response.getBody(), is(equalTo(expectedResponseDto)));
    }

    @ParameterizedTest
    @MethodSource("undefinedExceptions")
    void handleUndefinedExceptions(final Exception ex) {
        ResponseEntity<ExceptionResponseDto> response = exceptionHandler.handleUndefinedExceptions(ex);

        val expectedResponseDto = ExceptionResponseDto.builder()
                                                      .code(UNDEFINED.getCode())
                                                      .message("Something went wrong, please try again")
                                                      .build();

        assertThat(response.getBody(), is(equalTo(expectedResponseDto)));
    }

    private static Stream<Arguments> definedExceptions() {
        return Stream.of(
                Arguments.of(new AccessDeniedException())
        );
    }

    private static Stream<Arguments> undefinedExceptions() {
        return Stream.of(Arguments.of(new NullPointerException()));
    }
}