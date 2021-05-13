package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
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
public class SubjectCollection {

    // embedding meta to index
    private final BiMap<SubjectMeta, Integer> metaMap;
    private INDArray embeddings;

    public static SubjectCollection from(final Stream<Embedding> embeddings) {
        final var rawEmbeddings = new LinkedList<double[]>();
        final Map<SubjectMeta, Integer> metaMap = new HashMap<>();

        var index = new AtomicInteger();
        embeddings.forEach(embedding -> {
            rawEmbeddings.add(embedding.getEmbedding());
            metaMap.put(SubjectMeta.from(embedding), index.getAndIncrement());
        });

        return new SubjectCollection(
                HashBiMap.create(metaMap),
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

    public Set<SubjectMeta> getMetas() {
        return Collections.unmodifiableSet(metaMap.keySet());
    }

    public synchronized void updateSubjectName(String oldSubjectName, UUID newSubjectId, String newSubjectName) {
        final List<SubjectMeta> metas = metaMap.keySet()
                .stream()
                .filter(metaKey -> metaKey.getSubjectName().equals(oldSubjectName))
                .collect(Collectors.toList());

        metas.forEach(metaKey -> metaMap.put(metaKey.withNewValues(newSubjectId, newSubjectName), metaMap.remove(metaKey)));
    }

    public synchronized SubjectMeta addEmbedding(final Embedding embedding) {
        embeddings = Nd4j.concat(
                0,
                embeddings,
                Nd4j.create(new double[][]{embedding.getEmbedding()})
        );

        final var metaKey = SubjectMeta.from(embedding);
        metaMap.put(
                metaKey,
                getSize() - 1
        );

        return metaKey;
    }

    public synchronized Collection<SubjectMeta> removeSubject(UUID subjectId) {
        // not efficient at ALL! review current approach

        final List<SubjectMeta> toRemove = metaMap.keySet().stream()
                .filter(meta -> meta.getSubjectId().equals(subjectId))
                .collect(Collectors.toList());

        toRemove.forEach(this::removeEmbedding); // <- rethink it

        return toRemove;
    }

    public synchronized SubjectMeta removeEmbedding(Embedding embedding) {
        return removeEmbedding(SubjectMeta.from(embedding));
    }

    public synchronized SubjectMeta removeEmbedding(SubjectMeta metaKey) {
        if (metaMap.isEmpty()) {
            return null;
        }

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

    public synchronized Optional<INDArray> getEmbeddingById(UUID embeddingId) {
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

    private <T> Optional<T> findByEmbeddingId(UUID embeddingId, Function<Map.Entry<SubjectMeta, Integer>, T> func) {
        if (embeddingId == null) {
            return Optional.empty();
        }

        return metaMap.entrySet()
                .stream()
                .filter(entry -> embeddingId.equals(entry.getKey().getEmbeddingId()))
                .findFirst()
                .map(func);
    }
}
