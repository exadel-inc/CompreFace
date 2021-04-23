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

import com.exadel.frs.commonservice.entity.Face;
import com.exadel.frs.commonservice.exception.ImageNotFoundException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.val;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class FaceCollection {

    private final BiMap<FaceBO, Integer> facesMap;
    private final AtomicInteger size;
    private INDArray embeddings;
    private INDArray embeddingsCopy;

    private FaceCollection(
            BiMap<FaceBO, Integer> facesMap,
            INDArray embeddings,
            AtomicInteger size
    ) {
        this.facesMap = facesMap;
        this.embeddings = embeddings;
        if (embeddings != null) {
            this.embeddingsCopy = embeddings.dup();
        }
        this.size = size;
    }

    public BiMap<FaceBO, Integer> getFacesMap() {
        return facesMap;
    }

    public INDArray getEmbeddings() {
        if (embeddingsCopy != null) {
            embeddingsCopy.assign(embeddings);
        }
        return embeddingsCopy;
    }

    public static FaceCollection buildFromFaces(final List<Face> faces) {
        if (faces.isEmpty()) {
            return new FaceCollection(HashBiMap.create(), null, new AtomicInteger());
        }

        val rawEmbeddings = faces.stream()
                                 .map(Face::getEmbedding)
                                 .map(Face.Embedding::getEmbeddings)
                                 .map(l -> l.stream().mapToDouble(d -> d).toArray())
                                 .toArray(double[][]::new);
        val indArray = Nd4j.create(rawEmbeddings);
        val index = new AtomicInteger();
        val facesMap = faces.stream()
                            .collect(Collectors.<Face, FaceBO, Integer>toMap(
                                    face -> new FaceBO(face.getFaceName(), face.getId()),
                                    face -> index.getAndIncrement()
                            ));

        return new FaceCollection(HashBiMap.create(facesMap), indArray, index);
    }

    public synchronized FaceBO addFace(final Face face) {
        val cachedFace = new FaceBO(face.getFaceName(), face.getId());
        facesMap.put(cachedFace, size.get());
        val faceEmbeddings = face.getEmbedding().getEmbeddings()
                                 .stream()
                                 .mapToDouble(d -> d).toArray();
        if (embeddings == null) {
            embeddings = Nd4j.create(new double[][]{faceEmbeddings});
        } else {
            embeddings = Nd4j.concat(
                    0,
                    embeddings,
                    Nd4j.create(new double[][]{faceEmbeddings})
            );
        }

        embeddingsCopy = embeddings.dup();
        size.getAndIncrement();

        return cachedFace;
    }

    public synchronized FaceBO removeFace(final String imageId, final String faceName) {
        if (facesMap.size() == 0) {
            return null;
        }

        val faceToDelete = new FaceBO(faceName, imageId);
        val index = facesMap.get(faceToDelete);
        facesMap.remove(faceToDelete);
        val shiftedItems = facesMap.entrySet().stream()
                                   .filter(entry -> entry.getValue() > index)
                                   .sorted(Map.Entry.comparingByValue())
                                   .collect(Collectors.toList());

        shiftedItems.forEach(e -> facesMap.replace(e.getKey(), e.getValue(), e.getValue() - 1));

        embeddings = Nd4j.concat(
                0,
                embeddings.get(NDArrayIndex.interval(0, index), NDArrayIndex.all()),
                embeddings.get(NDArrayIndex.interval(index + 1, size.get()), NDArrayIndex.all())
        );
        embeddingsCopy = embeddings.dup();

        size.getAndDecrement();

        return faceToDelete;
    }

    public synchronized Set<FaceBO> getFaces() {
        return facesMap.keySet();
    }

    public synchronized INDArray getEmbeddingsByImageId(String imageId) {
        val index = facesMap.get(facesMap.keySet().stream()
                                         .filter(face -> face.getImageId().equals(imageId))
                                         .findFirst()
                                         .orElseThrow(() -> new ImageNotFoundException(imageId)));
        embeddingsCopy = embeddings.dup();
        return embeddingsCopy.get(NDArrayIndex.interval(index, index + 1), NDArrayIndex.all());
    }
}