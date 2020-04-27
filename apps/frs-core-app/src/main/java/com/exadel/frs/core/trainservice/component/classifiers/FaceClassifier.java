package com.exadel.frs.core.trainservice.component.classifiers;

import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.util.Map;

public interface FaceClassifier extends Serializable {

    void train(double[][] input, int[] output, Map<Integer, String> labelMap);

    Pair<Integer, String> predict(double[] input);

    boolean isTrained();
}