package com.exadel.frs.core.trainservice.mapper;

import com.exadel.frs.core.trainservice.cache.SubjectMeta;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EmbeddingMapper {

    FaceResponseDto toResponseDto(SubjectMeta meta);

    Collection<FaceResponseDto> toResponseDto(Collection<SubjectMeta> metas);

    default String map(UUID value) {
        return Optional.ofNullable(value).map(Object::toString).orElse(null);
    }
}
