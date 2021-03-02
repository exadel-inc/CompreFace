package com.exadel.frs.core.trainservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProcessImageParams {
    private String apiKey;
    private Object file;
    private Integer limit;
    private Double detProbThreshold;
    private String facePlugins;
    private Map<String, Object> additionalParams;
}
