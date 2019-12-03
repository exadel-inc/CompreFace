package com.exadel.frs.mapper;

import com.exadel.frs.dto.UserOrganizationRoleDto;
import com.exadel.frs.entity.UserOrganizationRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserOrganizationRoleMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "organization.id", target = "organizationId")
    UserOrganizationRoleDto toDto(UserOrganizationRole userOrganizationRole);

    @Mapping(source = "userId", target = "id.userId")
    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "organizationId", target = "id.organizationId")
    @Mapping(source = "organizationId", target = "organization.id")
    UserOrganizationRole toEntity(UserOrganizationRoleDto userOrganizationRoleDto);

}
