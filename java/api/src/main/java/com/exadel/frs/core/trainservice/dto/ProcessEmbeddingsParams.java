package com.exadel.frs.core.trainservice.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessEmbeddingsParams {

    private String apiKey;
    private double[][] embeddings;
    private Map<String, Object> additionalParams;
}
