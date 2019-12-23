package com.exadel.frs.mapper;

import com.exadel.frs.dto.AppRoleDto;
import com.exadel.frs.dto.UserRoleDto;
import com.exadel.frs.entity.UserAppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserAppRoleMapper {

    @Mapping(source = "user.id", target = "userId")
    UserRoleDto toUserRoleDto(UserAppRole userAppRole);

    @Mapping(source = "app.id", target = "appId")
    AppRoleDto toAppRoleDto(UserAppRole userAppRole);

    @Mapping(source = "userId", target = "id.userId")
    @Mapping(source = "userId", target = "user.id")
    UserAppRole toEntity(UserRoleDto userRoleDto);

    @Mapping(source = "appId", target = "id.appId")
    @Mapping(source = "appId", target = "app.id")
    UserAppRole toEntity(AppRoleDto appRoleDto);

}
