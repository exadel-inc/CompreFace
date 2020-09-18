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

import static java.lang.Math.min;
import static java.util.Arrays.sort;
import static org.nd4j.linalg.factory.Nd4j.create;
import static org.nd4j.linalg.factory.Nd4j.createFromArray;
import com.google.common.primitives.Doubles;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;

public class EuclideanDistanceClassifier implements Classifier {

    private final List<String> faces;
    private INDArray indArray;

    public EuclideanDistanceClassifier(final List<String> faces) {
        this.faces = faces;
    }

    @Override
    public void train(final double[][] input, final int[] output) {
        indArray = createFromArray(input);
    }

    @Override
    public List<Pair<Double, String>> predict(final double[] input, final int resultCount) {
        val inputFace = create(input);
        val probabilities = recognize(inputFace, indArray);
        val argSort = argSort(probabilities);
        val result = new LinkedList<Pair<Double, String>>();

        for (int i = 0; i < min(resultCount, argSort.length); i++) {
            val faceName = faces.get(argSort[i]);
            val prob = probabilities[argSort[i]];
            result.add(Pair.of(prob, faceName));
        }

        return result;
    }

    @Override
    public boolean isTrained() {
        return indArray != null;
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
        distance.divi(2).rsubi(1);

        return distance.toDoubleVector();
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