package com.exadel.frs.dto.ui;

import com.exadel.frs.dto.UserRoleDto;
import lombok.Data;

import java.util.List;

@Data
public class AppUpdateDto {

    private String name;
    private List<UserRoleDto> userAppRoles;

}
