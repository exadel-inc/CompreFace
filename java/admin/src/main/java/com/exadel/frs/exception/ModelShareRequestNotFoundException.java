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

import java.util.UUID;

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.MODEL_SHARE_REQUEST_NOT_FOUND;
import static java.lang.String.format;

public class ModelShareRequestNotFoundException extends BasicException {

    public static final String MESSAGE = "Model share request %s not found. It might be expired.";

    public ModelShareRequestNotFoundException(final UUID requestId) {
        super(MODEL_SHARE_REQUEST_NOT_FOUND, format(MESSAGE, requestId.toString()));
    }
}