package com.exadel.frs.dto.ui;

import com.exadel.frs.system.statistics.IStatistics;
import com.exadel.frs.system.statistics.ObjectType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class AppResponseDto implements IStatistics {

    private String id;
    private String name;
    private String apiKey;
    private String role;
    private AppOwnerDto owner;

    @Override
    @JsonIgnore
    public String getGuid() {
        return id;
    }

    @Override
    public ObjectType getObjectType() {
        return ObjectType.APP;
    }
}
