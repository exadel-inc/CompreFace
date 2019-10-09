package com.exadel.frs.mapper;

import com.exadel.frs.helpers.ModelAccessType;
import org.mapstruct.Mapper;

import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface ModelAccessTypeMapper {

    default ModelAccessType toModelAccessType(String code) {
        return code == null ? null : Stream.of(ModelAccessType.values())
                .filter(accessType -> accessType.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Access type " + code + " does not exists"));
    }

    default String fromModelAccessType(ModelAccessType modelAccessType) {
        return modelAccessType == null ? null : modelAccessType.getCode();
    }

}
