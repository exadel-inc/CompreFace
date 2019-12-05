package com.exadel.frs.mapper;

import com.exadel.frs.dto.OrganizationRoleDto;
import com.exadel.frs.dto.UserRoleDto;
import com.exadel.frs.entity.UserOrganizationRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserOrganizationRoleMapper {

    @Mapping(source = "user.id", target = "userId")
    UserRoleDto toUserRoleDto(UserOrganizationRole userOrganizationRole);

    @Mapping(source = "organization.id", target = "organizationId")
    OrganizationRoleDto toOrganizationRoleDto(UserOrganizationRole userOrganizationRole);

    @Mapping(source = "userId", target = "id.userId")
    @Mapping(source = "userId", target = "user.id")
    UserOrganizationRole toEntity(UserRoleDto userRoleDto);

    @Mapping(source = "organizationId", target = "id.organizationId")
    @Mapping(source = "organizationId", target = "organization.id")
    UserOrganizationRole toEntity(OrganizationRoleDto organizationRoleDto);

}
