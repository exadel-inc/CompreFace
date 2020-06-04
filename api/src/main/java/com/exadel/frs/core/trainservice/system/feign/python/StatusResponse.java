package com.exadel.frs.core.trainservice.system.feign.python;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class StatusResponse {

    @JsonProperty(value = "build_version")
    String buildVersion1;

    @JsonProperty(value = "calculator_version")
    String calculatorVersion;

    @JsonProperty(value = "status")
    String status;
}
