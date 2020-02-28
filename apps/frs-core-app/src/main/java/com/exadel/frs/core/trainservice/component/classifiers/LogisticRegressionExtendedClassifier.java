package com.exadel.frs.core.trainservice.component.classifiers;

import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import smile.classification.LogisticRegressionExt;
import smile.classification.LogisticRegressionExt.Trainer;

@Component
@Scope("prototype")
public class LogisticRegressionExtendedClassifier implements FaceClassifier {

    private Map<Integer, String> labelMap;
    private LogisticRegressionExt logisticRegression;

    @Override
    public void train(final double[][] x, final int[] y, final Map<Integer, String> labelMap) {
        this.labelMap = labelMap;

        var trainer = new Trainer();
        trainer.setMaxNumIteration(50);
        trainer.setTolerance(0.005);
        this.logisticRegression = trainer.train(x, y);
    }

    @Override
    public Pair<Integer, String> predict(final double[] input) {
        if (isTrained()) {
            var predict = logisticRegression.predict(input);
            return Pair.of(predict, labelMap.get(predict));
        }

        throw new RuntimeException("Model not trained");
    }

    @Override
    public boolean isTrained() {
        return !(logisticRegression == null);
    }
}