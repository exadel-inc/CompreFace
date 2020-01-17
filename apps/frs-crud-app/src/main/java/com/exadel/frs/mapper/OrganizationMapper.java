package com.exadel.frs.mapper;

import com.exadel.frs.dto.ui.OrgResponseDto;
import com.exadel.frs.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = UserOrgRoleMapper.class)
public interface OrganizationMapper {

    @Mapping(source = "guid", target = "id")
    OrgResponseDto toResponseDto(Organization organization);
    List<OrgResponseDto> toResponseDto(List<Organization> organizations);

}
