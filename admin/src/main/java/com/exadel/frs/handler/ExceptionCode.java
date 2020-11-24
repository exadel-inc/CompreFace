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

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ExceptionCode {

    APP_ACCESS_DENIED(1, FORBIDDEN),
    APP_NOT_FOUND(2, NOT_FOUND),
    MODEL_DOES_NOT_BELONG_TO_APP(3, NOT_FOUND),
    EMAIL_ALREADY_REGISTERED(4, BAD_REQUEST),
    EMPTY_REQUIRED_FIELD(5, BAD_REQUEST),

    INCORRECT_ACCESS_TYPE(6, BAD_REQUEST),
    INCORRECT_APP_ROLE(7, BAD_REQUEST),
    INCORRECT_GLOBAL_ROLE(8, BAD_REQUEST),
    INSUFFICIENT_PRIVILEGES(9, BAD_REQUEST),
    MODEL_NOT_FOUND(10, NOT_FOUND),

    ILLEGAL_REPLACER(13, BAD_REQUEST),
    SELF_ROLE_CHANGE(14, BAD_REQUEST),
    USER_DOES_NOT_EXIST(16, NOT_FOUND),
    NAME_IS_NOT_UNIQUE(17, BAD_REQUEST),
    USER_ALREADY_HAS_ACCESS_TO_APP(19, BAD_REQUEST),

    DEMO_MODEL_NOT_AVAILABLE(20, FORBIDDEN),
    UNAVAILABLE_FILE_EXTENSION(21, BAD_REQUEST),
    INVALID_EMAIL_FORMAT(22, BAD_REQUEST),
    MODEL_SHARE_REQUEST_NOT_FOUND(23, NOT_FOUND),
    UNREACHABLE_EMAIL_ADDRESS(24, BAD_REQUEST),

    USER_REGISTRATION_TOKEN_EXPIRED(25, FORBIDDEN),
    VALIDATION_CONSTRAINT_VIOLATION(26, BAD_REQUEST),

    UNDEFINED(0, BAD_REQUEST);

    private final Integer code;
    private final HttpStatus httpStatus;
}