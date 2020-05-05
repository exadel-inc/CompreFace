package com.exadel.frs.core.trainservice.dao;

import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.entity.Face;
import com.exadel.frs.core.trainservice.entity.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import com.exadel.frs.core.trainservice.system.Token;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
public class FaceDao {

    private final FacesRepository facesRepository;

    public EmbeddingFaceList findAllFaceEmbeddings() {
        val faces = facesRepository.findAll();

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFacesIn(List<String> ids){
        val faces = facesRepository.findByIdIn(ids);

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFaceEmbeddingsByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);

        return facesToEmbeddingList(faces);
    }

    private EmbeddingFaceList facesToEmbeddingList(List<Face> faces){
        if (faces.isEmpty()){
            return new EmbeddingFaceList();
        }
        Map<Pair<String, String>, List<List<Double>>> map = faces.stream()
                .collect(toMap(face -> Pair.of(face.getId(), face.getFaceName()),
                        face -> face.getEmbeddings().stream()
                                .map(Embedding::getEmbedding)
                                .collect(toList()), (l1, l2) -> Stream
                                .concat(l1.stream(), l2.stream())
                                .collect(toList())
                ));

        EmbeddingFaceList embeddingFaceList = new EmbeddingFaceList();
        embeddingFaceList.setFaceEmbeddings(map);
        embeddingFaceList.setCalculatorVersion(faces.get(0).getEmbeddings().get(0).getCalculatorVersion());
        return embeddingFaceList;
    }

    public Map<String, List<String>> findAllFaceNamesByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);
        val faceNames = faces.stream()
                             .map(Face::getFaceName)
                             .distinct()
                             .collect(toList());

        return Map.of("names", faceNames);
    }

    public void deleteFaceByName(final String faceName, final Token token) {
        facesRepository.deleteByApiKeyAndFaceName(token.getModelApiKey(), faceName);
    }

    public List<Face> deleteFacesByApiKey(final Token token) {
        return facesRepository.deleteFacesByApiKey(token.getModelApiKey());
    }
}