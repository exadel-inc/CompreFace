package com.exadel.frs.core.trainservice.scan;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ScanBox {
    private Double probability;
    @JsonProperty(value = "x_max")
    private Integer xMax;
    @JsonProperty(value = "y_max")
    private Integer yMax;
    @JsonProperty(value = "x_min")
    private Integer xMin;
    @JsonProperty(value = "y_min")
    private Integer yMin;
}
