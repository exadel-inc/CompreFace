package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FaceClassifierManager {

    private final ModelDao modelDao;
    private final FaceDao faceDao;
    private final FaceClassifierLockManager lockManager;
    private final ApplicationContext context;

    public void saveClassifier(String appKey, String modelId, FaceClassifier classifier) {
        modelDao.saveModel(modelId, classifier);
        lockManager.unlock(appKey, modelId);
    }

    public void removeFaceClassifier(final String appKey, final String modelId) {
        lockManager.unlock(appKey, modelId);
        modelDao.deleteModel(modelId);
    }

    public void initNewClassifier(String appKey, String modelId) {
        lockManager.lock(appKey, modelId);
        val proxy = context.getBean(FaceClassifierAdapter.class);
        proxy.train(faceDao.findAllFaceEmbeddingsByApiKey(appKey), appKey, modelId);
    }

    public void abortClassifierTraining(String appKey, String modelId) {
        lockManager.unlock(appKey, modelId);
    }

    public boolean isTraining(String appKey, String modelId) {
        return lockManager.isLocked(appKey, modelId);
    }
}