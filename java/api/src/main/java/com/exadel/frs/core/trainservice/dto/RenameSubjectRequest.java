package com.exadel.frs.core.trainservice.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RenameSubjectRequest {
    @NotNull
    private SubjectDto subjectDto;
    @NotBlank
    private String oldSubjectName;
}
