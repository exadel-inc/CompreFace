package com.exadel.frs.commonservice.system.feign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsFacesEntity {

    @JsonProperty("install_guid")
    private String userGuid;

    @JsonProperty("collection_guid")
    private String collectionGuid;

    @JsonProperty("faces_range")
    private String range;
}
