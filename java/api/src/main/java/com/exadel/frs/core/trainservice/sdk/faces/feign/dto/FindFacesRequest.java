package com.exadel.frs.core.trainservice.sdk.faces.feign.dto;

import com.exadel.frs.core.trainservice.system.global.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindFacesRequest {
    @JsonProperty("file")
    private String imageAsBase64;

    private Integer limit;

    @JsonProperty(Constants.DET_PROB_THRESHOLD)
    private Double detProbThreshold;

    @JsonProperty(Constants.FACE_PLUGINS)
    private String facePlugins;
}
