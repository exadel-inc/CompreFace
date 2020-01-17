package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.ModelResponseDto;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.Model;
import com.exadel.frs.enums.AppModelAccess;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper
public interface MlModelMapper {

    @Mapping(source = "guid", target = "id")
    @Mapping(source = "model", target = "accessLevel", qualifiedByName = "getAccessLevel")
    ModelResponseDto toResponseDto(Model model, @Context String appGuid);
    List<ModelResponseDto> toResponseDto(List<Model> model, @Context String appGuid);

    @Named("getAccessLevel")
    default AppModelAccess getAccessLevel(Model model, @Context String appGuid) {
        if (model.getApp().getGuid().equals(appGuid)) {
            return AppModelAccess.OWNER;
        }
        return model.getAppModel(appGuid)
                .map(AppModel::getAccessType)
                .orElse(null);
    }

}
