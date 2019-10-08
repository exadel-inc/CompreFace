package com.exadel.frs.proxy.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecognizeResponse {

    @JsonProperty("box parameters")
    private List<List<Double>> boxParameters;
    private String prediction;
    private Double probability;

}
