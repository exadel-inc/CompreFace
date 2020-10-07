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

package com.exadel.frs.core.trainservice.service;

import static java.util.stream.Collectors.toSet;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDao faceDao;
    private final FaceCacheProvider faceCacheProvider;

    public Set<FaceBO> findFaces(final String apiKey) {
        return faceCacheProvider.getOrLoad(apiKey).getFaces();
    }

    public Set<FaceBO> deleteFaceByName(final String faceName, final String apiKey) {
        val faces = faceCacheProvider.getOrLoad(apiKey);

        return faceDao.deleteFaceByName(faceName, apiKey)
                      .stream()
                      .map(face -> faces.removeFace(face.getId(), face.getFaceName()))
                      .collect(toSet());
    }

    public FaceBO deleteFaceById(final String id, final String apiKey) {
        val collection = faceCacheProvider.getOrLoad(apiKey);
        val face = faceDao.deleteFaceById(id);
        if (face != null) {
            return collection.removeFace(face.getId(), face.getFaceName());
        }

        return null;
    }

    public void deleteFacesByModel(final String modelKey) {
        faceDao.deleteFacesByApiKey(modelKey);
        faceCacheProvider.invalidate(modelKey);
    }

    public int countFacesInModel(final String modelKey) {
        return faceCacheProvider.getOrLoad(modelKey).getFaces().size();
    }
}