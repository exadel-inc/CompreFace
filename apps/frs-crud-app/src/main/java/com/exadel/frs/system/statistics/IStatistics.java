package com.exadel.frs.system.statistics;

public interface IStatistics {
    String getGuid();

    default ObjectType getObjectType() {
        return ObjectType.UNKNOWN;
    }
}
