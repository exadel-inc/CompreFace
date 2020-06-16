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

import static com.exadel.frs.validation.EmailValidator.isInvalid;
import static com.exadel.frs.validation.EmailValidator.isValid;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    private static List<String> VALID_EMAILS;
    private static List<String> INVALID_EMAILS;

    @BeforeAll
    public static void init() {
        VALID_EMAILS = List.of(
                "tester@exadel.com",
                "tester.100@exadel.com",
                "tester123@tester.com",
                "abc@3.com",
                "tester-9000@exadel.com",
                "tester-5500@tester.net",
                "tester.100@tester.com.au",
                "tester@gmail.com.com",
                "tester+100@gmail.com",
                "tester-100@exadel-test.com",
                "valijon@sobirov.uz"
        );

        INVALID_EMAILS = List.of(
                "tester",
                "tester@.com.my",
                "tester123@gmail.a",
                "tester123@.com",
                "tester123@.com.com",
                ".tester@tester.com",
                "tester()*@gmail.com",
                "tester@%*.com",
                "tester..2002@gmail.com",
                "tester.@gmail.com",
                "tester@tester@gmail.com",
                "tester@gmail.com.1a",
                "",
                "   "
        );
    }

    @Test
    void validEmailsAreValid() {
        assertThat(VALID_EMAILS).allSatisfy(
                email -> assertThat(isValid(email)).isTrue()
        );
    }

    @Test
    void invalidEmailsAreNotValid() {
        assertThat(INVALID_EMAILS).allSatisfy(
                email -> assertThat(isValid(email)).isFalse()
        );
    }

    @Test
    void invalidEmailsAreInvalid() {
        assertThat(INVALID_EMAILS).allSatisfy(
                email -> assertThat(isInvalid(email)).isTrue()
        );
    }

    @Test
    void validEmailsAreNotInvalid() {
        assertThat(VALID_EMAILS).allSatisfy(
                email -> assertThat(isInvalid(email)).isFalse()
        );
    }
}