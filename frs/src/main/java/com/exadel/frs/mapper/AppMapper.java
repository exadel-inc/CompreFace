package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppDto;
import com.exadel.frs.dto.ModelAccess;
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
public interface AppMapper extends ModelAccessTypeMapper {

    AppMapper INSTANCE = Mappers.getMapper(AppMapper.class);

    @Mapping(source = "modelAccess", target = "appModelList")
    App toEntity(AppDto appDto);

    @Mapping(source = "modelAccess", target = "appModelList", qualifiedByName = "toAppModel")
    App toEntity(AppDto appDto, @Context Long appId);

    @Named("toAppModel")
    default AppModel toAppModel(ModelAccess modelAccess, @Context Long appId) {
        if (modelAccess == null) {
            return null;
        }

        AppModel appModel = new AppModel();

        App app = new App();
        app.setId(appId);
        appModel.setApp(app);

        Model model = new Model();
        model.setId(modelAccess.getModelId());
        appModel.setModel(model);

        AppModelId appModelId = new AppModelId();
        appModelId.setAppId(appId);
        appModelId.setModelId(modelAccess.getModelId());
        appModel.setId(appModelId);

        appModel.setAccessType(toModelAccessType(modelAccess.getAccessType()));

        return appModel;
    }

    @Mapping(source = "appModelList", target = "modelAccess")
    AppDto toDto(App app);

    @Mapping(source = "id.modelId", target = "modelId")
    ModelAccess toModelAccess(AppModel appModel);

}
