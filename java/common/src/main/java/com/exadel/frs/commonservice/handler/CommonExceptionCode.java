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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@AllArgsConstructor
@Getter
public enum CommonExceptionCode implements HttpExceptionCode {

    EMPTY_REQUIRED_FIELD(5, BAD_REQUEST),
    INCORRECT_ACCESS_TYPE(6, INTERNAL_SERVER_ERROR),
    INCORRECT_APP_ROLE(7, INTERNAL_SERVER_ERROR),
    INCORRECT_GLOBAL_ROLE(8, INTERNAL_SERVER_ERROR),

    MODEL_NOT_FOUND(10, NOT_FOUND),
    IMAGE_NOT_FOUND(11, NOT_FOUND),

    INCORRECT_MODEL_TYPE(12, INTERNAL_SERVER_ERROR),
    INCORRECT_PREDICTION_COUNT(12, BAD_REQUEST),
    ILLEGAL_REPLACER(13, BAD_REQUEST),

    MISSING_REQUEST_HEADER(20, BAD_REQUEST),
    UNAVAILABLE_FILE_EXTENSION(21, BAD_REQUEST),
    VALIDATION_CONSTRAINT_VIOLATION(26, BAD_REQUEST),
    ALREADY_IN_PROGRESS(27, BAD_REQUEST),
    NO_FACES_FOUND(28, BAD_REQUEST),

    TOO_MANY_FACES(31, BAD_REQUEST),
    WRITING_NOT_ALLOWED(32, METHOD_NOT_ALLOWED),
    MISSING_REQUEST_PARAMETER(33, BAD_REQUEST),
    MISSING_REQUEST_PART(34, BAD_REQUEST),
    MISSING_PATH_VARIABLE(35, BAD_REQUEST),
    INCORRECT_ARGUMENT(36, BAD_REQUEST),
    WRONG_EMBEDDING_COUNT(37, BAD_REQUEST),

    FACES_SERVICE_EXCEPTION(41, INTERNAL_SERVER_ERROR),
    SUBJECT_NOT_FOUND(42, NOT_FOUND),
    SUBJECT_ALREADY_EXISTS(43, BAD_REQUEST),
    EMBEDDING_NOT_FOUND(44, NOT_FOUND),

    UNDEFINED(0, BAD_REQUEST);

    private final Integer code;
    private final HttpStatus httpStatus;
}