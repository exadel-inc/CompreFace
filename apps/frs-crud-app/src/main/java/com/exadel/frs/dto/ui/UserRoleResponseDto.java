package com.exadel.frs.dto.ui;

import lombok.Data;

@Data
public class UserRoleResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String accessLevel;

}
