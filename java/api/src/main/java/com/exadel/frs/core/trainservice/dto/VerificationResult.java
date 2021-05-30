package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.exadel.frs.commonservice.dto.PluginsVersionsDto;
import lombok.Value;

import java.util.List;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationResult {
    List<FaceVerification> result;

    @JsonProperty("plugins_versions")
    PluginsVersionsDto pluginsVersions;
}
