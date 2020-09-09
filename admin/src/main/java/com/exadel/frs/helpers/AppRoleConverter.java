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

import com.exadel.frs.enums.AppRole;
import com.exadel.frs.exception.IncorrectAppRoleException;
import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class AppRoleConverter implements AttributeConverter<AppRole, String> {

    @Override
    public String convertToDatabaseColumn(AppRole appRole) {
        return appRole == null ? null : appRole.getCode();
    }

    @Override
    public AppRole convertToEntityAttribute(String code) {
        return code == null
                ? null
                : Stream.of(AppRole.values())
                        .filter(appRole -> appRole.getCode().equals(code))
                        .findFirst()
                        .orElseThrow(() -> new IncorrectAppRoleException(code));
    }
}