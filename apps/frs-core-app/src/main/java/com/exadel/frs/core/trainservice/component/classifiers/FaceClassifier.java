package com.exadel.frs.core.trainservice.component.classifiers;


import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface FaceClassifier extends Serializable {

    void train(double[][] input, int[] output, Map<Integer, Pair<String, String>> labelMap);

    Pair<Integer, String> predict(double[] input);

    List<String> getUsedFaceIds();

    boolean isTrained();
}