package com.exadel.frs.core.trainservice.component.classifiers;

import java.util.Map;
import org.springframework.data.util.Pair;

public interface FaceClassifier {

    void train(double[][] input, int[] output, Map<Integer, String> labelMap);

    Pair<Integer, String> predict(double[] input);

    boolean isTrained();
}