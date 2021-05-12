package com.exadel.frs.core.trainservice.mapper;

import com.exadel.frs.core.trainservice.cache.SubjectMeta;
import com.exadel.frs.core.trainservice.dto.FaceResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface EmbeddingMapper {

    @Mapping(source = "imgId", target = "image_id")
    @Mapping(source = "subjectName", target = "subject")
    FaceResponseDto toResponseDto(SubjectMeta meta);

    Collection<FaceResponseDto> toResponseDto(Collection<SubjectMeta> metas);
}
