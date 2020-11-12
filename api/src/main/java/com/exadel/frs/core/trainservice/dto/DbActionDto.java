package com.exadel.frs.core.trainservice.dto;

import com.exadel.frs.core.trainservice.enums.DbAction;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbActionDto {

    @JsonProperty("action")
    private DbAction action;

    @JsonProperty("apiKey")
    private String apiKey;

    @JsonProperty("faceIds")
    private List<String> faceIds;

    @JsonProperty("faceName")
    private String faceName;

    @JsonProperty("uuid")
    private String serverUUID;
}
