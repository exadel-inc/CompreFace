package com.exadel.frs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private List<UserAppRoleDto> userAppRoles;
    private List<UserOrganizationRoleDto> userOrganizationRoles;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
