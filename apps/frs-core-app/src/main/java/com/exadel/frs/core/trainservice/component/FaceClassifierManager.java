package com.exadel.frs.core.trainservice.component;

import com.exadel.frs.core.trainservice.component.classifiers.FaceClassifier;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import com.exadel.frs.core.trainservice.exception.ModelHasNoFacesException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FaceClassifierManager {

    private final ModelDao modelDao;
    private final FaceDao faceDao;
    private final FaceClassifierLockManager lockManager;
    private final ApplicationContext context;

    public void saveClassifier(String modelKey, FaceClassifier classifier, String calculatorVersion) {
        try {
            modelDao.saveModel(modelKey, classifier, calculatorVersion);
        } finally {
            lockManager.unlock(modelKey);
        }
    }

    public void removeFaceClassifier(final String modelKey) {
        lockManager.unlock(modelKey);
        modelDao.deleteModel(modelKey);
    }

    public void initNewClassifier(String modelKey, List<String> faces) {
        lockManager.lock(modelKey);
        val proxy = context.getBean(FaceClassifierAdapter.class);
        proxy.train(faceDao.findAllFacesIn(faces), modelKey);
    }

    public void initNewClassifier(String modelKey) {
        if (faceDao.countFacesInModel(modelKey) < 1) {
            throw new ModelHasNoFacesException();
        }

        lockManager.lock(modelKey);
        val proxy = context.getBean(FaceClassifierAdapter.class);
        proxy.train(faceDao.findAllFaceEmbeddingsByApiKey(modelKey), modelKey);
    }

    public void abortClassifierTraining(String modelKey) {
        lockManager.unlock(modelKey);
    }

    public boolean isTraining(String modelKey) {
        return lockManager.isLocked(modelKey);
    }
}