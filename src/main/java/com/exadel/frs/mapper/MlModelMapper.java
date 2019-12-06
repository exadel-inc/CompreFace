package com.exadel.frs.mapper;

import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.entity.Model;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = AppModelAccessMapper.class)
public interface MlModelMapper {

    @Mapping(source = "appId", target = "app.id")
    Model toEntity(ModelDto modelDto);

    @Mapping(source = "app.id", target = "appId")
    ModelDto toDto(Model model);

    List<ModelDto> toDto(List<Model> model);

}
