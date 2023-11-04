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

package com.exadel.frs.commonservice.sdk.faces.exception;


import com.exadel.frs.commonservice.exception.BasicException;

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.NO_FACES_FOUND;

public class NoFacesFoundException extends BasicException {

    private static final String MESSAGE = "No face is found in the given image";

    public NoFacesFoundException() {
        super(NO_FACES_FOUND, MESSAGE);
    }

    @Override
    public LogLevel getLogLevel() {
        return LogLevel.DEBUG;
    }
}
