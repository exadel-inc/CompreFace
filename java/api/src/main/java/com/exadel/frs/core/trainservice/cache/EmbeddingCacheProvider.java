package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.repository.EmbeddingRepository;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class EmbeddingCacheProvider {

    private static final long CACHE_EXPIRATION = 60 * 60 * 24L;
    private static final long CACHE_MAXIMUM_SIZE = 10;

    private final EmbeddingRepository embeddingRepository;

    private static final Cache<String, EmbeddingCollection> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(CACHE_EXPIRATION, TimeUnit.SECONDS)
                    .maximumSize(CACHE_MAXIMUM_SIZE)
                    .build();

    public EmbeddingCollection getOrLoad(final String apiKey) {
        var result = cache.getIfPresent(apiKey);
        if (result == null) {
            result = embeddingRepository.doWithEmbeddingsStream(apiKey, EmbeddingCollection::from);
            cache.put(apiKey, result);
        }

        return result;
    }

    public void ifPresent(String apiKey, Consumer<EmbeddingCollection> consumer) {
        Optional.ofNullable(cache.getIfPresent(apiKey))
                .ifPresent(consumer);
    }

    public void invalidate(final String apiKey) {
        cache.invalidate(apiKey);
    }
}
