package com.exadel.frs.dto;

import lombok.Data;

import java.util.List;

@Data
public class AppDto {

    private Long id;
    private String name;
    private String guid;
    private Long organizationId;
    private List<UserRoleDto> userAppRoles;
    private List<ModelAccessDto> appModelAccess;
    private List<ModelDto> models;

}
