package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppAccess;
import com.exadel.frs.dto.ModelDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.AppModel;
import com.exadel.frs.entity.AppModelId;
import com.exadel.frs.entity.Model;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ModelMapper extends ModelAccessTypeMapper {

    ModelMapper INSTANCE = Mappers.getMapper(ModelMapper.class);

    @Mapping(source = "appAccess", target = "appModelList")
    Model toEntity(ModelDto modelDto);

    @Mapping(source = "appAccess", target = "appModelList", qualifiedByName = "toAppModel")
    Model toEntity(ModelDto modelDto, @Context Long modelId);

    default AppModel toAppModel(AppAccess appAccess) {
        return toAppModel(appAccess, null);
    }

    @Named("toAppModel")
    default AppModel toAppModel(AppAccess appAccess, @Context Long modelId) {
        if (appAccess == null) {
            return null;
        }

        AppModel appModel = new AppModel();

        App app = new App();
        app.setId(appAccess.getAppId());
        appModel.setApp(app);

        Model model = new Model();
        model.setId(modelId);
        appModel.setModel(model);

        AppModelId appModelId = new AppModelId();
        appModelId.setAppId(appAccess.getAppId());
        appModelId.setModelId(modelId);
        appModel.setId(appModelId);

        appModel.setAccessType(toModelAccessType(appAccess.getAccessType()));

        return appModel;
    }

    @Mapping(source = "appModelList", target = "appAccess")
    ModelDto toDto(Model model);

    @Mapping(source = "id.appId", target = "appId")
    AppAccess toAppAccess(AppModel appModel);

}
