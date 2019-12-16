package com.exadel.frs.dto;

import com.exadel.frs.enums.AppRole;
import lombok.Data;

@Data
public class AppRoleDto {

    private Long appId;
    private AppRole role;

}
