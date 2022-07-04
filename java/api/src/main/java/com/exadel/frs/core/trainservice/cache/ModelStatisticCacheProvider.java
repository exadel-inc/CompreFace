package com.exadel.frs.core.trainservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class ModelStatisticCacheProvider {

    private static final Cache<Long, ModelStatisticCacheEntry> cache = CacheBuilder.newBuilder().build();

    public Map<Long, ModelStatisticCacheEntry> getCopyAsMap() {
        return new HashMap<>(cache.asMap());
    }

    public ModelStatisticCacheEntry get(final long modelId) {
        return Optional.ofNullable(cache.getIfPresent(modelId))
                       .orElseGet(() -> {
                           val entry = new ModelStatisticCacheEntry();
                           cache.put(modelId, entry);
                           return entry;
                       });
    }

    public Set<Long> getKeySet() {
        return cache.asMap().keySet();
    }

    public boolean isEmpty() {
        return cache.size() == 0;
    }

    public void invalidate(Long key) {
        cache.invalidate(key);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }
}
