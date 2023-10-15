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

package com.exadel.frs.commonservice.handler;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.MISSING_REQUEST_HEADER;
import static com.exadel.frs.commonservice.handler.CommonExceptionCode.UNDEFINED;
import static com.exadel.frs.commonservice.handler.CommonExceptionCode.VALIDATION_CONSTRAINT_VIOLATION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import com.exadel.frs.commonservice.dto.ExceptionResponseDto;
import com.exadel.frs.commonservice.exception.BasicException;
import com.exadel.frs.commonservice.exception.ConstraintViolationException;
import com.exadel.frs.commonservice.exception.DemoNotAvailableException;
import com.exadel.frs.commonservice.exception.EmptyRequiredFieldException;
import com.exadel.frs.commonservice.exception.IncorrectPredictionCountException;
import com.exadel.frs.commonservice.exception.MissingPathVarException;
import com.exadel.frs.commonservice.exception.MissingRequestParamException;
import com.exadel.frs.commonservice.exception.MissingRequestPartException;
import com.exadel.frs.commonservice.exception.PatternMatchException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<ExceptionResponseDto> handleDefinedExceptions(final BasicException ex) {
        switch (ex.getLogLevel()) {
            case ERROR -> log.error("Defined exception occurred", ex);
            case DEBUG -> log.debug("Defined exception occurred", ex);
        }

        return ResponseEntity
                .status(ex.getExceptionCode().getHttpStatus())
                .body(buildBody(ex));
    }

    @ExceptionHandler(value = {MissingRequestHeaderException.class})
    public ResponseEntity<ExceptionResponseDto> handleMissingRequestHeader(final MissingRequestHeaderException e) {
        return handleMissingRequestHeader(e.getHeaderName());
    }

    public ResponseEntity<ExceptionResponseDto> handleMissingRequestHeader(final String headerName) {
        log.error("Missing header exception: " + headerName);

        return ResponseEntity
                .status(MISSING_REQUEST_HEADER.getHttpStatus())
                .body(buildBody(MISSING_REQUEST_HEADER.getCode(), "Missing header: " + headerName));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("Field validation failed", ex);

        FieldError fieldError = ex.getBindingResult().getFieldErrors().get(0);
        BasicException basicException = getException(fieldError);

        return ResponseEntity
                .status(status)
                .body(buildBody(basicException));
    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    ResponseEntity<ExceptionResponseDto> handleConstraintViolationException(javax.validation.ConstraintViolationException e) {
        log.error("Constraint violation exception occurred", e);

        val sb = new StringBuilder();
        for (val violation : e.getConstraintViolations()) {
            sb.append(violation.getMessage());
            sb.append("; ");
        }

        return ResponseEntity
                .status(VALIDATION_CONSTRAINT_VIOLATION.getHttpStatus())
                .body(buildBody(VALIDATION_CONSTRAINT_VIOLATION.getCode(), sb.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUndefinedExceptions(final Exception ex) {
        log.error("Undefined exception occurred", ex);

        return ResponseEntity
                .status(UNDEFINED.getHttpStatus())
                .body(buildBody());
    }

    @ExceptionHandler(DemoNotAvailableException.class)
    public ResponseEntity<ExceptionResponseDto> handleDemoNotAvailableException(final DemoNotAvailableException ex) {
        return ResponseEntity.status(NOT_FOUND)
                             .body(ExceptionResponseDto
                                     .builder()
                                     .code(NOT_FOUND.value())
                                     .message(DemoNotAvailableException.MESSAGE)
                                     .build()
                             );
    }

    @ExceptionHandler(IncorrectPredictionCountException.class)
    public ResponseEntity<ExceptionResponseDto> handleIncorrectPredictionCountException(final IncorrectPredictionCountException ex) {
        return ResponseEntity.status(NOT_FOUND)
                             .body(ExceptionResponseDto
                                     .builder()
                                     .code(BAD_REQUEST.value())
                                     .message(IncorrectPredictionCountException.INCORRECT_PREDICTION_MESSAGE)
                                     .build()
                             );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("404 error has occurred", ex);

        return ResponseEntity
                .status(NOT_FOUND)
                .body(ExceptionResponseDto
                        .builder()
                        .code(UNDEFINED.getCode())
                        .message(ex.getMessage() != null ? ex.getMessage() : "No message available")
                        .build()
                );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("400 bad request. Request part is missing: {}", ex.getRequestPartName());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildBody(new MissingRequestPartException(ex.getRequestPartName())));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("400 bad request. Request param is missing: {}", ex.getParameterName());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildBody(new MissingRequestParamException(ex.getParameterName())));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.error("400 bad request. Path variable is missing: {}", ex.getVariableName());

        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildBody(new MissingPathVarException(ex.getVariableName())));
    }

    private static BasicException getException(final FieldError fieldError) {
        BasicException basicException;

        final String code = fieldError.getCode();

        if (code == null) {
            return new BasicException(UNDEFINED, "");
        }

        basicException = switch (code) {
            case "NotBlank", "ValidEnum", "Size" -> new ConstraintViolationException(fieldError.getDefaultMessage());
            case "NotNull", "NotEmpty" -> new EmptyRequiredFieldException(fieldError.getField());
            case "Pattern" -> new PatternMatchException(fieldError.getDefaultMessage());
            default -> new BasicException(UNDEFINED, "");
        };

        return basicException;
    }

    private static ExceptionResponseDto buildBody(final BasicException ex) {
        return buildBody(
                ex.getExceptionCode().getCode(),
                ex.getMessage()
        );
    }

    private static ExceptionResponseDto buildBody() {
        return buildBody(
                UNDEFINED.getCode(),
                "Something went wrong, please try again"
        );
    }

    private static ExceptionResponseDto buildBody(Integer code, String message) {
        return ExceptionResponseDto
                .builder()
                .code(code)
                .message(message)
                .build();
    }
}
