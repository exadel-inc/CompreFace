package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FaceCacheProvider {

    private final FaceDao faceDao;
    private final ApplicationContext context;

    private static final long CACHE_EXPIRATION = 10;
    private static final long CACHE_MAXIMUM_SIZE = 3;

    private static final Cache<String, FaceCollection> cache = CacheBuilder.newBuilder()
                                                                           .expireAfterAccess(CACHE_EXPIRATION, TimeUnit.SECONDS)
                                                                           .maximumSize(CACHE_MAXIMUM_SIZE)
                                                                           .build();

    public FaceCollection getOrLoad(String collectionId) {
        if (collectionId == null) {
            return null;
        }

        FaceCollection result = cache.getIfPresent(collectionId);
        if (result == null) {
            result = FaceCollection.buildFromFaces(findFaces(collectionId));
            cache.put(collectionId, result);
        }
        return result;
    }

    public boolean cached(String collectionId) {
        return cache.getIfPresent(collectionId) != null;
    }

    public void invalidate(String collectionId) {
        cache.invalidate(collectionId);
    }

    private List<Face> findFaces(final String apiKey) {
        return faceDao.findAllFacesByApiKey(apiKey);
    }
}
