package com.exadel.frs.dto;

import com.exadel.frs.system.statistics.IStatistics;
import com.exadel.frs.system.statistics.ObjectType;
import lombok.Data;

import java.util.List;

@Data
public class AppDto implements IStatistics {

    private Long id;
    private String name;
    private String guid;
    private String apiKey;
    private Long organizationId;
    private List<UserRoleDto> userAppRoles;
    private List<ModelAccessDto> appModelAccess;
    private List<ModelDto> models;

    @Override
    public ObjectType getObjectType() {
        return ObjectType.APP;
    }
}
