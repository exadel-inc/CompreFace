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

package com.exadel.frs.exception;

import com.exadel.frs.commonservice.exception.BasicException;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.USER_ALREADY_HAS_ACCESS_TO_APP;
import static java.lang.String.format;

public class UserAlreadyHasAccessToAppException extends BasicException {

    private static final String MESSAGE = "User %s already has access to application %s";

    public UserAlreadyHasAccessToAppException(String user, String appName) {
        super(USER_ALREADY_HAS_ACCESS_TO_APP, format(MESSAGE, user, appName));
    }
}
