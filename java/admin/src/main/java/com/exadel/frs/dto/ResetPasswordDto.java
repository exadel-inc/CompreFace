package com.exadel.frs.dto;

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
public class ResetPasswordDto {

    @NotBlank(message = "The password cannot be blank")
    @Size(min = 8, max = 255, message = "The password size must be between 8 and 255 characters")
    private String password;
}
