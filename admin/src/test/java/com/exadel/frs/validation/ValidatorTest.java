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

import com.exadel.frs.dto.ui.UserRoleUpdateDto;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.Validator;

@SpringBootTest
public class ValidatorTest {

    @Autowired
    private Validator validator;

    @Test
    public void testUserRoleUpdateDtoEmpty() {
        val actual = validator.validate(UserRoleUpdateDto.builder().build());

        Assertions.assertFalse(actual.isEmpty());
    }

    @Test
    public void testUserRoleUpdateDto() {
        val actual = validator.validate(UserRoleUpdateDto.builder()
                .role("test")
                .userId("test")
                .build());

        Assertions.assertTrue(actual.isEmpty());
    }


}
