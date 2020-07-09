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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.entity.mongo.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FaceDao {

    private final FacesRepository facesRepository;
    private final GridFsOperations gridFsOperations;

    public EmbeddingFaceList findAllFaceEmbeddings() {
        val faces = facesRepository.findAll();

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFacesIn(List<String> ids) {
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

        val map = faces.stream()
                       .collect(toMap(face -> Pair.of(face.getId(), face.getFaceName()),
                               face -> face.getEmbeddings().stream()
                                           .map(Embedding::getEmbedding)
                                           .collect(toList()), (l1, l2) -> Stream
                                       .concat(l1.stream(), l2.stream())
                                       .collect(toList())
                       ));

        val embeddingFaceList = new EmbeddingFaceList();
        embeddingFaceList.setFaceEmbeddings(map);
        embeddingFaceList.setCalculatorVersion(faces.get(0).getEmbeddings().get(0).getCalculatorVersion());

        return embeddingFaceList;
    }

    public List<Face> findAllFacesByApiKey(final String modelApiKey) {
        return facesRepository.findByApiKey(modelApiKey);
    }

    public List<Face> deleteFaceByName(final String faceName, final String modelApiKey) {
        val deletedFaces = facesRepository.deleteByApiKeyAndFaceName(modelApiKey, faceName);
        deleteFiles(deletedFaces);

        return deletedFaces;
    }

    public Face deleteFaceById(final String faceId) {
        val foundFace = facesRepository.findById(faceId);
        foundFace.ifPresent(face -> {
            facesRepository.delete(face);
            deleteFiles(List.of(face));
        });

        return foundFace.orElse(null);
    }

    public List<Face> deleteFacesByApiKey(final String modelApiKey) {
        val deletedFaces = facesRepository.deleteFacesByApiKey(modelApiKey);
        deleteFiles(deletedFaces);

        return deletedFaces;
    }

    public int countFacesInModel(final String modelApiKey) {
        return facesRepository.countByApiKey(modelApiKey);
    }

    public void updateFacesModelKey(final String modelApiKey, final String newModelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);
        faces.forEach(face -> face.setApiKey(newModelApiKey));

        facesRepository.saveAll(faces);
    }

    private void deleteFiles(final List<Face> deletedFaces) {
        deletedFaces.forEach(face -> {
            val deleteOriginalPhoto = new Query(new Criteria("_id").is(face.getRawImgId()));
            val deleteCroppedPhoto = new Query(new Criteria("_id").is(face.getFaceImgId()));
            gridFsOperations.delete(deleteOriginalPhoto);
            gridFsOperations.delete(deleteCroppedPhoto);
        });
    }

    public Face addNewFace(
            final List<Embedding> embeddings,
            final MultipartFile file,
            final String faceName,
            final String modelKey
    ) throws IOException {
        val faceId = gridFsOperations.store(file.getInputStream(), faceName);

        val face = new Face()
                .setEmbeddings(embeddings)
                .setFaceName(faceName)
                .setApiKey(modelKey)
                .setFaceImgId(faceId)
                .setRawImgId(faceId);

        facesRepository.save(face);

        return face;
    }
}