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

import com.exadel.frs.core.trainservice.cache.FaceCacheProvider;
import com.exadel.frs.commonservice.sdk.faces.FacesApiClient;
import com.exadel.frs.commonservice.sdk.faces.exception.FacesServiceException;
import com.exadel.frs.commonservice.sdk.faces.feign.dto.FacesStatusResponse;
import com.google.common.primitives.Doubles;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Arrays.sort;
import static org.nd4j.linalg.factory.Nd4j.create;
import static org.nd4j.linalg.ops.transforms.Transforms.tanh;

@Component
@RequiredArgsConstructor
public class EuclideanDistanceClassifier implements Classifier {

    public static final int PREDICTION_COUNT_INFINITY = -1;
    private final FaceCacheProvider faceCacheProvider;
    private final FacesApiClient facesApiClient;

    @Override
    public List<Pair<Double, String>> predict(final double[] input, final String apiKey, final int resultCount) {
        val inputFace = create(input);
        val faceCollection = faceCacheProvider.getOrLoad(apiKey);
        val result = new ArrayList<Pair<Double, String>>();
        if (faceCollection.getEmbeddings() != null && faceCollection.getEmbeddings().length() > 0) {
            val probabilities = recognize(inputFace, faceCollection.getEmbeddings());
            val argSort = argSort(probabilities);
            val facesMap = faceCollection.getFacesMap().inverse();
            int predictionCount = getPredictionCount(resultCount, argSort);

            for (int i = 0; i < min(predictionCount, argSort.length); i++) {
                val face = facesMap.get(argSort[i]);
                val prob = probabilities[argSort[i]];

                result.add(Pair.of(prob, face.getName()));
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
    public Double verify(double[] processFileEmbedding, double[][] checkFileEmbedding) {
        INDArray firstFace = create(processFileEmbedding);
        INDArray secondFace = create(checkFileEmbedding);
        double[] probabilities = recognize(firstFace, secondFace);
        return probabilities[0];
    }

    @Override
    public Double verify(final double[] input, final String apiKey, final String imageId) {
        if (input == null) {
            return (double) 0;
        }

        val inputFace = create(input);
        val faceCollection = faceCacheProvider.getOrLoad(apiKey);

        val probabilities = recognize(inputFace, faceCollection.getEmbeddingsByImageId(imageId));

        return probabilities[0];
    }

    private INDArray normalizeOne(final INDArray embeddings) {
        val embeddings1Norm = embeddings.norm2();

        return embeddings.divi(embeddings1Norm);
    }

    private INDArray normalize(final INDArray embeddings) {
        val embeddings1Norm = embeddings.norm2(1);

        return embeddings.divi(embeddings1Norm);
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
        return tanh(distance.rsubi(coefficients.get(0)).muli(coefficients.get(1)), false).addi(1).divi(2);
    }

    private static INDArray euclidean_distance(final INDArray newFace, INDArray existingFaces) {
        existingFaces = existingFaces.subi(newFace);

        return existingFaces.norm2(1);
    }

    private int[] argSort(final double[] a) {
        val indexes = new Integer[a.length];
        for (int i = 0; i < indexes.length; ++i) {
            indexes[i] = i;
        }

        sort(indexes, (i1, i2) -> -Doubles.compare(a[i1], a[i2]));

        val ret = new int[indexes.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = indexes[i];
        }

        return ret;
    }
}