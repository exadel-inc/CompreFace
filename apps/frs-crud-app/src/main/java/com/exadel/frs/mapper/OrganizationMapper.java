package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.OrgResponseDto;
import com.exadel.frs.entity.Organization;
import com.exadel.frs.entity.UserOrganizationRole;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = UserOrgRoleMapper.class)
public interface OrganizationMapper {

    @Mapping(source = "guid", target = "id")
    @Mapping(source = "organization", target = "role", qualifiedByName = "getRole")
    OrgResponseDto toResponseDto(Organization organization, @Context Long userId);
    List<OrgResponseDto> toResponseDto(List<Organization> organizations, @Context Long userId);

    @Named("getRole")
    default String getRole(Organization organization, @Context Long userId) {
        return organization.getUserOrganizationRole(userId)
                .map(UserOrganizationRole::getRole)
                .map(Enum::name)
                .orElse(null);
    }

}
