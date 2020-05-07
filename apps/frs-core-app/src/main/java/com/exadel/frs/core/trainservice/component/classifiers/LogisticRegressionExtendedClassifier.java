package com.exadel.frs.core.trainservice.component.classifiers;

import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt;
import com.exadel.frs.core.trainservice.ml.LogisticRegressionExt.Trainer;
import lombok.val;
import org.springframework.context.annotation.Scope;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Scope("prototype")
public class LogisticRegressionExtendedClassifier implements FaceClassifier {

    static final long serialVersionUID = -1866949081344084764L;

    private Map<Integer, Pair<String, String>> labelMap;
    private LogisticRegressionExt logisticRegression;

    @Override
    public void train(final double[][] x, final int[] y, final Map<Integer, Pair<String, String>> labelMap) {
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

            return Pair.of(predict, labelMap.get(predict).getRight());
        }

        throw new RuntimeException("Model not trained");
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
                .collect(Collectors.toList());
    }
}