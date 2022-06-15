package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.core.trainservice.dto.CacheActionDto;
import com.exadel.frs.core.trainservice.service.EmbeddingService;
import com.exadel.frs.core.trainservice.service.NotificationSenderService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmbeddingCacheProvider {

    private static final long CACHE_EXPIRATION = 60 * 60 * 24L;
    private static final long CACHE_MAXIMUM_SIZE = 10;

    private final EmbeddingService embeddingService;

    private final NotificationSenderService notificationSenderService;

    private static final Cache<String, EmbeddingCollection> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(CACHE_EXPIRATION, TimeUnit.SECONDS)
                    .maximumSize(CACHE_MAXIMUM_SIZE)
                    .build();

    public EmbeddingCollection getOrLoad(final String apiKey) {

        var result = cache.getIfPresent(apiKey);

        if (result == null) {
            result = embeddingService.doWithEnhancedEmbeddingProjectionStream(apiKey, EmbeddingCollection::from);

            cache.put(apiKey, result);

            notifyCacheEvent("UPDATE", apiKey);
        }

        return result;
    }

    public void ifPresent(String apiKey, Consumer<EmbeddingCollection> consumer) {
        Optional.ofNullable(cache.getIfPresent(apiKey))
                .ifPresent(consumer);

        EmbeddingCollection dd = cache.getIfPresent(apiKey);
        notifyCacheEvent("UPDATE", apiKey);
    }

    public void invalidate(final String apiKey) {
        cache.invalidate(apiKey);
        notifyCacheEvent("DELETE", apiKey);
    }


    public void receivePutOnCache(String apiKey) {
        var result = embeddingService.doWithEnhancedEmbeddingProjectionStream(apiKey, EmbeddingCollection::from);
        cache.put(apiKey, result);
    }

    public void receiveInvalidateCache(final String apiKey) {
        cache.invalidate(apiKey);
    }

    private void notifyCacheEvent(String event, String apiKey) {
        CacheActionDto cacheActionDto = new CacheActionDto(event, apiKey, SERVER_UUID);
        notificationSenderService.notifyCacheChange(cacheActionDto);
    }
}
