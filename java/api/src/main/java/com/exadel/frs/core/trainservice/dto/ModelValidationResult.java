package com.exadel.frs.core.trainservice.dto;

import static com.exadel.frs.commonservice.enums.ValidationResult.FORBIDDEN;
import com.exadel.frs.commonservice.enums.ValidationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModelValidationResult {

    private long modelId;
    private ValidationResult result = FORBIDDEN;
}
