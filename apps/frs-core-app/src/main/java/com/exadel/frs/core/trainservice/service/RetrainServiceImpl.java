package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrainServiceImpl implements RetrainService {

    private final FaceClassifierManager manager;

    @Override
    public void startRetrain(String appKey, String modelId) {
        manager.initNewClassifier(appKey, modelId);
    }

    @Override
    public boolean isTrainingRun(String appKey, String modelId) {
        return manager.isTraining(appKey, modelId);
    }

    @Override
    public void abortTraining(String appKey, String modelId) {
        manager.abortClassifierTraining(appKey, modelId);
    }
}