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

import static com.exadel.frs.core.trainservice.enums.DbAction.DELETE;
import static com.exadel.frs.core.trainservice.system.global.Constants.SERVER_UUID;
import static java.util.stream.Collectors.toSet;
import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.config.repository.Notifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dto.DbActionDto;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.enums.DbAction;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDao faceDao;
    private final FaceCacheProvider faceCacheProvider;
    private final Notifier notifier;

    public Set<FaceBO> findFaces(final String apiKey) {
        return faceCacheProvider.getOrLoad(apiKey).getFaces();
    }

    public Set<FaceBO> deleteFaceByName(final String faceName, final String apiKey) {
        val faces = faceCacheProvider.getOrLoad(apiKey);

        val deletedFaces = faceDao.deleteFaceByName(faceName, apiKey);

        val faceIds = deletedFaces.stream().map(Face::getId).collect(Collectors.toList());
        deletedFaces.forEach(face -> notifier.notifyWithMessage(new DbActionDto(DELETE, apiKey, faceIds, face.getFaceName(), SERVER_UUID)));

        return deletedFaces
                .stream()
                .map(face -> faces.removeFace(face.getId(), face.getFaceName()))
                .collect(toSet());
    }

    public FaceBO deleteFaceById(final String id, final String apiKey) {
        val collection = faceCacheProvider.getOrLoad(apiKey);
        val face = faceDao.deleteFaceById(id);
        if (face != null) {
            notifier.notifyWithMessage(new DbActionDto(DELETE, face.getApiKey(), List.of(face.getId()), face.getFaceName(), SERVER_UUID));
            return collection.removeFace(face.getId(), face.getFaceName());
        }

        return null;
    }

    public void deleteFacesByModel(final String modelKey) {
        faceDao.deleteFacesByApiKey(modelKey);
        notifier.notifyWithMessage(new DbActionDto(DbAction.DELETE_ALL, modelKey, null, null, SERVER_UUID));
        faceCacheProvider.invalidate(modelKey);
    }

    public int countFacesInModel(final String modelKey) {
        return faceCacheProvider.getOrLoad(modelKey).getFaces().size();
    }
}