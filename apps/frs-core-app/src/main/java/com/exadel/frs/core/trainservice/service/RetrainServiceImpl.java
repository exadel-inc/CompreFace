package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RetrainServiceImpl implements RetrainService {

    private final FaceClassifierStorage storage;
    private final FaceDao faceDao;

    @Override
    public void startRetrain(String appKey, String modelKey) {
        storage.lock(appKey, modelKey);
        storage.getFaceClassifier(appKey, modelKey)
               .train(faceDao.findAllFaceEmbeddingsByApiKey(modelKey), appKey, modelKey);
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