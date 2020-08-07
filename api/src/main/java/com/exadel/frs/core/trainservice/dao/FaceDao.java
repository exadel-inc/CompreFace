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

import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.entity.postgres.Face;
import com.exadel.frs.core.trainservice.entity.postgres.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.postgres.FacesRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FaceDao {

    private final FacesRepository facesRepository;

    public EmbeddingFaceList findAllFacesIn(List<Long> ids) {
        val faces = facesRepository.findByIdIn(ids);

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFaceEmbeddingsByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);

        return facesToEmbeddingList(faces);
    }

    private EmbeddingFaceList facesToEmbeddingList(List<Face> faces) {
        if (faces.isEmpty()) {
            return new EmbeddingFaceList();
        }

        Map<String, List<Double>> map = faces.stream().collect(toMap(
                face -> face.getFaceName(),
                face -> face.getEmbedding().getEmbeddings()
                )
        );

        val embeddingFaceList = new EmbeddingFaceList();
        embeddingFaceList.setFaceEmbeddings(map);
        embeddingFaceList.setCalculatorVersion(faces.get(0).getEmbedding().getCalculatorVersion());

        return embeddingFaceList;
    }

    public List<Face> findAllFacesByApiKey(final String modelApiKey) {
        return facesRepository.findByApiKey(modelApiKey);
    }

    public List<Face> deleteFaceByName(final String faceName, final String modelApiKey) {
        return facesRepository.deleteByApiKeyAndFaceName(modelApiKey, faceName);
    }

    public Face deleteFaceById(final Long faceId) {
        val foundFace = facesRepository.findById(faceId);
        foundFace.ifPresent(face -> {
            facesRepository.delete(face);
        });

        return foundFace.orElse(null);
    }

    public List<Face> deleteFacesByApiKey(final String modelApiKey) {
        return facesRepository.deleteFacesByApiKey(modelApiKey);
    }

    public int countFacesInModel(final String modelApiKey) {
        return facesRepository.countByApiKey(modelApiKey);
    }

    public void updateFacesModelKey(final String modelApiKey, final String newModelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);
        faces.forEach(face -> face.setApiKey(newModelApiKey));

        facesRepository.saveAll(faces);
    }

    public Face addNewFace(
            final Embedding embeddings,
            final MultipartFile file,
            final String faceName,
            final String modelKey
    ) throws IOException {
        val face = new Face()
                .setEmbedding(embeddings)
                .setFaceName(faceName)
                .setApiKey(modelKey)
                .setFaceImg(file.getBytes())
                .setRawImg(file.getBytes());

        facesRepository.save(face);

        return face;
    }
}