package com.exadel.frs.core.trainservice.repository;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Setter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@Setter
@Scope(value = "prototype")
public class FaceClassifierProxy {

    public static final String CLASSIFIER_IMPLEMENTATION_BEAN_NAME = "logisticRegressionExtendedClassifier";

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FaceClassifierStorage storage;

    private FaceClassifier classifier;

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
            val x = new ArrayList<double[]>();
            val y = new ArrayList<Integer>();
            val labelMap = new HashMap<Integer, String>();

            for (val faceName : faceNameEmbeddings.keySet()) {
                labelMap.put(faceId, faceName);
                val lists = faceNameEmbeddings.get(faceName).stream()
                                              .filter(list -> !CollectionUtils.isEmpty(list))
                                              .collect(Collectors.toList());
                for (val list : lists) {
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

    public void trainSync(
            final Map<String, List<List<Double>>> faceNameEmbeddings,
            final String appKey,
            final String modelId
    ) {
        this.train(faceNameEmbeddings, appKey, modelId);
    }

    public Pair<Integer, String> predict(final double[] x) {
        return classifier.predict(x);
    }

    public FaceClassifier getClassifier() {
        return classifier;
    }
}