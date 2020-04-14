package com.exadel.frs.core.trainservice.dao;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.domain.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.FaceClassifierStorage;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.Token;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceDao {

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

    public Map<String, List<List<Double>>> findAllFaceEmbeddingsByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);

        return faces.stream()
                    .collect(toMap(Face::getFaceName,
                            face -> face.getEmbeddings().stream()
                                        .map(Embedding::getEmbedding)
                                        .collect(toList()), (l1, l2) -> Stream
                                    .concat(l1.stream(), l2.stream())
                                    .collect(toList())
                    ));
    }

    public Map<String, List<String>> findAllFaceNamesByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);
        val faceNames = faces.stream()
                             .map(Face::getFaceName)
                             .collect(toList());

        return Map.of("names", faceNames);
    }

    public void deleteFaceByName(final String faceName, final Token token) {
        facesRepository.deleteByApiKeyAndFaceName(token.getAppApiKey(), faceName);
    }

    public List<Face> deleteFacesByApiKey(final Token token) {
        storage.removeFaceClassifier(token.getAppApiKey(), token.getModelApiKey());

        return facesRepository.deleteFacesByApiKey(token.getModelApiKey());
    }
}