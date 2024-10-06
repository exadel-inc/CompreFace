package com.exadel.frs.core.trainservice.dto;

import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingsVerificationRequest {

    @NotEmpty
    private double[] source;

    @NotEmpty
    private double[][] targets;
}
