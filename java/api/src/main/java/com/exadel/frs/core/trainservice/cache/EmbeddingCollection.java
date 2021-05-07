package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.exception.ImageNotFoundException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddingCollection {

    // embedding meta to index
    private final BiMap<EmbeddingMeta, Integer> metaMap;
    private INDArray embeddings;

    public static EmbeddingCollection build(final List<Embedding> embeddings) {
        if (CollectionUtils.isEmpty(embeddings)) {
            return new EmbeddingCollection(
                    HashBiMap.create(),
                    Nd4j.create(new double[][]{})
            );
        }

        final var rawEmbeddings = new double[embeddings.size()][];
        final Map<EmbeddingMeta, Integer> metaMap = new HashMap<>();

        var index = 0;
        while (index < embeddings.size()) {
            var embedding = embeddings.get(index);
            rawEmbeddings[index] = embedding.getEmbedding();
            metaMap.put(EmbeddingMeta.from(embedding), index);

            index++;
        }

        return new EmbeddingCollection(
                HashBiMap.create(metaMap),
                Nd4j.create(rawEmbeddings)
        );
    }

    private int getSize() {
        return (int) embeddings.size(0);
    }

    /**
     * Please, note, current method returns COPY! Each time you invoke it, memory consumed, be careful!
     *
     * @return copy of underlying embeddings array.
     */
    public INDArray getEmbeddings() {
        return embeddings.dup();
    }

    public synchronized EmbeddingMeta addEmbedding(final Embedding embedding) {
        embeddings = Nd4j.concat(
                0,
                embeddings,
                Nd4j.create(new double[][]{embedding.getEmbedding()})
        );

        final var metaKey = EmbeddingMeta.from(embedding);
        metaMap.put(
                metaKey,
                getSize() - 1
        );

        return metaKey;
    }

    public synchronized EmbeddingMeta removeEmbedding(final String subjectName, final UUID imgId) {
        if (metaMap.isEmpty()) {
            return null;
        }

        var metaKey = new EmbeddingMeta(subjectName, imgId);
        var index = metaMap.remove(metaKey);

        // remove embedding by concatenating sub lists [0, index) + [index + 1, size),
        // thus size of resulting array is decreased by one
        embeddings = Nd4j.concat(
                0,
                embeddings.get(NDArrayIndex.interval(0, index), NDArrayIndex.all()),
                embeddings.get(NDArrayIndex.interval(index + 1, getSize()), NDArrayIndex.all())
        );

        // shifting (-1) all indexes, greater than current one
        metaMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > index)
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> metaMap.replace(e.getKey(), e.getValue(), e.getValue() - 1));

        return metaKey;
    }

    public synchronized Optional<INDArray> getEmbeddingByImgId(UUID imgId) {
        if (imgId == null) {
            return Optional.empty();
        }

        var embeddingMeta = metaMap.keySet().stream()
                .filter(em -> imgId.equals(em.getImgId()))
                .findFirst()
                .orElseThrow(() -> new ImageNotFoundException(imgId.toString()));

        var index = metaMap.get(embeddingMeta);

        // return copy
        return Optional.of(embeddings.getRow(index).dup());
    }
}
