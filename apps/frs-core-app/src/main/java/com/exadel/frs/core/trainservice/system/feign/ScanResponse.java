package com.exadel.frs.core.trainservice.system.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScanResponse {

    @JsonProperty(value = "calculator_version")
    private String calculatorVersion;

    private List<ScanResult> result = new ArrayList<>();
}