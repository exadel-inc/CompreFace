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
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.sdk.faces.FacesApiClient;
import com.exadel.frs.core.trainservice.sdk.faces.feign.dto.FindFacesResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FaceService {

    private static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final FacesApiClient facesApiClient;
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

    public FaceBO findAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        FindFacesResponse findFacesResponse = facesApiClient.findFacesWithCalculator(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold, null);
        val result = findFacesResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        val embedding = result.stream()
                              .findFirst().orElseThrow()
                              .getEmbedding();

        val embeddingToSave = new Face.Embedding(Arrays.asList(embedding), findFacesResponse.getPluginsVersions().getCalculator());

        return faceCacheProvider
                .getOrLoad(modelKey)
                .addFace(faceDao.addNewFace(embeddingToSave, file, faceName, modelKey));
    }
}