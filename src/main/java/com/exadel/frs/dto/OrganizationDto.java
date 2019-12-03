package com.exadel.frs.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationDto {

    private Long id;
    private String name;
    private List<UserOrganizationRoleDto> userOrganizationRoles;

}
