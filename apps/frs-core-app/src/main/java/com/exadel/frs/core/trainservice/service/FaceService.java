package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.getTrainingOption;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;

import com.exadel.frs.core.trainservice.component.FaceClassifierManager;
import com.exadel.frs.core.trainservice.dao.FaceDao;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.system.SystemService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceDao faceDao;
    private final SystemService systemService;
    private final RetrainService retrainService;
    private final FaceClassifierManager classifierManager;
    private final GridFsOperations gridFsOperations;

    public Map<String, List<String>> findAllFaceNames(final String apiKey) {
        val token = systemService.buildToken(apiKey);

        return faceDao.findAllFaceNamesByApiKey(token.getModelApiKey());
    }

    public void deleteFaceByName(
            final String faceName,
            final String apiKey,
            final String retrain
    ) {
        val faceNameEncoded = encode(faceName, UTF_8);
        val token = systemService.buildToken(apiKey);

        val deletedFaces = faceDao.deleteFaceByName(faceNameEncoded, token.getModelApiKey());
        deleteFiles(deletedFaces);

        getTrainingOption(retrain).run(token, retrainService);
    }

    public int deleteFacesByModel(final String apiKey) {
        val token = systemService.buildToken(apiKey);
        classifierManager.removeFaceClassifier(token.getModelApiKey());
        val deletedFaces = faceDao.deleteFacesByApiKey(token.getModelApiKey());
        deleteFiles(deletedFaces);

        return deletedFaces.size();
    }

    private void deleteFiles(List<Face> deletedFaces) {
        deletedFaces.forEach(face -> {
            Query deleteOriginalPhoto = new Query(new Criteria("_id").is(face.getRawImgId()));
            Query deleteCroppedPhoto = new Query(new Criteria("_id").is(face.getFaceImgId()));
            gridFsOperations.delete(deleteOriginalPhoto);
            gridFsOperations.delete(deleteCroppedPhoto);
        });
    }

    public void updateModelApiKeyForFaces(String apiKey, String newModelApiKey) {
        val token = systemService.buildToken(apiKey);
        faceDao.updateFacesModelKey(token.getModelApiKey(), newModelApiKey);
    }
}