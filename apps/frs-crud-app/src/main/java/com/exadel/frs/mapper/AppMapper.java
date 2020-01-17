package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.AppResponseDto;
import com.exadel.frs.entity.App;
import com.exadel.frs.entity.User;
import com.exadel.frs.entity.UserAppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {UserAppRoleMapper.class, MlModelMapper.class, UserMapper.class})
public interface AppMapper {

    @Mapping(source = "guid", target = "id")
    @Mapping(source = "app", target = "owner", qualifiedByName = "getOwner")
    AppResponseDto toResponseDto(App app);
    List<AppResponseDto> toResponseDto(List<App> apps);

    @Named("getOwner")
    default User getOwner(App app) {
        return app.getOwner()
                .map(UserAppRole::getUser)
                .orElse(null);
    }

}
