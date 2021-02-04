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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EnumValidatorTest {

    private enum TestEnum {
        VAL1, VAL2
    }

    @ValidEnum(targetClassType = TestEnum.class)
    private final EnumValidator validator = new EnumValidator();

    @BeforeEach
    void setUp() {
        ValidEnum ann = this.getClass().getDeclaredFields()[0].getAnnotation(ValidEnum.class);
        validator.initialize(ann);
    }

    static Stream<Arguments> verifyIsValidWithNotCorrectValue() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(""),
                Arguments.of("NOT_EXISTING")
        );
    }

    @ParameterizedTest
    @MethodSource("verifyIsValidWithNotCorrectValue")
    void testIsValidWithNotCorrectValue(String value) {
        // given & when
        boolean valid = validator.isValid(value, null);

        // then
        assertFalse(valid);
    }

    static Stream<Arguments> verifyIsValidWithCorrectValue() {
        return Stream.of(
                Arguments.of("VAL1"),
                Arguments.of("VAL2")
        );
    }

    @ParameterizedTest
    @MethodSource("verifyIsValidWithCorrectValue")
    void testIsValidWithCorrectValue(String value) {
        // given & when
        boolean valid = validator.isValid(value, null);

        // then
        assertTrue(valid);
    }
}