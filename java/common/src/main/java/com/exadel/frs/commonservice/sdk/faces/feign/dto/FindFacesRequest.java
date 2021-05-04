package com.exadel.frs.commonservice.sdk.faces.feign.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FindFacesRequest {
    @JsonProperty("file")
    private final String imageAsBase64;
}
