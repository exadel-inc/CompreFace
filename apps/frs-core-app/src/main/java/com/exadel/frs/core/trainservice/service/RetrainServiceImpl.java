package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrainServiceImpl implements RetrainService {

    private final FaceClassifierManager manager;

    @Override
    public void startRetrain(final String appKey, final String modelKey) {
        manager.initNewClassifier(appKey, modelKey);
    }

    @Override
    public boolean isTrainingRun(final String appKey, final String modelKey) {
        return manager.isTraining(appKey, modelKey);
    }

    @Override
    public void abortTraining(final String appKey, final String modelKey) {
        manager.abortClassifierTraining(appKey, modelKey);
    }
}