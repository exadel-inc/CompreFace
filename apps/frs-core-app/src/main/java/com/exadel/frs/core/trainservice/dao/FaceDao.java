package com.exadel.frs.core.trainservice.dao;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.EmbeddingFaceList;
import com.exadel.frs.core.trainservice.entity.mongo.Face;
import com.exadel.frs.core.trainservice.entity.mongo.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.mongo.FacesRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceDao {

    private final FacesRepository facesRepository;
    private final GridFsOperations gridFsOperations;

    public EmbeddingFaceList findAllFaceEmbeddings() {
        val faces = facesRepository.findAll();

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFacesIn(List<String> ids) {
        val faces = facesRepository.findByIdIn(ids);

        return facesToEmbeddingList(faces);
    }

    public EmbeddingFaceList findAllFaceEmbeddingsByApiKey(final String modelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);

        return facesToEmbeddingList(faces);
    }

    private EmbeddingFaceList facesToEmbeddingList(List<Face> faces) {
        if (faces.isEmpty()) {
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

    public List<Face> deleteFaceByName(final String faceName, final String modelApiKey) {
        val deletedFaces = facesRepository.deleteByApiKeyAndFaceName(modelApiKey, faceName);
        deleteFiles(deletedFaces);

        return deletedFaces;
    }

    public List<Face> deleteFacesByApiKey(final String modelApiKey) {
        val deletedFaces = facesRepository.deleteFacesByApiKey(modelApiKey);
        deleteFiles(deletedFaces);

        return deletedFaces;
    }

    public int countFacesInModel(final String modelApiKey) {
        return facesRepository.countByApiKey(modelApiKey);
    }

    public void updateFacesModelKey(final String modelApiKey, final String newModelApiKey) {
        val faces = facesRepository.findByApiKey(modelApiKey);
        faces.forEach(face -> face.setApiKey(newModelApiKey));

        facesRepository.saveAll(faces);
    }

    private void deleteFiles(final List<Face> deletedFaces) {
        deletedFaces.forEach(face -> {
            val deleteOriginalPhoto = new Query(new Criteria("_id").is(face.getRawImgId()));
            val deleteCroppedPhoto = new Query(new Criteria("_id").is(face.getFaceImgId()));
            gridFsOperations.delete(deleteOriginalPhoto);
            gridFsOperations.delete(deleteCroppedPhoto);
        });
    }
}