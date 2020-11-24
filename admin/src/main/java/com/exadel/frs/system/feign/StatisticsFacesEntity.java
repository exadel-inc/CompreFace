package com.exadel.frs.system.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsFacesEntity {

    @JsonProperty("user_guid")
    private String userGuid;

    @JsonProperty("collection_guid")
    private String collectionGuid;

    @JsonProperty("number_of_faces")
    private int numberOfFaces;

}
