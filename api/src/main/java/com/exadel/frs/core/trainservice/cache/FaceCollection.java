package com.exadel.frs.core.trainservice.cache;

import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.entity.Face;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.val;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

public class FaceCollection {

    private final BiMap<CachedFace, Integer> facesMap;

    private INDArray embeddings;

    private INDArray embeddingsCopy;

    private final AtomicInteger size;

    private FaceCollection(BiMap<CachedFace, Integer> facesMap,
                           INDArray embeddings,
                           AtomicInteger size) {
        this.facesMap = facesMap;
        this.embeddings = embeddings;
        if (embeddings != null) {
            this.embeddingsCopy = embeddings.dup();
        }
        this.size = size;
    }

    public BiMap<CachedFace, Integer> getFacesMap() {
        return facesMap;
    }

    public INDArray getEmbeddings() {
        embeddingsCopy.assign(embeddings);
        return embeddingsCopy;
    }

    public static FaceCollection buildFromFaces(final List<Face> faces) {
        if (faces.size() < 1) {
            return new FaceCollection(HashBiMap.create(), null, new AtomicInteger());
        }
        val rawEmbeddings = faces.stream()
                                 .map(Face::getEmbedding)
                                 .map(Face.Embedding::getEmbeddings)
                                 .map(l -> l.stream().mapToDouble(d -> d).toArray())
                                 .toArray(double[][]::new);
        val indArray = Nd4j.create(rawEmbeddings);
        val index = new AtomicInteger();
        Map<CachedFace, Integer> facesMap = faces.stream().collect(toMap(
                face -> new CachedFace(face.getFaceName(), face.getId()),
                face -> index.getAndIncrement()
        ));

        return new FaceCollection(HashBiMap.create(facesMap), indArray, index);
    }

    synchronized public CachedFace addFace(Face faceEntity) {
        val cachedFace = new CachedFace(faceEntity.getFaceName(), faceEntity.getId());
        facesMap.put(cachedFace, size.get());
        val faceEmbeddings = faceEntity.getEmbedding().getEmbeddings()
                                       .stream()
                                       .mapToDouble(d -> d).toArray();
        if (embeddings == null) {
            embeddings = Nd4j.create(new double[][]{faceEmbeddings});
        } else {
            embeddings = Nd4j.concat(0, embeddings,
                    Nd4j.create(new double[][]{faceEmbeddings})
            );
        }

        embeddingsCopy = embeddings.dup();
        size.getAndIncrement();
        return cachedFace;
    }

    synchronized public CachedFace removeFace(final String imageId, final String faceName) {
        if (facesMap.size() == 0) {
            return null;
        }
        val faceToDelete = new CachedFace(faceName, imageId);
        val index = facesMap.get(faceToDelete);
        facesMap.remove(faceToDelete);
        facesMap.entrySet().forEach(entry -> {
            if (entry.getValue() > index) {
                entry.setValue(entry.getValue() - 1);
            }
        });

        embeddings = Nd4j.concat(0, embeddings.get(NDArrayIndex.interval(0, index), NDArrayIndex.all()),
                embeddings.get(NDArrayIndex.interval(index + 1, size.get()), NDArrayIndex.all())
        );
        embeddingsCopy = embeddings.dup();

        size.getAndDecrement();
        return faceToDelete;
    }

    synchronized public Set<CachedFace> getFaces() {
        return facesMap.keySet();
    }
}
