package com.exadel.frs.commonservice.system.feign;

import com.exadel.frs.commonservice.enums.StatisticsType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsGeneralEntity {

    @JsonProperty("install_guid")
    private String userGuid;

    @JsonProperty("action_name")
    private StatisticsType actionName;

}
