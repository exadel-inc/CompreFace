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
public enum CrudExceptionCode implements HttpExceptionCode {

    APP_ACCESS_DENIED(1, FORBIDDEN),
    APP_NOT_FOUND(2, NOT_FOUND),
    MODEL_DOES_NOT_BELONG_TO_APP(3, NOT_FOUND),
    EMAIL_ALREADY_REGISTERED(4, BAD_REQUEST),

    INSUFFICIENT_PRIVILEGES(9, BAD_REQUEST),
    MODEL_NOT_FOUND(10, NOT_FOUND),
    INCORRECT_MODEL_TYPE(11, INTERNAL_SERVER_ERROR),

    SELF_ROLE_CHANGE(14, BAD_REQUEST),
    USER_DOES_NOT_EXIST(16, NOT_FOUND),
    NAME_IS_NOT_UNIQUE(17, BAD_REQUEST),
    INCORRECT_USER_PASSWORD(18, BAD_REQUEST),
    USER_ALREADY_HAS_ACCESS_TO_APP(19, BAD_REQUEST),

    DEMO_MODEL_NOT_AVAILABLE(20, FORBIDDEN),
    UNAVAILABLE_FILE_EXTENSION(21, BAD_REQUEST),
    INVALID_EMAIL_FORMAT(22, BAD_REQUEST),
    MODEL_SHARE_REQUEST_NOT_FOUND(23, NOT_FOUND),
    UNREACHABLE_EMAIL_ADDRESS(24, BAD_REQUEST),

    USER_REGISTRATION_TOKEN_EXPIRED(25, FORBIDDEN),
    VALIDATION_CONSTRAINT_VIOLATION(26, BAD_REQUEST),

    APPERY_SERVICE_EXCEPTION(27, INTERNAL_SERVER_ERROR),
    INCORRECT_STATISTICS_ROLE(28, BAD_REQUEST),
    MAIL_SERVER_EXCEPTION(29, INTERNAL_SERVER_ERROR),
    INVALID_RESET_PASSWORD_TOKEN(30, BAD_REQUEST),

    UNDEFINED(0, BAD_REQUEST);

    private final Integer code;
    private final HttpStatus httpStatus;
}
