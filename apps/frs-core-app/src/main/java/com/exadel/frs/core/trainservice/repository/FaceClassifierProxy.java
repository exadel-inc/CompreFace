package com.exadel.frs.core.trainservice.repository;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
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
            final String modelKey
    ) {
        try {
            Thread.currentThread().setName(appKey + modelKey);
            val faceId = 0;
            val x = new ArrayList<double[]>();
            val y = new ArrayList<Integer>();
            val labelMap = new HashMap<Integer, String>();

            for (val faceName : faceNameEmbeddings.keySet()) {
                labelMap.put(faceId, faceName);
                val lists = faceNameEmbeddings.get(faceName).stream()
                                              .filter(list -> isNotEmpty(list))
                                              .collect(toList());
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
            storage.unlock(appKey, modelKey);
        }
    }

    public void trainSync(
            final Map<String, List<List<Double>>> faceNameEmbeddings,
            final String appKey,
            final String modelKey
    ) {
        this.train(faceNameEmbeddings, appKey, modelKey);
    }

    public Pair<Integer, String> predict(final double[] x) {
        return classifier.predict(x);
    }

    public FaceClassifier getClassifier() {
        return classifier;
    }
}