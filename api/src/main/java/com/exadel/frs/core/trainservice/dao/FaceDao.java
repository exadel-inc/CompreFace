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

package com.exadel.frs.core.trainservice.dao;

import static java.util.UUID.randomUUID;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Face.Embedding;
import com.exadel.frs.core.trainservice.entity.Image;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.repository.ImagesRepository;
import com.exadel.frs.core.trainservice.system.global.ImageProperties;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FaceDao {

    private final FacesRepository facesRepository;

    private final ImagesRepository imagesRepository;

    private final ImageProperties imageProperties;

    public List<Face> findAllFacesByApiKey(final String modelApiKey) {
        return facesRepository.findByApiKey(modelApiKey);
    }

    public List<Face> deleteFaceByName(final String faceName, final String modelApiKey) {
        return facesRepository.deleteByApiKeyAndFaceName(modelApiKey, faceName);
    }

    public Face deleteFaceById(final String faceId) {
        val foundFace = facesRepository.findById(faceId);
        foundFace.ifPresent(face -> {
            facesRepository.delete(face);
        });

        return foundFace.orElse(null);
    }

    public void deleteFacesByApiKey(final String modelApiKey) {
        facesRepository.deleteFacesByApiKey(modelApiKey);
    }

    public int countFacesInModel(final String modelApiKey) {
        return facesRepository.countByApiKey(modelApiKey);
    }

    public Face addNewFace(
            final Embedding embeddings,
            final MultipartFile file,
            final String faceName,
            final String modelKey
    ) throws IOException {
        val face = new Face()
                .setId(randomUUID().toString())
                .setEmbedding(embeddings)
                .setFaceName(faceName)
                .setApiKey(modelKey);

        if (imageProperties.isSaveImagesToDB()) {
            val image = new Image()
                    .setFaceImg(file.getBytes())
                    .setRawImg(file.getBytes())
                    .setFace(face);

            imagesRepository.save(image);
        }

        return facesRepository.save(face);
    }
}