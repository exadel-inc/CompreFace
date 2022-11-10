package com.exadel.frs.core.trainservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingVerificationProcessResult {

    private double[] embedding;
    private float similarity;
}
