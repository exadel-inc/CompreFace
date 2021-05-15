package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.entity.EmbeddingProjection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddingCollection {

    private final BiMap<EmbeddingProjection, Integer> projections2Index;
    private INDArray embeddings;

    public static EmbeddingCollection from(final Stream<Embedding> embeddings) {
        final var rawEmbeddings = new LinkedList<double[]>();
        final Map<EmbeddingProjection, Integer> projections2Index = new HashMap<>();

        var index = new AtomicInteger();
        embeddings.forEach(embedding -> {
            rawEmbeddings.add(embedding.getEmbedding());
            projections2Index.put(EmbeddingProjection.from(embedding), index.getAndIncrement());
        });

        return new EmbeddingCollection(
                HashBiMap.create(projections2Index),
                Nd4j.create(rawEmbeddings.toArray(double[][]::new))
        );
    }

    private int getSize() {
        return (int) embeddings.size(0);
    }

    /**
     * NOTE: current method returns COPY! Each time you invoke it, memory consumed, be careful!
     *
     * @return copy of underlying embeddings array.
     */
    public INDArray getEmbeddings() {
        return embeddings.dup();
    }

    public Set<EmbeddingProjection> getProjections() {
        return Collections.unmodifiableSet(projections2Index.keySet());
    }

    public synchronized void updateSubjectName(String oldSubjectName, String newSubjectName) {
        final List<EmbeddingProjection> projections = projections2Index.keySet()
                .stream()
                .filter(projection -> projection.getSubjectName().equals(oldSubjectName))
                .collect(Collectors.toList());

        projections.forEach(projection -> projections2Index.put(projection.withNewSubjectName(newSubjectName), projections2Index.remove(projection)));
    }

    public synchronized EmbeddingProjection addEmbedding(final Embedding embedding) {
        final var projection = EmbeddingProjection.from(embedding);

        embeddings = Nd4j.concat(
                0,
                embeddings,
                Nd4j.create(new double[][]{embedding.getEmbedding()})
        );
        projections2Index.put(
                projection,
                getSize() - 1
        );

        return projection;
    }

    public synchronized Collection<EmbeddingProjection> removeEmbeddingsBySubjectName(String subjectName) {
        // not efficient at ALL! review current approach

        final List<EmbeddingProjection> toRemove = projections2Index.keySet().stream()
                .filter(projection -> projection.getSubjectName().equals(subjectName))
                .collect(Collectors.toList());

        toRemove.forEach(this::removeEmbedding); // <- rethink

        return toRemove;
    }

    public synchronized EmbeddingProjection removeEmbedding(Embedding embedding) {
        return removeEmbedding(EmbeddingProjection.from(embedding));
    }

    public synchronized EmbeddingProjection removeEmbedding(EmbeddingProjection projection) {
        if (projections2Index.isEmpty()) {
            return null;
        }

        var index = projections2Index.remove(projection);

        // remove embedding by concatenating sub lists [0, index) + [index + 1, size),
        // thus size of resulting array is decreased by one
        embeddings = Nd4j.concat(
                0,
                embeddings.get(NDArrayIndex.interval(0, index), NDArrayIndex.all()),
                embeddings.get(NDArrayIndex.interval(index + 1, getSize()), NDArrayIndex.all())
        );

        // shifting (-1) all indexes, greater than current one
        projections2Index.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > index)
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> projections2Index.replace(e.getKey(), e.getValue(), e.getValue() - 1));

        return projection;
    }

    public synchronized Optional<INDArray> getRawEmbeddingById(UUID embeddingId) {
        return findByEmbeddingId(
                embeddingId,
                // return duplicated row
                entry -> embeddings.getRow(entry.getValue()).dup()
        );
    }

    public synchronized Optional<String> getSubjectNameByEmbeddingId(UUID embeddingId) {
        return findByEmbeddingId(
                embeddingId,
                entry -> entry.getKey().getSubjectName()
        );
    }

    private <T> Optional<T> findByEmbeddingId(UUID embeddingId, Function<Map.Entry<EmbeddingProjection, Integer>, T> func) {
        if (embeddingId == null) {
            return Optional.empty();
        }

        return projections2Index.entrySet()
                .stream()
                .filter(entry -> embeddingId.equals(entry.getKey().getEmbeddingId()))
                .findFirst()
                .map(func);
    }
}
