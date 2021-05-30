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
package com.exadel.frs.helpers;

import static com.exadel.frs.commonservice.enums.ModelType.DETECTION;
import static com.exadel.frs.commonservice.enums.ModelType.RECOGNITION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.exadel.frs.commonservice.exception.IncorrectModelTypeException;
import com.exadel.frs.commonservice.enums.ModelType;
import java.util.stream.Stream;

import com.exadel.frs.commonservice.helpers.ModelTypeConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ModelTypeConverterTest {

    private final ModelTypeConverter converter = new ModelTypeConverter();

    @BeforeEach
    void setUp() {

    }

    @Test
    void testConvertToDatabaseColumnNullEnumeration() {
        // when
        String code = converter.convertToDatabaseColumn(null);

        // then
        assertNull(code);
    }

    static Stream<Arguments> verifyCorrectEnumMapping() {
        return Stream.of(
                Arguments.of(DETECTION, "D"),
                Arguments.of(RECOGNITION, "R")
        );
    }

    @ParameterizedTest
    @MethodSource("verifyCorrectEnumMapping")
    void testConvertToDatabaseColumn(ModelType type, String code) {
        // when
        String actualCode = converter.convertToDatabaseColumn(type);

        // then
        assertThat(actualCode, is(code));
    }

    static Stream<Arguments> verifyConvertToEntityAttributeWithNotCorrectInput() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of("NOT_EXISTING")
        );
    }

    @ParameterizedTest
    @MethodSource("verifyConvertToEntityAttributeWithNotCorrectInput")
    void testConvertToEntityAttributeWithNotCorrectInput(String code) {
        // when
        Executable action = () -> converter.convertToEntityAttribute(code);

        // then
        assertThrows(IncorrectModelTypeException.class, action);
    }

    @ParameterizedTest
    @MethodSource("verifyCorrectEnumMapping")
    void testConvertToEntityAttribute(ModelType type, String code) {
        // when
        ModelType actualType = converter.convertToEntityAttribute(code);

        // then
        assertThat(actualType, is(type));
    }
}