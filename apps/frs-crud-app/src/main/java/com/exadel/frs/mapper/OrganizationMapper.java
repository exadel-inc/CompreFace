package com.exadel.frs.mapper;

import com.exadel.frs.dto.OrganizationDto;
import com.exadel.frs.entity.Organization;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserOrganizationRoleMapper.class)
public interface OrganizationMapper {

    Organization toEntity(OrganizationDto organizationDto);

    OrganizationDto toDto(Organization organization);
    List<OrganizationDto> toDto(List<Organization> organizations);

}
