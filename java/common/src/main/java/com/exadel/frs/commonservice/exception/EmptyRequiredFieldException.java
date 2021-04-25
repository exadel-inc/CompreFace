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

package com.exadel.frs.commonservice.exception;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.EMPTY_REQUIRED_FIELD;
import static java.lang.String.format;

public class EmptyRequiredFieldException extends BasicException {

    public static final String MESSAGE = "Field %s cannot be empty";

    public EmptyRequiredFieldException(final String fieldName) {
        super(EMPTY_REQUIRED_FIELD, format(MESSAGE, fieldName));
    }
}