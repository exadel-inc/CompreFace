package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppAccessDto;
import com.exadel.frs.dto.ModelAccessDto;
import com.exadel.frs.entity.AppModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AppModelAccessMapper {

    @Mapping(source = "app.id", target = "appId")
    AppAccessDto toAppAccessDto(AppModel appModel);

    @Mapping(source = "model.id", target = "modelId")
    ModelAccessDto toModelAccessDto(AppModel appModel);

    @Mapping(source = "appId", target = "id.appId")
    @Mapping(source = "appId", target = "app.id")
    AppModel toEntity(AppAccessDto appAccessDto);

    @Mapping(source = "modelId", target = "id.modelId")
    @Mapping(source = "modelId", target = "model.id")
    AppModel toEntity(ModelAccessDto modelAccessDto);

}
