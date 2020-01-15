package com.exadel.frs.dto;

import com.exadel.frs.system.statistics.IStatistics;
import com.exadel.frs.system.statistics.ObjectType;
import lombok.Data;

import java.util.List;

@Data
public class ModelDto implements IStatistics {

    private String name;
    private String guid;
    private String apiKey;
    private List<AppAccessDto> appModelAccess;
    private Long appId;

    @Override
    public ObjectType getObjectType() {
        return ObjectType.MODEL;
    }
}
