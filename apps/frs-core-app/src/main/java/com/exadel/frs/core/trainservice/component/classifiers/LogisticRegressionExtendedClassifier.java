package com.exadel.frs.core.trainservice.component.classifiers;

import static java.util.stream.Collectors.toList;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt;
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt.Trainer;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

public class LogisticRegressionExtendedClassifier implements FaceClassifier {

    private static final long serialVersionUID = 1966949081344084764L;

    private static final double LAMBDA = 0;
    private static final double TOLERANCE = 0.005;
    private static final int MAX_ITER = 50;

    private final Trainer trainer = new Trainer(LAMBDA, TOLERANCE, MAX_ITER);
    private final Map<Integer, Pair<String, String>> labelMap;
    private LogisticRegressionExt logisticRegression;

    public LogisticRegressionExtendedClassifier(final Map<Integer, Pair<String, String>> labelMap) {
        this.labelMap = labelMap;
    }

    @Override
    public void train(final double[][] x, final int[] y) {
        this.logisticRegression = this.trainer.train(x, y);
    }

    @Override
    public Pair<Integer, String> predict(final double[] input) {
        if (isTrained()) {
            val predict = logisticRegression.predict(input);

            return Pair.of(predict, labelMap.get(predict).getRight());
        }

        throw new ModelNotTrainedException();
    }

    @Override
    public boolean isTrained() {
        return logisticRegression != null;
    }

    @Override
    public List<String> getUsedFaceIds() {
        if (labelMap == null) {
            return List.of();
        }

        return labelMap.values().stream()
                       .map(Pair::getLeft)
                       .collect(toList());
    }
}