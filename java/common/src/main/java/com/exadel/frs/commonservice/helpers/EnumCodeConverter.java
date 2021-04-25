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
package com.exadel.frs.commonservice.helpers;

import com.exadel.frs.commonservice.enums.EnumCode;
import com.exadel.frs.commonservice.exception.BasicException;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.AttributeConverter;
import java.util.stream.Stream;

public abstract class EnumCodeConverter<T extends Enum<T> & EnumCode> implements AttributeConverter<T, String> {

    @Override
    public String convertToDatabaseColumn(T enumeration) {
        return enumeration == null ? null : enumeration.getCode();
    }

    protected T convertToEntityAttribute(String code, T[] values, BasicException exception) {
        if (StringUtils.isBlank(code) || values == null) {
            throw exception;
        }

        return Stream.of(values)
                     .filter(value -> value.getCode().equals(code))
                     .findFirst()
                     .orElseThrow(() -> exception);
    }
}
