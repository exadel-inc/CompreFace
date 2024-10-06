package com.exadel.frs.core.trainservice.cache;

import com.exadel.frs.commonservice.entity.Embedding;
import com.exadel.frs.commonservice.exception.IncorrectImageIdException;
import com.exadel.frs.commonservice.projection.EmbeddingProjection;
import com.exadel.frs.commonservice.projection.EnhancedEmbeddingProjection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.val;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddingCollection {

    private final BiMap<EmbeddingProjection, Integer> projection2Index;
    private INDArray embeddings;

    public static EmbeddingCollection from(final Stream<EnhancedEmbeddingProjection> stream) {
        val rawEmbeddings = new LinkedList<double[]>();
        val projections2Index = new HashMap<EmbeddingProjection, Integer>();
        val index = new AtomicInteger(); // just to bypass 'final' variables restriction inside lambdas

        stream.forEach(projection -> {
            projections2Index.put(EmbeddingProjection.from(projection), index.getAndIncrement());
            rawEmbeddings.add(projection.embeddingData());
        });

        return new EmbeddingCollection(
                HashBiMap.create(projections2Index),
                rawEmbeddings.isEmpty()
                        ? Nd4j.empty()
                        : Nd4j.create(rawEmbeddings.toArray(double[][]::new))
        );
    }

    public Map<Integer, EmbeddingProjection> getIndexMap() {
        // returns index to projection map
        return Collections.unmodifiableMap(projection2Index.inverse());
    }

    public Set<EmbeddingProjection> getProjections() {
        return Collections.unmodifiableSet(projection2Index.keySet());
    }

    private int getSize() {
        // should be invoked only if underlying array is not empty!
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

    public synchronized void updateSubjectName(String oldSubjectName, String newSubjectName) {
        final List<EmbeddingProjection> projections = projection2Index.keySet()
                .stream()
                .filter(projection -> projection.subjectName().equals(oldSubjectName))
                .toList();

        projections.forEach(projection -> projection2Index.put(
                projection.withNewSubjectName(newSubjectName),
                projection2Index.remove(projection)
        ));
    }

    public synchronized EmbeddingProjection addEmbedding(final Embedding embedding) {
        final var projection = EmbeddingProjection.from(embedding);

        final INDArray array = Nd4j.create(new double[][]{embedding.getEmbedding()});

        embeddings = embeddings.isEmpty()
                ? array
                : Nd4j.concat(0, embeddings, array);

        projection2Index.put(
                projection,
                getSize() - 1
        );

        return projection;
    }

    public synchronized Collection<EmbeddingProjection> removeEmbeddingsBySubjectName(String subjectName) {
        // not efficient at ALL! review current approach!

        final List<EmbeddingProjection> toRemove = projection2Index.keySet().stream()
                .filter(projection -> projection.subjectName().equals(subjectName))
                .toList();

        toRemove.forEach(this::removeEmbedding); // <- rethink

        return toRemove;
    }

    public synchronized EmbeddingProjection removeEmbedding(Embedding embedding) {
        return removeEmbedding(EmbeddingProjection.from(embedding));
    }

    public synchronized EmbeddingProjection removeEmbedding(EmbeddingProjection projection) {
        if (projection2Index.isEmpty()) {
            return null;
        }

        var index = projection2Index.remove(projection);

        // remove embedding by concatenating sub lists [0, index) + [index + 1, size),
        // thus size of resulting array is decreased by one
        embeddings = Nd4j.concat(
                0,
                embeddings.get(NDArrayIndex.interval(0, index), NDArrayIndex.all()),
                embeddings.get(NDArrayIndex.interval(index + 1, getSize()), NDArrayIndex.all())
        );

        // shifting (-1) all indexes, greater than current one
        projection2Index.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > index)
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> projection2Index.replace(e.getKey(), e.getValue(), e.getValue() - 1));

        return projection;
    }

    public synchronized Optional<INDArray> getRawEmbeddingById(UUID embeddingId) {
        return findByEmbeddingId(
                embeddingId,
                // return duplicated row
                entry -> embeddings.getRow(entry.getValue(), true).dup()
        );
    }

    public synchronized Optional<String> getSubjectNameByEmbeddingId(UUID embeddingId) {
        return findByEmbeddingId(
                embeddingId,
                entry -> entry.getKey().subjectName()
        );
    }

    private <T> Optional<T> findByEmbeddingId(UUID embeddingId, Function<Map.Entry<EmbeddingProjection, Integer>, T> func) {
        validImageId(embeddingId);

        return Optional.ofNullable(projection2Index.entrySet()
                .stream()
                .filter(entry ->  embeddingId.equals(entry.getKey().embeddingId()))
                .findFirst()
                .map(func)
                .orElseThrow(IncorrectImageIdException::new));
    }

    private void validImageId(UUID embeddingId) {
        if (embeddingId == null) {
            throw new IncorrectImageIdException();
        }
    }
}
