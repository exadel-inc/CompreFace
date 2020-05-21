package com.exadel.frs.core.trainservice.component.classifiers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

class LogisticRegressionExtendedClassifierTestIT {

    @Test
    void train() {
        //TODO
    }

    @Test
    void predict() {
        val faceName = "faceName";
        val predictResult = Pair.of(0, faceName);
        val labelMap = Map.of(0, Pair.of("faceId", faceName));
        val classifier = new LogisticRegressionExtendedClassifier(labelMap);
        val xorMatrix = new double[][]{{0, 0}, {1, 0}};
        val xorResults = new int[]{0, 1};

        classifier.train(xorMatrix, xorResults);

        val actual = classifier.predict(new double[]{0, 0});

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(predictResult);
    }

    @Test
    void predictThrowsException() {
        assertThrows(
                ModelNotTrainedException.class,
                () -> new LogisticRegressionExtendedClassifier(null).predict(null)
        );
    }

    @Test
    void isTrained() {
        //TODO
    }

    @Test
    void getUsedFaceIds() {
        //TODO
    }
}