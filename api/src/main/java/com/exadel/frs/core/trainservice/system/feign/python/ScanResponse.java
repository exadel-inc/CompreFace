package com.exadel.frs.core.trainservice.system.feign.python;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ScanResponse {

    @JsonProperty(value = "calculator_version")
    private String calculatorVersion;

    private List<ScanResult> result = new ArrayList<>();
}