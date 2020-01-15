package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.UserRoleResponseDto;
import com.exadel.frs.entity.UserOrganizationRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserOrgRoleMapper {

    @Mapping(source = "user.guid", target = "id")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "role", target = "accessLevel")
    UserRoleResponseDto toUserRoleResponseDto(UserOrganizationRole userAppRole);
    List<UserRoleResponseDto> toUserRoleResponseDto(List<UserOrganizationRole> userAppRoles);

}
