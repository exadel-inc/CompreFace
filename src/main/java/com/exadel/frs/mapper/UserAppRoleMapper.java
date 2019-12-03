package com.exadel.frs.mapper;

import com.exadel.frs.dto.UserAppRoleDto;
import com.exadel.frs.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserAppRoleMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "app.id", target = "appId")
    UserAppRoleDto toDto(UserAppRole userAppRoleDto);

    @Mapping(source = "userId", target = "id.userId")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "appId", target = "id.appId")
    @Mapping(source = "appId", target = "app.id")
    UserAppRole toEntity(UserAppRoleDto userAppRoleDto);

}
