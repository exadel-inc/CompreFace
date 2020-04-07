package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrainServiceImpl implements RetrainService {

    private final FaceClassifierStorage storage;
    private final FaceService faceService;

    @Override
    public void startRetrain(String appKey, String modelId) {
        storage.lock(appKey, modelId);
        storage.getFaceClassifier(appKey, modelId)
               .train(faceService.findAllFaceEmbeddingsByApiKey(appKey), appKey, modelId);
    }

    @Override
    public boolean isTraining(String appKey, String modelId) {
        return storage.isLocked(appKey, modelId);
    }

    @Override
    public void abortTraining(String appKey, String modelId) {
        storage.unlock(appKey, modelId);
    }
}