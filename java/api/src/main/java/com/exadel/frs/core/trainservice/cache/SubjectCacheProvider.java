package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.core.trainservice.dao.SubjectDao;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SubjectCacheProvider {

    private static final long CACHE_EXPIRATION = 60 * 60 * 24L;
    private static final long CACHE_MAXIMUM_SIZE = 20;

    private final SubjectDao subjectDao;

    private static final Cache<String, SubjectCollection> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(CACHE_EXPIRATION, TimeUnit.SECONDS)
                    .maximumSize(CACHE_MAXIMUM_SIZE)
                    .build();

    public SubjectCollection getOrLoad(final String apiKey) {
        var result = cache.getIfPresent(apiKey);
        if (result == null) {
            result = subjectDao.doWithEmbeddingsStream(apiKey, SubjectCollection::from);
            cache.put(apiKey, result);
        }

        return result;
    }

    public void invalidate(final String apiKey) {
        cache.invalidate(apiKey);
    }
}
