package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.service.ModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FaceClassifierPredictor {

    private final ModelService modelService;
    private final ApplicationContext context;

    public Pair<Integer, String> predict(final String appKey, final String modelId, double[] input){
        FaceClassifier model = modelService.getModel(modelId);
        FaceClassifierProxy classifierProxy = context.getBean(FaceClassifierProxy.class);
        classifierProxy.setClassifier(model);
        return classifierProxy.predict(input);
    }
}
