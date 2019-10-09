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

@Mapper(componentModel = "spring")
public interface AppMapper extends ModelAccessTypeMapper {

    @Mapping(source = "modelAccess", target = "appModelList")
    @Mapping(source = "ownerId", target = "owner.id")
    App toEntity(AppDto appDto);

    @Mapping(source = "modelAccess", target = "appModelList", qualifiedByName = "toAppModel")
    @Mapping(source = "ownerId", target = "owner.id")
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
    @Mapping(source = "owner.id", target = "ownerId")
    AppDto toDto(App app);

    @Mapping(source = "id.modelId", target = "modelId")
    ModelAccess toModelAccess(AppModel appModel);

}
