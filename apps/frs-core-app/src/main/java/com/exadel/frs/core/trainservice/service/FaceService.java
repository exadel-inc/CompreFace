package com.exadel.frs.core.trainservice.service;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.domain.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final FacesRepository facesRepository;

    public Map<String, List<List<Double>>> findAllFaceEmbeddings() {
        var faces = facesRepository.findAll();

        return faces.stream()
                  .collect(toMap(Face::getFaceName,
                          face -> face.getEmbeddings()
                                      .stream()
                                      .map(Embedding::getEmbedding)
                                      .collect(toList()), (l1, l2) -> Stream
                                  .concat(l1.stream(), l2.stream())
                                  .collect(toList())
                  ));
    }

    public Map<String, List<List<Double>>> findAllFaceEmbeddingsByAppKey(final String appKey) {
        var faces = facesRepository.findByApiKey(appKey);

        return faces.stream()
                  .collect(toMap(Face::getFaceName,
                          face -> face.getEmbeddings()
                                      .stream()
                                      .map(Embedding::getEmbedding)
                                      .collect(toList()), (l1, l2) -> Stream
                                  .concat(l1.stream(), l2.stream())
                                  .collect(toList())
                  ));
    }
}