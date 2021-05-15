package com.exadel.frs.core.trainservice.mapper;

import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.exadel.frs.core.trainservice.dto.EmbeddingDto;
import org.mapstruct.Mapper;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface EmbeddingMapper {

    EmbeddingDto toResponseDto(EmbeddingProjection projection);

    Collection<EmbeddingDto> toResponseDto(Collection<EmbeddingProjection> projections);

    default String map(UUID value) {
        return Optional.ofNullable(value).map(Object::toString).orElse(null);
    }
}
