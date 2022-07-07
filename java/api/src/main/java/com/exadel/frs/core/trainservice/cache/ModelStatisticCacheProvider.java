package com.exadel.frs.core.trainservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ModelStatisticCacheProvider {

    private static final Cache<Long, Integer> cache = CacheBuilder.newBuilder().build();

    public void incrementRequestCount(final long key) {
        cache.asMap().compute(key, (k, v) -> v == null ? 1 : v + 1);
    }

    public Map<Long, Integer> getCacheCopyAsMap() {
        return new HashMap<>(cache.asMap());
    }

    public boolean isEmpty() {
        return cache.size() == 0;
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }
}
