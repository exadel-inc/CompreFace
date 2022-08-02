package com.exadel.frs.dto.ui;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordTokenDto {

    @NotBlank(message = "User's email cannot be empty")
    @Size(min = 1, max = 63, message = "User's email size must be between 1 and 63")
    private String email;
}
