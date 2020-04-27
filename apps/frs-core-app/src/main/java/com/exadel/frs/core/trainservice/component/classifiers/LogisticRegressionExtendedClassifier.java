package com.exadel.frs.core.trainservice.component.classifiers;

import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt;
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt.Trainer;

import java.util.Map;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LogisticRegressionExtendedClassifier implements FaceClassifier {

    private Map<Integer, String> labelMap;
    private LogisticRegressionExt logisticRegression;

    @Override
    public void train(final double[][] x, final int[] y, final Map<Integer, String> labelMap) {
        this.labelMap = labelMap;
        val trainer = new Trainer();
        trainer.setMaxNumIteration(50);
        trainer.setTolerance(0.005);
        this.logisticRegression = trainer.train(x, y);
    }

    @Override
    public Pair<Integer, String> predict(final double[] input) {
        if (isTrained()) {
            val predict = logisticRegression.predict(input);

            return Pair.of(predict, labelMap.get(predict));
        }

        throw new RuntimeException("Model not trained");
    }

    @Override
    public boolean isTrained() {
        return !(logisticRegression == null);
    }

}