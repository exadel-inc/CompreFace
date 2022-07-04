package com.exadel.frs.core.trainservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class ModelStatisticCacheProvider {

    private static final Cache<Long, ModelStatisticCacheEntry> cache = CacheBuilder.newBuilder().build();

    public Map<Long, ModelStatisticCacheEntry> getCacheCopyAsMap() {
        return new HashMap<>(cache.asMap());
    }

    public ModelStatisticCacheEntry getCacheEntryByKey(final long key) {
        return Optional.ofNullable(cache.getIfPresent(key))
                       .orElseGet(() -> {
                           val entry = new ModelStatisticCacheEntry();
                           cache.put(key, entry);
                           return entry;
                       });
    }

    public void invalidateCache() {
        cache.invalidateAll();
    }
}
