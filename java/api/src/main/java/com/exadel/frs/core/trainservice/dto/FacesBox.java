package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Accessors(chain = true)
@JsonInclude(NON_NULL)
public class FacesBox {

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
