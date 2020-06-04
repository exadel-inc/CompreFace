package com.exadel.frs.helpers;

import com.exadel.frs.enums.AppModelAccess;
import com.exadel.frs.exception.IncorrectAccessTypeException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = true)
public class ModelAccessTypeConverter implements AttributeConverter<AppModelAccess, String> {

    @Override
    public String convertToDatabaseColumn(AppModelAccess appModelAccess) {
        return appModelAccess == null ? null : appModelAccess.getCode();
    }

    @Override
    public AppModelAccess convertToEntityAttribute(String code) {
        return code == null ? null : Stream.of(AppModelAccess.values())
                .filter(accessType -> accessType.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IncorrectAccessTypeException(code));
    }

}
