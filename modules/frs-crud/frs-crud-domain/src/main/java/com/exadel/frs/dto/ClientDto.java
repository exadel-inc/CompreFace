package com.exadel.frs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientDto {

    private Long id;
    private String email;
    private String username;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

}
