package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigDto {

    @JsonProperty("clientMaxFileSize")
    private Long maxFileSize;

    @JsonProperty("clientMaxBodySize")
    private Long maxBodySize;
}
