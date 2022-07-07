package com.exadel.frs.core.trainservice.cache;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
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
    void shouldGetEmptyCacheEntryByKeyIfCacheEntryDoesNotExist() {
        var cacheEntry = cacheProvider.getRequestCount(1L);

        assertThat(cacheEntry).isNotNull();
        assertThat(cacheEntry.getRequestCount()).isZero();
    }

    @Test
    void shouldGetCacheEntryByKeyIfCacheEntryExists() {
        var expected = cacheProvider.getRequestCount(1L);

        expected.incrementRequestCount();
        expected.incrementRequestCount();
        expected.incrementRequestCount();

        var actual = cacheProvider.getRequestCount(1L);

        assertThat(actual).isNotNull();
        assertThat(actual.getRequestCount()).isEqualTo(3L);
    }

    @Test
    void shouldGetCacheCopyAsMap() {
        var expected = Map.of(
                1L, cacheProvider.getRequestCount(1L),
                2L, cacheProvider.getRequestCount(2L),
                3L, cacheProvider.getRequestCount(3L)
        );

        expected.get(1L).incrementRequestCount();
        expected.get(1L).incrementRequestCount();
        expected.get(1L).incrementRequestCount();

        expected.get(2L).incrementRequestCount();
        expected.get(2L).incrementRequestCount();

        expected.get(3L).incrementRequestCount();

        var actual = cacheProvider.getCacheCopyAsMap();

        assertThat(actual).isNotNull().hasSize(3);

        assertThat(actual.get(1L)).isNotNull();
        assertThat(actual.get(1L).getRequestCount()).isEqualTo(3L);

        assertThat(actual.get(2L)).isNotNull();
        assertThat(actual.get(2L).getRequestCount()).isEqualTo(2L);

        assertThat(actual.get(3L)).isNotNull();
        assertThat(actual.get(3L).getRequestCount()).isEqualTo(1L);
    }

    @Test
    void shouldInvalidateAllCache() {
        var cache = Map.of(
                1L, cacheProvider.getRequestCount(1L),
                2L, cacheProvider.getRequestCount(2L),
                3L, cacheProvider.getRequestCount(3L)
        );

        cache.get(1L).incrementRequestCount();
        cache.get(1L).incrementRequestCount();
        cache.get(1L).incrementRequestCount();

        cache.get(2L).incrementRequestCount();
        cache.get(2L).incrementRequestCount();

        cache.get(3L).incrementRequestCount();

        cacheProvider.invalidateCache();

        var actual = cacheProvider.getCacheCopyAsMap();

        assertThat(actual).isNotNull().isEmpty();
    }
}
