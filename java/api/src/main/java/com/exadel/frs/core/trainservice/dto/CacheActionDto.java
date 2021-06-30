package com.exadel.frs.core.trainservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheActionDto {
    @JsonProperty("cacheAction")
    private String cacheAction;
    @JsonProperty("apiKey")
    private String apiKey;
    @JsonProperty("uuid")
    private String serverUUID;
    }
