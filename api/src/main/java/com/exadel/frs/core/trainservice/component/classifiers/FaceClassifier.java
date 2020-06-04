package com.exadel.frs.core.trainservice.component.classifiers;

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public interface FaceClassifier extends Serializable {

    void train(double[][] input, int[] output);

    Pair<Integer, String> predict(double[] input);

    List<String> getUsedFaceIds();

    boolean isTrained();
}