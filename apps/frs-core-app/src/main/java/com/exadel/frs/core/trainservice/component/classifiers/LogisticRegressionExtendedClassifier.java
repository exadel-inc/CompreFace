package com.exadel.frs.core.trainservice.component.classifiers;

import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import smile.classification.LogisticRegressionExt;
import smile.classification.LogisticRegressionExt.Trainer;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Scope("prototype")
public class LogisticRegressionExtendedClassifier implements FaceClassifier {

  private Map<Integer, String> labelMap;

  private LogisticRegressionExt logisticRegression;

  @Override
  public void train(double[][] x, int[] y, Map<Integer, String> labelMap) {
    this.labelMap = labelMap;
    Trainer trainer = new Trainer();
    trainer.setMaxNumIteration(50);
    trainer.setTolerance(0.005);
    this.logisticRegression = trainer.train(x, y);
  }

  @Override
  public Pair<Integer, String> predict(double[] input) {
    if (isTrained()) {
      int predict = logisticRegression.predict(input);
      return Pair.of(predict, labelMap.get(predict));
    }
    throw new RuntimeException("Model not trained");
  }

  @Override
  public boolean isTrained() {
    return !(logisticRegression == null);
  }
}
