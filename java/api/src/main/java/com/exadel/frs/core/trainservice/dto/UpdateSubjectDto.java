package com.exadel.frs.core.trainservice.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSubjectDto {

    @NotBlank
    private String subject;
}
