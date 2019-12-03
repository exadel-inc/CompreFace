package com.exadel.frs.dto;

import com.exadel.frs.enums.OrganizationRole;
import lombok.Data;

@Data
public class UserOrganizationRoleDto {

    private Long userId;
    private Long organizationId;
    private OrganizationRole role;

}
