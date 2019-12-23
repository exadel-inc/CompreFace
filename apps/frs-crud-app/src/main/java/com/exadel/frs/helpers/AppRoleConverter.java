package com.exadel.frs.helpers;

import com.exadel.frs.enums.AppRole;
import com.exadel.frs.exception.IncorrectAppRoleException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class AppRoleConverter implements AttributeConverter<AppRole, String> {

    @Override
    public String convertToDatabaseColumn(AppRole appRole) {
        return appRole == null ? null : appRole.getCode();
    }

    @Override
    public AppRole convertToEntityAttribute(String code) {
        return code == null ? null : Stream.of(AppRole.values())
                .filter(appRole -> appRole.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IncorrectAppRoleException(code));
    }

}
