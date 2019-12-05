package com.exadel.frs.dto;

import com.exadel.frs.enums.AppRole;
import lombok.Data;

@Data
public class OrganizationRoleDto {

    private Long organizationId;
    private AppRole role;

}
