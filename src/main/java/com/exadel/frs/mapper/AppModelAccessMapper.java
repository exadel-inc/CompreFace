package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppModelDto;
import com.exadel.frs.entity.AppModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppModelAccessMapper {

    @Mapping(source = "app.id", target = "appId")
    @Mapping(source = "model.id", target = "modelId")
    AppModelDto toDto(AppModel appModel);

    @Mapping(source = "modelId", target = "id.modelId")
    @Mapping(source = "modelId", target = "model.id")
    @Mapping(source = "appId", target = "id.appId")
    @Mapping(source = "appId", target = "app.id")
    AppModel toEntity(AppModelDto appModelDto);

}
