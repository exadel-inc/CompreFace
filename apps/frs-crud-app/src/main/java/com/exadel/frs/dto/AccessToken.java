package com.exadel.frs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessToken {

    @JsonProperty("access_token")
    private String accessToken;

}
