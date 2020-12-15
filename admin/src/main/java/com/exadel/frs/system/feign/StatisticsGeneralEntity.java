package com.exadel.frs.system.feign;

import com.exadel.frs.enums.StatisticsType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsGeneralEntity {

    @JsonProperty("user_guid")
    private String userGuid;

    @JsonProperty("action_name")
    private StatisticsType actionName;

}
