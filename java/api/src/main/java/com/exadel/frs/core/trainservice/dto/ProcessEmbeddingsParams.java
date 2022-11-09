package com.exadel.frs.core.trainservice.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessEmbeddingsParams {

    private String apiKey;
    private Integer predictionCount;
    private List<double[]> embeddings;
}
