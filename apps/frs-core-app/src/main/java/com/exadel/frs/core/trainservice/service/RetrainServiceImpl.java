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
    public void startRetrain(final String appKey, final String modelKey) {
        storage.lock(appKey, modelKey);
        storage.getFaceClassifier(appKey, modelKey)
               .train(faceDao.findAllFaceEmbeddingsByApiKey(modelKey), appKey, modelKey);
    }

    @Override
    public boolean isTrainingRun(final String appKey, final String modelKey) {
        return storage.isLocked(appKey, modelKey);
    }

    @Override
    public void abortTraining(final String appKey, final String modelKey) {
        storage.unlock(appKey, modelKey);
    }
}