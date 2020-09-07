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

import static com.exadel.frs.handler.ExceptionCode.UNDEFINED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.exception.BasicException;
import com.exadel.frs.exception.ConstraintViolationException;
import com.exadel.frs.exception.EmptyRequiredFieldException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler {

    @ExceptionHandler(BasicException.class)
    public ResponseEntity<ExceptionResponseDto> handleDefinedExceptions(final BasicException ex) {
        log.error("Defined exception occurred", ex);

        return ResponseEntity
                .status(ex.getExceptionCode().getHttpStatus())
                .body(buildBody(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponseDto> handleInvalidMethodArguments(final MethodArgumentNotValidException ex) {
        log.error("Field validation failed", ex);

        val fieldError = ex.getBindingResult().getFieldErrors().get(0);

        return buildBody(fieldError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleUndefinedExceptions(final Exception ex) {
        log.error("Undefined exception occurred", ex);

        return ResponseEntity.status(UNDEFINED.getHttpStatus()).body(buildBody());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionResponseDto> handle404(final Exception ex) {
        log.error("404 error has occurred", ex);

        val body = ExceptionResponseDto.builder()
                                       .code(UNDEFINED.getCode())
                                       .message(ex.getMessage() != null ? ex.getMessage() : "No message available")
                                       .build();

        return ResponseEntity.status(NOT_FOUND).body(body);
    }

    private BasicException getException(final FieldError fieldError) {
        BasicException basicException;

        switch (fieldError.getCode()) {
            case "NotBlank":
            case "ValidEnum":
                basicException = new ConstraintViolationException(fieldError.getDefaultMessage());
                break;
            case "NotNull":
            case "NotEmpty":
                basicException = new EmptyRequiredFieldException(fieldError.getField());
                break;
            case "Size":
                basicException = new ConstraintViolationException(fieldError.getField(), fieldError.getDefaultMessage());
                break;
            default:
                basicException = new BasicException(UNDEFINED, "");
        }

        return basicException;
    }

    private ResponseEntity<ExceptionResponseDto> buildBody(final FieldError fieldError) {
        val exception = getException(fieldError);

        return handleDefinedExceptions(exception);
    }

    private ExceptionResponseDto buildBody(final BasicException ex) {
        return ExceptionResponseDto.builder()
                                   .code(ex.getExceptionCode().getCode())
                                   .message(ex.getMessage())
                                   .build();
    }

    private ExceptionResponseDto buildBody() {
        return ExceptionResponseDto.builder()
                                   .code(UNDEFINED.getCode())
                                   .message("Something went wrong, please try again")
                                   .build();
    }
}
