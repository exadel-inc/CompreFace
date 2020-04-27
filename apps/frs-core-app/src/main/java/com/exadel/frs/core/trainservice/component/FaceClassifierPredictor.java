package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.dao.ModelDao;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FaceClassifierPredictor {

    private final ModelDao modelDao;
    private final ApplicationContext context;

    public Pair<Integer, String> predict(final String appKey, final String modelKey, double[] input) {
        val model = modelDao.getModel(modelKey);
        FaceClassifierAdapter classifierAdapter = context.getBean(FaceClassifierAdapter.class);
        classifierAdapter.setClassifier(model);

        return classifierAdapter.predict(input);
    }
}
