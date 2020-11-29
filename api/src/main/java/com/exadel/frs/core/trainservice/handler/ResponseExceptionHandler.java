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

package com.exadel.frs.core.trainservice.handler;

import static com.exadel.frs.core.trainservice.handler.ExceptionCode.MISSING_REQUEST_HEADER;
import static com.exadel.frs.core.trainservice.handler.ExceptionCode.MISSING_REQUEST_PARAMETER;
import static com.exadel.frs.core.trainservice.handler.ExceptionCode.UNDEFINED;
import static com.exadel.frs.core.trainservice.handler.ExceptionCode.VALIDATION_CONSTRAINT_VIOLATION;
import com.exadel.frs.core.trainservice.dto.ExceptionResponseDto;
import com.exadel.frs.core.trainservice.exception.BasicException;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {MissingRequestHeaderException.class})
    public ResponseEntity<ExceptionResponseDto> handleMissingRequestHeader(final MissingRequestHeaderException e) {
        return handleMissingRequestHeader(e.getHeaderName());
    }

    public ResponseEntity<ExceptionResponseDto> handleMissingRequestHeader(final String headerName) {
        log.error("Missing header exception: " + headerName);

        return ResponseEntity.status(MISSING_REQUEST_HEADER.getHttpStatus())
                             .body(buildBody(MISSING_REQUEST_HEADER.getCode(), "Missing header: " + headerName));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ExceptionResponseDto> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint violation exception occurred", e);

        val sb = new StringBuilder();
        for (val violation : e.getConstraintViolations()) {
            sb.append(e.getMessage());
            sb.append("; ");
        }

        return ResponseEntity.status(VALIDATION_CONSTRAINT_VIOLATION.getHttpStatus())
                             .body(buildBody(VALIDATION_CONSTRAINT_VIOLATION.getCode(), sb.toString()));
    }

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<ExceptionResponseDto> handleDefinedExceptions(final BasicException ex) {
        log.error("Defined exception occurred", ex);

        return ResponseEntity
                .status(ex.getExceptionCode().getHttpStatus())
                .body(buildBody(ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUndefinedException(final Exception ex) {
        log.error("Multipart exception occurred", ex);

        return ResponseEntity
                .status(UNDEFINED.getHttpStatus())
                .body(buildBody(UNDEFINED.getCode(), "No message available"));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Missing parameter exception: " + ex.getParameterName());

        return ResponseEntity
                .status(MISSING_REQUEST_PARAMETER.getHttpStatus())
                .body(buildBody(MISSING_REQUEST_PARAMETER.getCode(), ex.getMessage()));
    }

    private ExceptionResponseDto buildBody(BasicException e) {
        return ExceptionResponseDto.builder()
                                   .code(e.getExceptionCode().getCode())
                                   .message(e.getMessage())
                                   .build();
    }

    private ExceptionResponseDto buildBody(Integer code, String message) {
        return ExceptionResponseDto.builder()
                                   .code(code)
                                   .message(message)
                                   .build();
    }
}