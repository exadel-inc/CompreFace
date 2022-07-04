package com.exadel.frs.core.trainservice.cache;

import lombok.Getter;

@Getter
public class ModelStatisticCacheEntry {

    private long requestCount;

    public void incrementRequestCount() {
        requestCount++;
    }
}
