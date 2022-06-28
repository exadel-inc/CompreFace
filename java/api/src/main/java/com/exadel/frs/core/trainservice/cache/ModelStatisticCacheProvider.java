package com.exadel.frs.core.trainservice.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.Optional;
import lombok.Getter;
import lombok.val;
import org.springframework.stereotype.Component;

@Component
public class ModelStatisticCacheProvider {

    private static final Cache<Long, CacheEntry> cache = CacheBuilder.newBuilder().build();

    public CacheEntry get(final long modelId) {
        return Optional.ofNullable(cache.getIfPresent(modelId))
                       .orElseGet(() -> {
                           val entry = new CacheEntry();
                           cache.put(modelId, entry);
                           return entry;
                       });
    }

    @Getter
    public static class CacheEntry {

        private long requestCount;

        public void incrementRequestCount() {
            requestCount++;
        }
    }
}
