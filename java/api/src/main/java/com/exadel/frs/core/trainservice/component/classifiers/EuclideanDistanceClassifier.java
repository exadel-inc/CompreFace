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

package com.exadel.frs.core.trainservice.component.classifiers;

import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.exadel.frs.core.trainservice.cache.EmbeddingCacheProvider;
import com.google.common.primitives.Doubles;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.lang.Math.min;

@Component
@RequiredArgsConstructor
public class EuclideanDistanceClassifier implements Classifier {

    public static final int PREDICTION_COUNT_INFINITY = -1;
    private final EmbeddingCacheProvider embeddingCacheProvider;
    private final FacesApiClient facesApiClient;

    @Override
    public List<Pair<Double, String>> predict(final double[] input, final String apiKey, final int resultCount) {
        INDArray inputFace = Nd4j.create(input);
        inputFace = normalizeOne(inputFace);

        var embeddingCollection = embeddingCacheProvider.getOrLoad(apiKey);
        final INDArray embeddings = embeddingCollection.getEmbeddings();

        var result = new ArrayList<Pair<Double, String>>();
        if (embeddings != null && embeddings.length() > 0) {
            val probabilities = recognize(inputFace, embeddings);
            val sortedIndexes = sortedIndexes(probabilities);
            val indexMap = embeddingCollection.getIndexMap();
            int predictionCount = getPredictionCount(resultCount, sortedIndexes);

            for (int i = 0; i < min(predictionCount, sortedIndexes.length); i++) {
                var prob = probabilities[sortedIndexes[i]];
                var embedding = indexMap.get(sortedIndexes[i]);

                result.add(Pair.of(prob, embedding.subjectName()));
            }
        }
        return result;
    }

    private int getPredictionCount(int resultCount, int[] argSort) {
        if (resultCount == PREDICTION_COUNT_INFINITY) {
            resultCount = argSort.length;
        }

        return resultCount;
    }

    @Override
    public double[] verify(double[] sourceImageEmbedding, double[][] targetImageEmbedding) {
        final INDArray sourceNormalized = normalizeOne(Nd4j.create(sourceImageEmbedding));
        final INDArray targetNormalized = normalize(Nd4j.create(targetImageEmbedding));

        return recognize(sourceNormalized, targetNormalized);
    }

    @Override
    public Double verify(final double[] input, final String apiKey, final UUID embeddingId) {
        if (input == null) {
            return (double) 0;
        }

        final Optional<INDArray> rawEmbeddingOptional = embeddingCacheProvider.getOrLoad(apiKey)
                .getRawEmbeddingById(embeddingId);

        if (rawEmbeddingOptional.isEmpty())  {
            return (double) 0;
        }

        var probabilities = recognize(
                normalizeOne(Nd4j.create(input)),
                rawEmbeddingOptional.get()
        );

        return probabilities[0];
    }

    private INDArray normalizeOne(final INDArray embeddings) {
        val embeddings1Norm = embeddings.norm2();

        return embeddings.divi(embeddings1Norm);
    }

    public double[] normalizeOne(final double[] rawEmbeddings) {
        INDArray embeddings = Nd4j.create(rawEmbeddings);
        embeddings = normalizeOne(embeddings);

        return embeddings.toDoubleVector();
    }

    private INDArray normalize(final INDArray embeddings) {
        val embeddingsNorm = embeddings.norm2(1);

        return embeddings.transposei().divi(embeddingsNorm).transposei();
    }

    private double[] recognize(final INDArray newFace, final INDArray existingFaces) {
        val distance = euclidean_distance(newFace, existingFaces);

        return calculateSimilarities(distance).toDoubleVector();
    }

    private INDArray calculateSimilarities(INDArray distance) {
        FacesStatusResponse status = facesApiClient.getStatus();
        if (status == null || status.getSimilarityCoefficients() == null || status.getSimilarityCoefficients().isEmpty()) {
            throw new FacesServiceException("No status information received");
        }

        List<Double> coefficients = status.getSimilarityCoefficients();
        // (tanh ((coef0 - distance) * coef1) + 1) / 2
        return Transforms.tanh(distance.rsubi(coefficients.get(0)).muli(coefficients.get(1)), false).addi(1).divi(2);
    }

    private static INDArray euclidean_distance(final INDArray newFace, INDArray existingFaces) {
        existingFaces = existingFaces.subi(newFace);

        return existingFaces.norm2(1);
    }

    /**
     * Create and sort array of indexes according highest probabilities.
     *
     * @param probabilities array of probability
     * @return sorted array of indexes (highest probability index first)
     */
    private static int[] sortedIndexes(double[] probabilities) {
        return IntStream.range(0, probabilities.length)
                .boxed()
                .sorted((index1, index2) -> -Doubles.compare(probabilities[index1], probabilities[index2]))
                .mapToInt(index -> index)
                .toArray();
    }
}
