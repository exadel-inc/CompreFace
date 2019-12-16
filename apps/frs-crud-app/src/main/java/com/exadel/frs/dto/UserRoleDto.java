package com.exadel.frs.dto;

import com.exadel.frs.enums.AppRole;
import lombok.Data;

@Data
public class UserRoleDto {

    private Long userId;
    private AppRole role;

}
