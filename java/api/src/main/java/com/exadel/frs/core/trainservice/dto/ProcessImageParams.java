package com.exadel.frs.core.trainservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ProcessImageParams {
    private String apiKey;
    private Object file;
    private String imageBase64;
    private Integer limit;
    private Double detProbThreshold;
    private String facePlugins;
    private Boolean status;
    private Boolean detectFaces;
    private Map<String, Object> additionalParams;
}
