package com.exadel.frs.core.trainservice.service;

import com.exadel.frs.core.trainservice.domain.Face;
import com.exadel.frs.core.trainservice.domain.Face.Embedding;
import com.exadel.frs.core.trainservice.repository.FacesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class FaceService {

  private final FacesRepository facesRepository;

  public Map<String, List<List<Double>>> findAllFaceEmbeddings() {
    List<Face> all = facesRepository.findAll();
    return all.stream()
        .collect(Collectors.toMap(Face::getFaceName,
            face -> face.getEmbeddings()
                .stream()
                .map(Embedding::getEmbedding)
                .collect(Collectors.toList()), (l1, l2) -> Stream
                .concat(l1.stream(), l2.stream())
                .collect(Collectors.toList())));
  }

  public Map<String, List<List<Double>>> findAllFaceEmbeddingsByAppKey(String appKey) {
    List<Face> all = facesRepository.findByApiKey(appKey);
    return all.stream()
        .collect(Collectors.toMap(Face::getFaceName,
            face -> face.getEmbeddings()
                .stream()
                .map(Embedding::getEmbedding)
                .collect(Collectors.toList()), (l1, l2) -> Stream
                .concat(l1.stream(), l2.stream())
                .collect(Collectors.toList())));
  }

  public Map<String, List<String>> findAllFaceNamesByApiKey(String appKey){
    List<Face> faces = facesRepository.findByApiKey(appKey);
    List<String> faceNames = faces.stream()
            .map(Face::getFaceName)
            .collect(Collectors.toList());

    Map<String, List<String>> response = new HashMap();
    response.put("names", faceNames);

    return response;
  }

}
