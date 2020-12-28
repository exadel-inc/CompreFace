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

import com.exadel.frs.core.trainservice.cache.FaceBO;
import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face.Embedding;
import com.exadel.frs.core.trainservice.exception.NoFacesFoundException;
import com.exadel.frs.core.trainservice.exception.TooManyFacesException;
import com.exadel.frs.core.trainservice.system.feign.python.FacesClient;
import com.exadel.frs.core.trainservice.system.feign.python.ScanResponse;
import feign.FeignException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    public static final int MAX_FACES_TO_SAVE = 1;
    public static final int MAX_FACES_TO_RECOGNIZE = 2;

    private final FacesClient facesClient;
    private final FaceDao faceDao;
    private final FaceCacheProvider faceCacheProvider;

    @Override
    public FaceBO scanAndSaveFace(
            final MultipartFile file,
            final String faceName,
            final Double detProbThreshold,
            final String modelKey
    ) throws IOException {
        ScanResponse scanResponse;
        try {
            scanResponse = facesClient.scanFaces(file, MAX_FACES_TO_RECOGNIZE, detProbThreshold);
        } catch (FeignException.BadRequest e) {
            throw new NoFacesFoundException();
        }
        val result = scanResponse.getResult();

        if (result.size() > MAX_FACES_TO_SAVE) {
            throw new TooManyFacesException();
        }

        val embedding = result.stream()
                              .findFirst().orElseThrow()
                              .getEmbedding();

        val embeddingToSave = new Embedding(embedding, scanResponse.getCalculatorVersion());

        return faceCacheProvider.getOrLoad(modelKey).addFace(
                faceDao.addNewFace(embeddingToSave, file, faceName, modelKey)
        );
    }
}