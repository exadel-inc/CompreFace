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

import static com.exadel.frs.commonservice.handler.CrudExceptionCode.UNREACHABLE_EMAIL_ADDRESS;

public class UnreachableEmailException extends BasicException {

    public static final String MESSAGE = "We couldn't send email to the given address(%s). Please make sure that it's correct!";

    public UnreachableEmailException(String email) {
        super(UNREACHABLE_EMAIL_ADDRESS, String.format(MESSAGE, email));
    }
}