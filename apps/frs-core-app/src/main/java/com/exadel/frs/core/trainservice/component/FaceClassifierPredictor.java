package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.dao.ModelDao;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FaceClassifierPredictor {

    private final ModelDao modelDao;
    private final ApplicationContext context;

    public Pair<Integer, String> predict(final String modelKey, double[] input) {
        val model = modelDao.getModel(modelKey);
        val classifierAdapter = context.getBean(FaceClassifierAdapter.class);
        classifierAdapter.setClassifier(model);

        return classifierAdapter.predict(input);
    }
}
