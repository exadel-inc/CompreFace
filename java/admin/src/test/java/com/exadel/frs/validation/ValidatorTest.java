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

package com.exadel.frs.validation;

import static org.assertj.core.api.Assertions.assertThat;
import com.exadel.frs.EmbeddedPostgreSQLTest;
import com.exadel.frs.dto.UserRoleUpdateDto;
import javax.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ValidatorTest extends EmbeddedPostgreSQLTest {

    @Autowired
    private Validator validator;

    @Test
    public void testUserRoleUpdateDtoEmpty() {
        val actual = validator.validate(UserRoleUpdateDto.builder().build());

        assertThat(actual).isNotEmpty();
    }

    @Test
    public void testUserRoleUpdateDto() {
        val actual = validator.validate(UserRoleUpdateDto.builder()
                                                         .role("USER")
                                                         .userId("test")
                                                         .build());

        assertThat(actual).isEmpty();
    }

    @Test
    public void cannotAcceptWrongRole() {
        val actual = validator.validate(UserRoleUpdateDto.builder()
                                                         .role("WRONG_VALUE")
                                                         .userId("test")
                                                         .build());

        assertThat(actual).isNotEmpty();
    }
}
