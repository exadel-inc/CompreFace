package com.exadel.frs.core.trainservice.dto;

import com.exadel.frs.commonservice.dto.PluginsVersionsDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@RequiredArgsConstructor
public class VerificationResult {

    final List<FaceVerification> result;

    @JsonProperty("plugins_versions")
    PluginsVersionsDto pluginsVersions;
}
