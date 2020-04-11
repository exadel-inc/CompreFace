package com.exadel.frs.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleUpdateDto {

    @NotNull
    private String userId;

    @NotNull
    private String role;

}
