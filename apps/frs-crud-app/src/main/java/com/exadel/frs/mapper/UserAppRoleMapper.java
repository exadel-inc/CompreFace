package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.UserRoleResponseDto;
import com.exadel.frs.entity.UserAppRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface UserAppRoleMapper {

    @Mapping(source = "user.guid", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    UserRoleResponseDto toUserRoleResponseDto(UserAppRole userAppRole);
    List<UserRoleResponseDto> toUserRoleResponseDto(List<UserAppRole> userAppRoles);

}
