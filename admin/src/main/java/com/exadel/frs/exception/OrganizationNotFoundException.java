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

import static com.exadel.frs.handler.ExceptionCode.ORGANIZATION_NOT_FOUND;
import static java.lang.String.format;

public class OrganizationNotFoundException extends BasicException {

    private static final String MESSAGE = "Organization %s not found";

    public OrganizationNotFoundException(final Object identifier) {
        super(ORGANIZATION_NOT_FOUND, format(MESSAGE, identifier));
    }

}
