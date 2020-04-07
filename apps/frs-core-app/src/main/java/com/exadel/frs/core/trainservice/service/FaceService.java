package com.exadel.frs.core.trainservice.service;

import static com.exadel.frs.core.trainservice.enums.RetrainOption.getTrainingOption;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.domain.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FaceClassifierStorage storage;
    private final FacesRepository facesRepository;

    public Map<String, List<List<Double>>> findAllFaceEmbeddings() {
        val faces = facesRepository.findAll();

        return faces.stream()
                .collect(toMap(Face::getFaceName,
                        face -> face.getEmbeddings().stream()
                                .map(Embedding::getEmbedding)
                                .collect(toList()), (l1, l2) -> Stream
                                .concat(l1.stream(), l2.stream())
                                .collect(toList())
                ));
    }

    public Map<String, List<List<Double>>> findAllFaceEmbeddingsByApiKey(final String apiKey) {
        val faces = facesRepository.findByApiKey(apiKey);

        return faces.stream()
                .collect(toMap(Face::getFaceName,
                        face -> face.getEmbeddings().stream()
                                .map(Embedding::getEmbedding)
                                .collect(toList()), (l1, l2) -> Stream
                                .concat(l1.stream(), l2.stream())
                                .collect(toList())
                ));
    }

    public Map<String, List<String>> findAllFaceNamesByApiKey(final String apiKey) {
        var faces = facesRepository.findByApiKey(apiKey);
        var faceNames = faces.stream()
                .map(Face::getFaceName)
                .collect(toList());

        return Map.of("names", faceNames);
    }

    public void deleteFaceByNameAndTrainModelIfRequired(
            final String faceName,
            final String appKey,
            final String modelGuid,
            final String retrain
    ) {
        facesRepository.deleteByApiKeyAndFaceName(appKey, faceName);
        handleModelTraining(appKey, modelGuid, retrain);
    }

    public List<Face> deleteFacesByApiKey(final String appKey, final String modelApiKey) {
        storage.removeFaceClassifier(appKey, modelApiKey);
        return facesRepository.deleteFacesByApiKey(modelApiKey);
    }

    private void handleModelTraining(final String appKey, final String modelGuid, final String retrain) {
        switch (getTrainingOption(retrain.toUpperCase())) {
            case YES:
                abortCurrentTrainingIfExists(appKey, modelGuid);
                beginNewTraining(appKey, modelGuid);
                break;
            case NO:
                break;
            default:
                abortCurrentTrainingIfExists(appKey, modelGuid);
        }
    }

    private void beginNewTraining(final String appKey, final String modelGuid) {
        storage.lock(appKey, modelGuid);
        storage.getFaceClassifier(appKey, modelGuid)
                .train(findAllFaceEmbeddingsByApiKey(appKey), appKey, modelGuid);
    }

    private void abortCurrentTrainingIfExists(final String appKey, final String modelGuid) {
        if (storage.isLocked(appKey, modelGuid)) {
            storage.unlock(appKey, modelGuid);
        }
    }
}