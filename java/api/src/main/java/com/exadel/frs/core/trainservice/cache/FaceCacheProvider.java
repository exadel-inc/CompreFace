/*
 * Copyright (c) 2020 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class FaceCacheProvider {

    private final FaceDao faceDao;

    private static final long CACHE_EXPIRATION = 60 * 60 * 24L;
    private static final long CACHE_MAXIMUM_SIZE = 10;

    private static final Cache<String, FaceCollection> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(CACHE_EXPIRATION, TimeUnit.SECONDS)
                    .maximumSize(CACHE_MAXIMUM_SIZE)
                    .build();

    public FaceCollection getOrLoad(final String apiKey) {
        if (apiKey == null) {
            return null;
        }

        var result = cache.getIfPresent(apiKey);
        if (result == null) {
            result = FaceCollection.buildFromFaces(faceDao.findAllFacesByApiKey(apiKey));
            cache.put(apiKey, result);
        }

        return result;
    }

    public void invalidate(final String collectionId) {
        cache.invalidate(collectionId);
    }
}