package com.exadel.frs.core.trainservice.cache;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ModelStatisticCacheProviderTest {

    private final ModelStatisticCacheProvider cacheProvider = new ModelStatisticCacheProvider();

    @AfterEach
    void cleanup() {
        cacheProvider.invalidateCache();
    }

    @Test
    void shouldIncrementCacheValueWhenIncrementRequestCountMethodInvoked() {
        cacheProvider.incrementRequestCount(1L);

        cacheProvider.incrementRequestCount(2L);
        cacheProvider.incrementRequestCount(2L);

        cacheProvider.incrementRequestCount(3L);
        cacheProvider.incrementRequestCount(3L);
        cacheProvider.incrementRequestCount(3L);

        var cache = cacheProvider.getCacheCopyAsMap();

        assertThat(cache).isNotNull().hasSize(3)
                         .containsEntry(1L, 1)
                         .containsEntry(2L, 2)
                         .containsEntry(3L, 3);
    }

    @Test
    void shouldInvalidateAllCacheWhenInvalidateCacheMethodInvoked() {
        cacheProvider.incrementRequestCount(1L);
        cacheProvider.incrementRequestCount(2L);
        cacheProvider.incrementRequestCount(3L);

        var cacheBefore = cacheProvider.getCacheCopyAsMap();

        assertThat(cacheBefore).isNotNull().hasSize(3);

        cacheProvider.invalidateCache();

        var cacheAfter = cacheProvider.getCacheCopyAsMap();

        assertThat(cacheAfter).isNotNull().isEmpty();
    }
}
