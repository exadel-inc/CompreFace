package com.exadel.frs.core.trainservice.component;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.component.classifiers.LogisticRegressionExtendedClassifier;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.exception.ModelNotTrainedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Setter
@Slf4j
@Scope(value = "prototype")
@RequiredArgsConstructor
public class FaceClassifierAdapter {

    public static final String CLASSIFIER_IMPLEMENTATION_BEAN_NAME =
            StringUtils.uncapitalize(LogisticRegressionExtendedClassifier.class.getSimpleName());

    private final ApplicationContext applicationContext;

    private final FaceClassifierManager storage;

    @Getter
    private FaceClassifier classifier;

    @PostConstruct
    public void postConstruct() {
        classifier = (FaceClassifier) applicationContext.getBean(CLASSIFIER_IMPLEMENTATION_BEAN_NAME);
    }

    @Async
    public void train(
            final EmbeddingFaceList embeddingFaceList,
            final String modelKey
    ) {
        try {
            currentThread().setName(modelKey);

            var faceId = 0;
            val x = new ArrayList<double[]>();
            val y = new ArrayList<Integer>();
            val labelMap = new HashMap<Integer, Pair<String, String>>();

            Map<Pair<String, String>, List<List<Double>>> faceNameEmbeddings = embeddingFaceList.getFaceEmbeddings();
            if (faceNameEmbeddings.isEmpty()) {
                throw new ModelNotTrainedException();
            }

            for (val faceNameId : faceNameEmbeddings.keySet()) {
                labelMap.put(faceId, faceNameId);
                val lists = faceNameEmbeddings.get(faceNameId).stream()
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

            storage.saveClassifier(modelKey, this.getClassifier(), embeddingFaceList.getCalculatorVersion());
        } catch (ModelNotTrainedException e) {
            log.error("Model {} hasn't enought data to train", modelKey);
        } finally {
            storage.abortClassifierTraining(modelKey);
        }
    }

    public void trainSync(
            final EmbeddingFaceList embeddingFaceList,
            final String modelKey
    ) {
        this.train(embeddingFaceList, modelKey);
    }

    public Pair<Integer, String> predict(final double[] x) {
        return classifier.predict(x);
    }
}