package com.exadel.frs.core.trainservice.dto;

import static com.exadel.frs.core.trainservice.system.global.Constants.SUBJECT_DESC;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiParam;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDto {

    @ApiParam(value = SUBJECT_DESC, required = true)
    @JsonProperty("subject")
    @NotBlank(message = "Subject name cannot be empty")
    @Size(min = 1, max = 50, message = "Subject name size must be between 1 and 50")
    private String subjectName;
}
