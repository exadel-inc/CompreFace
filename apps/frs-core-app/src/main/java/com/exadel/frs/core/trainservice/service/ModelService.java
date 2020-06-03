package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.dao.ModelDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final FaceDao faceDao;
    private final ModelDao modelDao;

    public void updateModelApiKeyForFaces(final String modelKey, final String newModelApiKey) {
        modelDao.updateModelApiKey(modelKey, newModelApiKey);
        faceDao.updateFacesModelKey(modelKey, newModelApiKey);
    }
}