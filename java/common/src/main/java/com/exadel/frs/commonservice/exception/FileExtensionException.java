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

import static com.exadel.frs.commonservice.handler.CommonExceptionCode.UNAVAILABLE_FILE_EXTENSION;
import static java.lang.String.format;

public class FileExtensionException extends BasicException {

    private static final String MESSAGE = "File [%s] has an unavailable extension";

    public FileExtensionException(final String fileName) {
        super(UNAVAILABLE_FILE_EXTENSION, format(MESSAGE, fileName));
    }
}