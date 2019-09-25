package com.exadel.frs.mapper;

import com.exadel.frs.helpers.ModelAccessType;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.stream.Stream;

@Mapper
public interface ModelAccessTypeMapper {

    ModelAccessTypeMapper INSTANCE = Mappers.getMapper(ModelAccessTypeMapper.class);

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
