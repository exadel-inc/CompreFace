package com.exadel.frs.dto.ui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelCreateDto {

    @NotBlank(message = "Model name cannot be empty")
    private String name;

}
