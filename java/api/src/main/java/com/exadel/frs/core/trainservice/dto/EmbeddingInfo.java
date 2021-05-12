package com.exadel.frs.core.trainservice.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
public class EmbeddingInfo {

    @NotNull
    String calculator;

    @NotNull
    double[] embedding;

    @NotNull
    byte[] imgContent;
}
