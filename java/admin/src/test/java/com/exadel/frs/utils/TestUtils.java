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

package com.exadel.frs.utils;

import static com.exadel.frs.handler.ExceptionCode.UNDEFINED;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import com.exadel.frs.dto.ExceptionResponseDto;
import com.exadel.frs.entity.User;
import com.exadel.frs.exception.BasicException;
import javax.sql.DataSource;
import lombok.val;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

public class TestUtils {

    public static final long USER_ID = 3L;

    public static ExceptionResponseDto buildExceptionResponse(final BasicException ex) {
        return ExceptionResponseDto.builder()
                                   .code(ex.getExceptionCode().getCode())
                                   .message(ex.getMessage())
                                   .build();
    }

    public static ExceptionResponseDto buildUndefinedExceptionResponse(final Exception ex) {
        return ExceptionResponseDto.builder()
                                   .code(UNDEFINED.getCode())
                                   .message(ex.getMessage())
                                   .build();
    }

    public static User buildUser() {
        return User.builder()
                   .id(USER_ID)
                   .firstName(randomAlphabetic(10))
                   .lastName(randomAlphabetic(10))
                   .email(randomAlphabetic(10) + "@" + randomAlphabetic(5) + ".com")
                   .build();
    }

    public static void executeScript(final DataSource dataSource, final String path) {
        val script = new ClassPathResource(path);
        val populator = new ResourceDatabasePopulator();
        populator.addScript(script);

        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}