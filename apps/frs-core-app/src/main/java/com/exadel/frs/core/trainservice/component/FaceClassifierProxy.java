package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Setter
@Scope(value = "prototype")
public class FaceClassifierProxy {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FaceClassifierStorage storage;

    private FaceClassifier classifier;

    public static final String CLASSIFIER_IMPLEMENTATION_BEAN_NAME = "logisticRegressionExtendedClassifier";

    @PostConstruct
    public void postConstruct() {
        classifier = (FaceClassifier) applicationContext.getBean(CLASSIFIER_IMPLEMENTATION_BEAN_NAME);
    }

    @Async
    public void train(
            final Map<String, List<List<Double>>> faceNameEmbeddings,
            final String appKey,
            final String modelId
    ) {
        try {
            Thread.currentThread().setName(appKey + modelId);
            var faceId = 0;
            var x = new ArrayList<double[]>();
            var y = new ArrayList<Integer>();
            var labelMap = new HashMap<Integer, String>();

            for (var faceName : faceNameEmbeddings.keySet()) {
                labelMap.put(faceId, faceName);
                var lists = faceNameEmbeddings.get(faceName);
                for (var list : lists) {
                    x.add(list.stream().mapToDouble(d -> d).toArray());
                    y.add(faceId);
                }
                faceId++;
            }

            classifier.train(
                    x.toArray(double[][]::new),
                    y.stream().mapToInt(integer -> integer).toArray(),
                    labelMap
            );
        } finally {
            storage.unlock(appKey, modelId);
        }
    }

    public void trainSync(Map<String, List<List<Double>>> faceNameEmbeddings, String appKey,
                          String modelId) {
        this.train(faceNameEmbeddings, appKey, modelId);
    }

    public Pair<Integer, String> predict(double[] x) {
        return classifier.predict(x);
    }

    public FaceClassifier getClassifier() {
        return classifier;
    }
}