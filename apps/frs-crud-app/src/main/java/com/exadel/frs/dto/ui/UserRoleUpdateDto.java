package com.exadel.frs.dto.ui;

import javax.validation.constraints.NotBlank;
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

    @NotBlank(message = "UserId cannot be empty")
    private String userId;

    @NotBlank(message = "Role cannot be empty")
    private String role;
}
